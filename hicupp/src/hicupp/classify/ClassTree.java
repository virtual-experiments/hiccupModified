package hicupp.classify;

import hicupp.*;
import hicupp.trees.*;

public final class ClassTree {
  private final Tree tree;
  private final ClassNode root;
  private SetOfPoints points;
  private byte[] classes;
  
  public ClassTree(Tree tree, SetOfPoints points) {
    this.tree = tree;
    this.root = new ClassNode(this, null, tree.getRoot());

    setPoints(points);
  }
  
  public Tree getTree() {
    return tree;
  }
  
  public ClassNode getRoot() {
    return root;
  }
  
  public SetOfPoints getPoints() {
    return points;
  }
  
  public void setPoints(SetOfPoints value) {
    points = value;
    
    root.newPoints();
    
    int ndims = points.getDimensionCount();
    int pointCount = points.getPointCount();
    classes = new byte[pointCount];
    
    PointIterator iter = points.createIterator();
    
    double[] point = new double[ndims];
    for (int i = 0; i < pointCount; i++) {
      iter.next();
      for (int j = 0; j < ndims; j++)
        point[j] = iter.getCoordinate(j);
      classes[i] = (byte) root.addPointAndClassify(point);
    }
    
    root.notifySubtreeNodeObservers("New Points");
  }
  
  byte[] getClasses() {
    return classes;
  }
}
