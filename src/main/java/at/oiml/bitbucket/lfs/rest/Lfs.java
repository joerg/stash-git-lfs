package at.oiml.bitbucket.lfs.rest;

import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositorySupplier;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectSupplier;
import com.atlassian.bitbucket.server.ApplicationPropertiesService;
import com.atlassian.bitbucket.user.UserService;
import com.atlassian.bitbucket.user.ApplicationUser;


import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.servlet.http.*;
import javax.servlet.ServletOutputStream;
import javax.ws.rs.core.Context;

import com.google.gson.*;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class Lfs {
  private final ApplicationPropertiesService appPropService;
  private final File home;
  private final String lfsStore;
  private final RepositorySupplier repoSupplier;
  private final ApplicationUser bitbucketUser;
  private final PermissionService permissionService;
  private final ProjectSupplier projectSupplier;
  private final String baseUrl;

  private static final Logger log = LoggerFactory.getLogger(Lfs.class);
  private final static String contentType = "application/vnd.git-lfs+json";
  public Lfs(RepositorySupplier repoSupplier,
             ApplicationPropertiesService appPropService,
             AuthenticationContext authContext,
             PermissionService permissionService,
             ProjectSupplier projectSupplier)
  {
    this.repoSupplier = repoSupplier;
    this.appPropService = appPropService;
    this.home = appPropService.getDataDir();
    this.lfsStore = appPropService.getDataDir()+"/lfs";
    this.bitbucketUser = authContext.getCurrentUser();
    this.permissionService = permissionService;
    this.projectSupplier = projectSupplier;
    this.baseUrl = appPropService.getBaseUrl().toString();
  }

  @GET
  @Produces(contentType)
  @Path("/{project}/{repo}/objects/{oid}")
  public Response getObject(@PathParam("project") String project,
                            @PathParam("repo") String repo,
                            @PathParam("oid") String oid)
  {
    LfsObject obj = new LfsObject(oid);
    if(!repoValid(project, repo) || !userCanRead(project, repo) || !obj.existsIn(repoStore(project, repo))) {
      return Response.status(404).build();
    }

    LfsModel output = new LfsModel(baseUrl, project, repo, oid);
    Gson gson = new Gson();

    return Response.ok(gson.toJson(output)).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/{project}/{repo}/download/{oid}")
  public Response downloadObject(@PathParam("project") String project,
                                 @PathParam("repo") String repo,
                                 @PathParam("oid") String oid,
                                 @Context HttpServletResponse response)
  {
    LfsObject obj = new LfsObject(oid);
    if (!userCanRead(project, repo) || !obj.existsIn(repoStore(project, repo))) {
      return Response.status(404).build();
    }

    // Need download streaming...
    // from: http://stackoverflow.com/questions/12239868/whats-the-correct-way-to-send-a-file-from-rest-web-service-to-client
    try {
      File object = new File(repoStore(project,repo)+"/"+oidStore(oid));
      // Long is quite big. Maybe to big for GIT. Download status counter does not work...
      response.addHeader("Content-Length", Long.toString(object.length()));
      response.addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM);

      ServletOutputStream outStream = response.getOutputStream();
      FileInputStream in = new FileInputStream(repoStore(project,repo)+"/"+oidStore(oid));
      int write = 0;
      byte[] bytes = new byte[1024];
      while ((write = in.read(bytes)) != -1) {
        outStream.write(bytes, 0, write);
        outStream.flush();
      }
      if (outStream != null) {
        outStream.flush();
      }
      in.close();
      outStream.close();
      response.flushBuffer();
    }catch (IOException e) {
      log.info(e.getMessage());
    }
    return Response.noContent().status(200).build();
  }

  @POST
  @Consumes(contentType)
  @Produces(contentType)
  @Path("/{project}/{repo}/objects")
  public Response postObject( @PathParam("project") String project,
                              @PathParam("repo") String repo,
                              String body)
  {
    Gson gson = new Gson();
    LfsObject tmp = new LfsObject();
    LfsObject obj = gson.fromJson(body, tmp.getClass());

    // See Lfs API docs
    if (!userCanRead(project, repo)) {
      return Response.status(404).build();
    }
    // return 403 if user can read but not write AND operation is upload
    // if (userCanRead(project, repo) && !userCanWrite(project, repo)) {
    //  return Response.status(403).build();
    // }

    LfsModel output = new LfsModel(baseUrl, project, repo, obj.getOid(), obj.getSize());

    if (obj.existsIn(repoStore(project, repo))) {
      return Response.ok(gson.toJson(output)).status(200).build();
    } else {
      return Response.ok(gson.toJson(output)).status(202).build();
    }
  }

  @POST
  @Consumes(contentType)
  @Produces(contentType)
  @Path("/{project}/{repo}/objects/batch")
  public Response postObjectsBatch( @PathParam("project") String project,
                                    @PathParam("repo") String repo,
                                    String body)
  {
    Gson gson = new Gson();
    BatchLfsObject tmp = new BatchLfsObject();

    BatchLfsObject obj = gson.fromJson(body, tmp.getClass());
    BatchLfsModel output = new BatchLfsModel(baseUrl, repoStore(project, repo), project, repo, obj);

    return Response.ok(gson.toJson(output)).build();
  }

  @POST
  @Consumes(contentType)
  @Produces(contentType)
  @Path("/{project}/{repo}/verify")
  public Response verifyObject(@PathParam("project") String project,
                               @PathParam("repo") String repo,
                               String body)
  {
    Gson gson = new Gson();
    LfsObject tmp = new LfsObject();
    LfsObject obj = gson.fromJson(body, tmp.getClass());

    if (!userCanRead(project, repo)) {
      return Response.status(404).build();
    }

    if (obj.verify(repoStore(project,repo))) {
      return Response.ok().build();
    } else {
      return Response.ok().status(400).build();
    }
  }

  @PUT
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Produces(contentType)
  @Path("/{project}/{repo}/upload/{oid}")
  public Response uploadObject( @PathParam("project") String project,
                                @PathParam("repo") String repo,
                                @PathParam("oid") String oid,
                                InputStream body)
  {
    // No access to push changes, triggers 403 according to API docs
    if (!userCanWrite(project, repo)) {
      return Response.status(403).build();
    }

    try {
      String filePath = repoStore(project,repo);

      File dir = new File(filePath + "/" + oidStore(oid));
      dir.getParentFile().mkdirs();

      FileOutputStream outputStream = new FileOutputStream(filePath+"/"+oidStore(oid));
      int read = 0;
      byte[] bytes = new byte[1024];
      while ((read = body.read(bytes)) != -1) {
        outputStream.write(bytes, 0, read);
      }

      outputStream.close();
    } catch(FileNotFoundException fnfe) {
      log.info(fnfe.getMessage());
    } catch(IOException ioe) {
      log.info(ioe.getMessage());
    }

    // TODO??: Check consistency of file here
    // and return error if sha256 checksum invalid
    // Delete file if consitency not given!!!!!
    // API has no specifications about return codes if
    // upload failed. What to do??
    return Response.ok().build();
  }

  private boolean repoValid(String project, String repo)
  {
    Repository validate = repoSupplier.getBySlug(project, repo);
    return validate!=null;
  }

  private boolean userCanRead(String project, String repo)
  {
    return userPermissonChecker(project, repo,
                                Permission.PROJECT_READ,
                                Permission.REPO_READ);
  }

  private boolean userCanWrite(String project, String repo)
  {
    return userPermissonChecker(project, repo,
                                Permission.PROJECT_WRITE,
                                Permission.REPO_WRITE);
  }

  private boolean userPermissonChecker(String project,
                                       String repo,
                                       Permission projPerm,
                                       Permission repoPerm)
  {
    Repository theRepo = repoSupplier.getBySlug(project, repo);
    Project theProject = projectSupplier.getByKey(project);

    boolean canProject = permissionService.hasProjectPermission(bitbucketUser, theProject, projPerm);
    boolean canRepo = permissionService.hasRepositoryPermission(bitbucketUser, theRepo, repoPerm);

    return (canProject || canRepo);
  }

  private String repoStore(String project, String repo)
  {
    return lfsStore+"/"+repoSupplier.getBySlug(project, repo).getId();
  }

  // Create git like storage Path of ab/cd/abcdefg...
  private String oidStore(String oid)
  {
    return oid.substring(0,2) + "/" +
           oid.substring(2,4) + "/" +
           oid;
  }
}
