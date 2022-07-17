package hicupp.trees;

import java.util.Observable;

public class Tree extends Observable {
  private final Node root = new Node(this, null, 1);
  
  public Node getRoot() {
    return root;
  }
  
  void changed() {
    setChanged();
    notifyObservers();
  }
}
