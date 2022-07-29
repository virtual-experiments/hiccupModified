package interactivehicupp;

import hicupp.trees.Tree;

public class GeneralPointsSourceType implements PointsSourceType {
  private PointsSourceProvider generalPointsSourceProvider;

  @Override
  public PointsSourceProvider createPointsSourceProvider(PointsSourceClient client,
                                                         Tree tree) {
    generalPointsSourceProvider = new GeneralPointsSourceProvider(client, tree);
    return generalPointsSourceProvider;
  }

  @Override
  public PointsSourceProvider getPointSourceProvider() {
    return generalPointsSourceProvider;
  }
}
