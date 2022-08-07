package hicupp.trees;

import java.util.Observable;

public final class Split extends Observable {
  private final Tree tree;
  private final Node parent;
  private final Node leftChild;
  private final Node rightChild;
  private final double[] axis;
  private double threshold;

  private int splitProjectionIndex;
  private int optimisationAlgorithmIndex;
  private int splitIterations;

  Split(Tree tree, Node parent, double[] axis, double threshold) {
    this.tree = tree;
    this.parent = parent;
    this.axis = axis;
    this.threshold = threshold;
    int leftChildSerialNumber = 2 * parent.getSerialNumber();
    leftChild = new Node(tree, this, leftChildSerialNumber);
    rightChild = new Node(tree, this, leftChildSerialNumber + 1);

    splitProjectionIndex = -1;
    optimisationAlgorithmIndex = -1;
    splitIterations = 0;
  }
  
  public Node getParent() {
    return parent;
  }

  public Node getLeftChild() {
    return leftChild;
  }

  public Node getRightChild() {
    return rightChild;
  }

  public double[] getAxis() {
    return (double[]) axis.clone();
  }
  
  public double getThreshold() {
	  return threshold;
  }
  
  public void setThreshold(double value) {
    threshold = value;
    setChanged();
    notifyObservers();
    tree.changed();
  }
  
  public double evaluate(double[] coords, int index) {
    double value = 0.0;
    int ndims = axis.length;
    for (int i = 0; i < ndims; i++)
      value += coords[index++] * axis[i];
    return value;
  }

  public double evaluate(double[] point) {
    return evaluate(point, 0);
  }
  
  public boolean classify(double[] coords, int index) {
    return evaluate(coords, index) < threshold;
  }
  
  public boolean classify(double[] point) {
    return classify(point, 0);
  }
  
  public boolean isLeafSplit() {
    return leftChild.getChild() == null && rightChild.getChild() == null;
  }
  
  public int computeClass(double[] point, int index) {
    return (classify(point, index) ?
            leftChild :
            rightChild).computeClass(point, index);
  }

  public int getSplitProjectionIndex() {
    return splitProjectionIndex;
  }

  public void setSplitProjectionIndex(int splitProjectionIndex) {
    this.splitProjectionIndex = splitProjectionIndex;
  }

  public int getSplitIterations() {
    return splitIterations;
  }

  public void setSplitIterations(int splitIterations) {
    this.splitIterations = splitIterations;
  }

  public int getOptimisationAlgorithmIndex() {
    return optimisationAlgorithmIndex;
  }

  public void setOptimisationAlgorithmIndex(int optimisationAlgorithmIndex) {
    this.optimisationAlgorithmIndex = optimisationAlgorithmIndex;
  }
}