package interactivehicupp;

import java.awt.*;

public interface PointsSourceProvider {
  void addMenuBarItems(MenuBar menuBar);
  String[] getParameterNames();
  NodeView getRoot();
  String getSourceFile();
  String getMetadata();
  void loadFile(String filename);
}
