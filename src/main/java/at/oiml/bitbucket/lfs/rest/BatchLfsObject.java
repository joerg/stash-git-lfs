package at.oiml.bitbucket.lfs.rest;

public class BatchLfsObject {
  private LfsObject[] objects;

  public BatchLfsObject() {
  }

  public BatchLfsObject(LfsObject[] objects) {
    this.objects = objects;
  }

  public LfsObject[] getObjects() {
    return objects;
  }
}
