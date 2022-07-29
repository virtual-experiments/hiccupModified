package interactivehicupp;

import hicupp.trees.Tree;

public interface PointsSourceType {
  PointsSourceProvider createPointsSourceProvider(PointsSourceClient client,
                                                  Tree tree);
  PointsSourceProvider getPointSourceProvider();
}
