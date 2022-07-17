package hicupp.classify;

import hicupp.*;
import hicupp.trees.*;

import java.util.*;

public final class ClassNode extends Observable implements SetOfPoints {
  private final ClassTree tree;
  private final ClassSplit parent;
  private ClassSplit child;
  private final Node node;
  private int pointCount;
  private double[] sums;
  private double[] sumsOfSquares;
  private ProjectionStatistics projectionStatisticsLookasideBuffer;
  
  private class NodeObserver implements Observer {
    public void update(Observable o, Object typeOfChange) {
      nodeChanged(typeOfChange);
    }
  }

  private Observer nodeObserver = new NodeObserver();

  ClassNode(ClassTree tree, ClassSplit parent, Node node) {
    this.tree = tree;
    this.parent = parent;
    this.node = node;

    node.addObserver(nodeObserver);
    
    Split split = node.getChild();
    if (split != null)
      child = new ClassSplit(tree, this, split);
  }
  
  public ClassTree getTree() {
    return tree;
  }
  
  public ClassSplit getParent() {
    return parent;
  }
  
  public ClassSplit getChild() {
    return child;
  }
  
  public Node getNode() {
    return node;
  }
  
  public boolean containsPointAtIndex(int index) {
    int serialNumber = node.getSerialNumber();
    int pointClass = tree.getClasses()[index];
    while (pointClass > serialNumber)
      pointClass >>>= 1;
    return pointClass == serialNumber;
  }
  
  public int getDimensionCount() {
    return tree.getPoints().getDimensionCount();
  }
  
  public int getPointCount() {
    return pointCount;
  }
  
  private class NodePointIterator implements PointIterator {
    private PointIterator iter = tree.getPoints().createIterator();
    private int iterIndex = -1;
    private int nextIndex = -1;

    {
      step();
    }
    
    public boolean hasNext() {
      return nextIndex < tree.getClasses().length;
    }
    
    private void step() {
      nextIndex++;
      int serialNumber = node.getSerialNumber();
      byte[] classes = tree.getClasses();
      while (nextIndex < classes.length) {
        int pointClass = classes[nextIndex] & 0xff;
        while (pointClass > serialNumber)
          pointClass >>>= 1;
        if (pointClass == serialNumber)
          break;
        nextIndex++;
      }
    }
    
    public void next() {
      while (iterIndex < nextIndex) {
        iter.next();
        iterIndex++;
      }
      step();
    }
    
    public double getCoordinate(int index) {
      return iter.getCoordinate(index);
    }
  }

  public hicupp.PointIterator createIterator() {
    return new NodePointIterator();
  }
  
  public double getMean(int index) {
    return sums[index] / pointCount;
  }
  
  public double getStandardDeviation(int index) {
    double mean = getMean(index);
    return Math.sqrt(sumsOfSquares[index] / pointCount - mean * mean);
  }

  public ProjectionStatistics getProjectionStatisticsLookasideBuffer() {
    return projectionStatisticsLookasideBuffer;
  }
  
  public ProjectionStatistics computeProjectionStatistics(double[] axis) {
    projectionStatisticsLookasideBuffer = new ProjectionStatistics(this, axis);
    return projectionStatisticsLookasideBuffer;
  }
  
  public void split(double[] axis) {
    double threshold = computeProjectionStatistics(axis).getBestThreshold();
    node.split(axis, threshold);
  }
  
  void newPoints() {
    pointCount = 0;
    projectionStatisticsLookasideBuffer = null;
    int ndims = tree.getPoints().getDimensionCount();
    sums = new double[ndims];
    sumsOfSquares = new double[ndims];
    
    if (child != null)
      child.newPoints();
  }
  
  int addPointAndClassify(double[] point) {
    pointCount++;
    for (int j = 0; j < point.length; j++) {
      sums[j] += point[j];
      sumsOfSquares[j] += point[j] * point[j];
    }
    
    if (child == null)
      return node.getSerialNumber();
    else
      return child.addPointAndClassify(point);
  }

  private void nodeChanged(Object typeOfChange) {
    if (typeOfChange == "Split") {
      child = new ClassSplit(tree, this, node.getChild());
      child.newPoints();
      child.addParentPointsToChildren();
    } else if (typeOfChange == "Prune")
      child = null;
    else
      throw new RuntimeException("Unknown type of change.");
    setChanged();
    notifyObservers(typeOfChange);
  }
  
  void notifySubtreeNodeObservers(Object info) {
    setChanged();
    notifyObservers(info);
    if (child != null)
      child.notifySubtreeNodeObservers(info);
  }
}
