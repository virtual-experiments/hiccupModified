package interactivehicupp;

import java.awt.*;

public interface PointsSourceClient {
  PopupMenu createNodePopupMenu(NodeView selectedNode);
  void layoutTree();
  Frame getFrame();
  TextArea getLogTextArea();
}
