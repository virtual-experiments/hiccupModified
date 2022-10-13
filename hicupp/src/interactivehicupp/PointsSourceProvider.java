package interactivehicupp;

import javax.swing.*;

public interface PointsSourceProvider {
  void addMenuBarItems(JMenuBar menuBar);
  String[] getParameterNames();
  NodeView getRoot();
  String getSourceFile();
  String getMetadata();
  void loadFile(String filename);
}
