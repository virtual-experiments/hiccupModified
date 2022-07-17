package interactivehicupp;

import java.awt.*;

public interface PointsSourceProvider {
  void addMenuBarItems(MenuBar menuBar);
  String[] getParameterNames();
  NodeView getRoot();
}
