package interactivehicupp;

import hicupp.trees.Tree;

public class ImagePointsSourceType implements PointsSourceType {
  public PointsSourceProvider createPointsSourceProvider(PointsSourceClient client,
                                                         Tree tree) {
    return new ImagePointsSourceProvider(client, tree);
  }
}
