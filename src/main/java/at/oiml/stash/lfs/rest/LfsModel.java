package at.oiml.stash.lfs.rest;

import com.google.gson.annotations.*;

public class LfsModel {
  private String project;
  private String repo;
  private String oid;
  private Long size;

  @SerializedName("_links") private LfsHypermedia links;

  public LfsModel() {
  }

  /* No size given, so we have a GET request */
  public LfsModel(String baseUrl, String project, String repo, String oid) {
    this.project = project;
    this.repo = repo;
    this.oid = oid;
    this.size = 0L; /* deal with size and other stuff here */
    this.links = new LfsHypermedia(baseUrl, project, repo, oid);
  }

  /* Size given, so this is a PUT request */
  public LfsModel(String baseUrl, String project, String repo, String oid, Long size) {
    this.project = project;
    this.repo = repo;
    this.oid = oid;
    this.size = size;
    this.links = new LfsHypermedia(baseUrl, project, repo, oid);
  }

  class LfsHypermedia {
    private LfsHypermediaLink self;
    private LfsHypermediaLink download;
    private LfsHypermediaLink upload;
    private LfsHypermediaLink verify;

    public LfsHypermedia(String baseUrl, String project, String repo, String oid)
    {
      this.self = new LfsHypermediaLink("self", baseUrl, project, repo, oid);
      this.download = new LfsHypermediaLink("download", baseUrl, project, repo, oid);
      this.upload = new LfsHypermediaLink("upload", baseUrl, project, repo, oid);
      this.verify = new LfsHypermediaLink("verify", baseUrl, project, repo, oid);
    }

    class LfsHypermediaLink {
      private String href;

      public LfsHypermediaLink(String type, String baseUrl, String project, String repo, String oid)
      {
        if (type == "upload" || type == "download")
            this.href = baseUrl + "/rest/lfs/" + project + "/" + repo + "/" + type + "/" + oid;
        else if (type == "verify")
            this.href = baseUrl + "/rest/lfs/" + project + "/" + repo + "/" + type;
        else if ( type == "self" )
            this.href = baseUrl + "/rest/lfs/" + project + "/" + repo;
      }
    }
  }
}
