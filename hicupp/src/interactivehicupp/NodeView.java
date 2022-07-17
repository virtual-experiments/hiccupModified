package interactivehicupp;

import java.awt.*;
import java.awt.event.*;

import hicupp.*;
import hicupp.classify.*;
import hicupp.trees.*;

public interface NodeView {
  Component getComponent();
  ClassNode getClassNode();
  SplitView getParentSplitView();
  SplitView getChild();
  void split() throws NoConvergenceException, CancellationException;
  void newPoints();
  void showInfo();
  void hideInfo();
  boolean infoIsShowing();
}
