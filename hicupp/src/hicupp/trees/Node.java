package hicupp.trees;

import java.util.Observable;

public final class Node extends Observable {
  private final Tree tree;
  private final Split parent;
  private Split child;
  private final int serialNumber;

  Node(Tree tree, Split parent, int serialNumber) {
    this.tree = tree;
    this.parent = parent;
    this.serialNumber = serialNumber;
  }
  
  public int getSerialNumber() {
    return serialNumber;
  }

  public Tree getTree() {
    return tree;
  }
  
  public Split getParent() {
    return parent;
  }

  public Split getChild() {
    return child;
  }

  public void split(double[] axis, double threshold) {
    child = new Split(tree, this, axis, threshold);
    setChanged();
    notifyObservers("Split");
    tree.changed();
  }
  
  public void prune() {
    child = null;
    setChanged();
    notifyObservers("Prune");
    tree.changed();
  }
  
  public boolean contains(double[] point) {
    return contains(point, 0);
  }
  
  /**
   * Returns <code>true</code> if this node contains the point whose
   * first coordinate is at index <code>index</code> in <code>point</code>;
   * returns <code>false</code> otherwise.
   */
  public boolean contains(double[] coords, int index) {
    boolean result;
    if (parent == null)
      result = true;
    else {
      boolean value = parent.classify(coords, index);
      result = value == (this == parent.getLeftChild()) &&
               parent.getParent().contains(coords, index);
    }
    return result;
  }
  
  public int computeClass(double[] coords, int index) {
    if (child == null)
      return serialNumber;
    else
      return child.computeClass(coords, index);
  }
}