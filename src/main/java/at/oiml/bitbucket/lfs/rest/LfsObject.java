package at.oiml.bitbucket.lfs.rest;
import java.io.File;

public class LfsObject {
  private String oid;
  private Long size;

  public LfsObject() {
  }

  public LfsObject(String oid) {
    this.oid = oid;
    this.size = 0L;
  }

  public LfsObject(String oid, Long size) {
    this.oid = oid;
    this.size = size;
  }

  public boolean existsIn(String path)
  {
    File objFile = new File(path+"/"+oidStore(oid));
    return objFile.exists();
  }

  public boolean verify(String path)
  {
    Long fileSize = new File(path+"/"+oidStore(oid)).length();
    return existsIn(path) && fileSize.equals(size);
  }

  // Copy from Lfs.java. I don't know yet how to remove this copy...
  // Create git like storage Path of ab/cd/abcdefg...
  private String oidStore(String oid)
  {
    return oid.substring(0,2) + "/" +
           oid.substring(2,4) + "/" +
           oid;
  }

  public Long getSize() { return size; }
  public String getOid() { return oid; }
}
