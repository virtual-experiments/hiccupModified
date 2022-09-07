package interactivehicupp;

import java.awt.*;
import java.io.IOException;

public interface Document {
  void addMenuBarItems(MenuBar menuBar);
  void addChangeListener(DocumentChangeListener listener);
  Component getComponent();
  void save(String filename) throws IOException;
  NodeView getRoot();
  void redraw();
}
