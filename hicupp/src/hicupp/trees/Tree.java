package hicupp.trees;

import java.util.Observable;

public class Tree extends Observable {
  private final Node root = new Node(this, null, 1);
  private int ndims = 2;

  public void setNdims(int ndims) {
    this.ndims = ndims;
  }

  public int getNdims() {
    return ndims;
  }

  public Node getRoot() {
    return root;
  }
  
  void changed() {
    setChanged();
    notifyObservers();
  }
}
