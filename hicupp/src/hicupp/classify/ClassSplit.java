package hicupp.classify;

import hicupp.*;
import hicupp.trees.*;
import java.util.*;

public final class ClassSplit extends Observable {
  private final ClassTree tree;
  private final ClassNode parent, leftChild, rightChild;
  private final Split split;
  private Histogram histogram;
  private double bestThreshold;
  private final SplitObserver splitObserver = new SplitObserver();
  
  private class SplitObserver implements Observer {
    public void update(Observable observable, Object object) {
      splitChanged();
    }
  }
  
  ClassSplit(ClassTree tree, ClassNode parent, Split split) {
    this.tree = tree;
    this.parent = parent;
    this.split = split;
    split.addObserver(splitObserver);
    
    leftChild = new ClassNode(tree, this, split.getLeftChild());
    rightChild = new ClassNode(tree, this, split.getRightChild());
  }
  
  public ClassTree getTree() {
    return tree;
  }
  
  public ClassNode getParent() {
    return parent;
  }
  
  public ClassNode getLeftChild() {
    return leftChild;
  }
  
  public ClassNode getRightChild() {
    return rightChild;
  }
  
  public Split getSplit() {
    return split;
  }
  
  private void ensureStatisticsUpdated() {
    if (histogram == null) {
      ProjectionStatistics projectionStatistics = parent.getProjectionStatisticsLookasideBuffer();
      boolean compute;
      if (projectionStatistics == null)
        compute = true;
      else {
        int i = 0;
        double[] axis = split.getAxis();
        while (i < axis.length && axis[i] == projectionStatistics.getAxisElement(i))
          i++;
        compute = i < axis.length;
      }
      if (compute)
        projectionStatistics = new ProjectionStatistics(parent, split.getAxis());
      histogram = projectionStatistics.getHistogram();
      bestThreshold = projectionStatistics.getBestThreshold();
    }
  }
  
  public Histogram getHistogram() {
    ensureStatisticsUpdated();
    return histogram;
  }
  
  public double getBestThreshold() {
    ensureStatisticsUpdated();
    return bestThreshold;
  }
  
  void newPoints() {
    histogram = null;
    leftChild.newPoints();
    rightChild.newPoints();
  }
  
  int addPointAndClassify(double[] point) {
    return split.classify(point) ? leftChild.addPointAndClassify(point) :
                                   rightChild.addPointAndClassify(point);
  }
  
  void addParentPointsToChildren() {
    SetOfPoints points = tree.getPoints();
    int ndims = points.getDimensionCount();
    double[] point = new double[ndims];
    byte[] classes = tree.getClasses();
    int serialNumber = parent.getNode().getSerialNumber();
    PointIterator iter = points.createIterator();
    for (int i = 0; i < classes.length; i++) {
      iter.next();
      int pointClass = classes[i] & 0xff;
      while (pointClass > serialNumber)
        pointClass >>>= 1;
      if (pointClass == serialNumber) {
        for (int j = 0; j < ndims; j++)
          point[j] = iter.getCoordinate(j);
        classes[i] = (byte) addPointAndClassify(point);
      }
    }
  }

  private void splitChanged() {
    leftChild.newPoints();
    rightChild.newPoints();
    addParentPointsToChildren();
    setChanged();
    notifyObservers();
    notifySubtreeNodeObservers("New Points");
  }
  
  void notifySubtreeNodeObservers(Object info) {
    leftChild.notifySubtreeNodeObservers(info);
    rightChild.notifySubtreeNodeObservers(info);
  }
}
