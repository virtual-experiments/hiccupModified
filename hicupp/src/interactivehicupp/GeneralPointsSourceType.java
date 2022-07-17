package interactivehicupp;

import hicupp.trees.Tree;

public class GeneralPointsSourceType implements PointsSourceType {
  public PointsSourceProvider createPointsSourceProvider(PointsSourceClient client,
                                                         Tree tree) {
    return new GeneralPointsSourceProvider(client, tree);
  }
}
