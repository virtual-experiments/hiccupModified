package interactivehicupp;

import javax.swing.*;

public interface PointsSourceClient {
  JPopupMenu createNodePopupMenu(NodeView selectedNode);
  void layoutTree();
  JFrame getFrame();
  JTextArea getLogTextArea();
  float getHistogramZoom();
}
