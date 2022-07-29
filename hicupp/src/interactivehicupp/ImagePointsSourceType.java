package interactivehicupp;

import hicupp.trees.Tree;

public class ImagePointsSourceType implements PointsSourceType {
  PointsSourceProvider imagePointsSourceProvider;

  @Override
  public PointsSourceProvider createPointsSourceProvider(PointsSourceClient client,
                                                         Tree tree) {
    imagePointsSourceProvider = new ImagePointsSourceProvider(client, tree);
    return  imagePointsSourceProvider;
  }

  @Override
  public PointsSourceProvider getPointSourceProvider() {
    return imagePointsSourceProvider;
  }
}
