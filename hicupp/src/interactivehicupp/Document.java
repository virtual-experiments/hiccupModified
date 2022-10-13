package interactivehicupp;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public interface Document {
  void addMenuBarItems(JMenuBar menuBar);
  void addChangeListener(DocumentChangeListener listener);
  Container getContainer();
  void save(String filename) throws IOException;
  NodeView getRoot();
  void exportCSV(String title);
}
