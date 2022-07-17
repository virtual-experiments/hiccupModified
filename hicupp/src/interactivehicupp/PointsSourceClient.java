package interactivehicupp;

import java.awt.Frame;
import java.awt.PopupMenu;
import hicupp.*;

public interface PointsSourceClient {
  PopupMenu createNodePopupMenu(NodeView selectedNode);
  void layoutTree();
  Frame getFrame();
}
