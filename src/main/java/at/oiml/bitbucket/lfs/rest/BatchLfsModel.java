package at.oiml.bitbucket.lfs.rest;

import java.util.ArrayList;
import java.util.List;

public class BatchLfsModel {
  private LfsModel[] objects;

  public BatchLfsModel() {
  }

  public BatchLfsModel(String baseUrl, String path, String project, String repo, BatchLfsObject objects) {
    List<LfsModel> valids = new ArrayList<LfsModel>();
    for (LfsObject obj : objects.getObjects()) {
      if (!obj.existsIn(path)) {
        LfsModel tmp = new LfsModel(baseUrl, project, repo, obj.getOid(), obj.getSize());
        valids.add(tmp);
      }
    }

    LfsModel[] validsArr = new LfsModel[valids.size()];
    this.objects = valids.toArray(validsArr);
  }
}
