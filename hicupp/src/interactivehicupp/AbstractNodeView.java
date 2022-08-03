package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.Observable;
import java.util.Observer;

import hicupp.*;
import hicupp.classify.*;
import hicupp.trees.*;

abstract class AbstractNodeView implements NodeView {
  private final TreeDocument client;
  private final ClassNode classNode;
  private final SplitView parent;
  private SplitView child;
  private Window infoFrame;
  private TextArea infoTextArea;

  private String splitProjectionIndex;
  private int splitNoOfIterations;
  
  AbstractNodeView(PointsSourceClient client, SplitView parent, ClassNode classNode) {
    this.client = (TreeDocument) client;
    this.parent = parent;
    this.classNode = classNode;
    this.splitProjectionIndex = "N/A";
    this.splitNoOfIterations = 0;
    
    classNode.addObserver((observable, info) -> {
      if (info == "Split") {
        child = createChild();
      } else if (info == "Prune") {
        this.splitProjectionIndex = "N/A";
        this.splitNoOfIterations = 0;
        child = null;
      } else if (info == "New Points") {
        newPoints();
      } else
        throw new RuntimeException("Unexpected info object: " + info);
    });
  }
  
  void initChild() {
    if (classNode.getChild() != null)
      child = createChild();
  }
  
  abstract SplitView createChild();
  
  public ClassNode getClassNode() {
    return classNode;
  }
  
  void initComponent() {
    getComponent().addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        Dimension size = getComponent().getSize();
        int x = e.getX();
        int y = e.getY();
        boolean inComponent = 0 <= x &&
                              x <= size.width &&
                              0 <= y &&
                              y <= size.height;
        if (inComponent && (e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
          PopupMenu popupMenu = client.createNodePopupMenu(AbstractNodeView.this);
          addNodePopupMenuItems(popupMenu);
          popupMenu.show(getComponent(), e.getX(), e.getY());
        }
      }
    });
  }    

  protected void addNodePopupMenuItems(PopupMenu popupMenu) {
  }
  
  public SplitView getParentSplitView() {
    return parent;
  }
  
  public SplitView getChild() {
    return child;
  }
  
  public void split() throws NoConvergenceException, CancellationException {
    
    final MonitorDialog monitorDialog = new MonitorDialog(client.getFrame());
    
    class Computation implements Runnable {
      public volatile double[] axis;
      public volatile Exception exception;
      public void run() {
        try {
          axis = Clusterer.findAxis(client.getProjectionIndex(),
                                    classNode,
                                    monitorDialog);
        } catch (Exception e) {
          exception = e;
        }
      }
    }
    
    Computation computation = new Computation();

    splitProjectionIndex = ProjectionIndexFunction.getProjectionIndexNames()[client.getProjectionIndex()];
    client.getLogTextArea().append("Splitting node " + getClassNode().getNode().getSerialNumber() +
                                   " using projection index " + splitProjectionIndex + "...\n");
    monitorDialog.show(computation, client.getLogTextArea());

    if (computation.exception != null) {
      if (computation.exception instanceof NoConvergenceException)
        throw (NoConvergenceException) computation.exception;
      if (computation.exception instanceof CancellationException)
        throw (CancellationException) computation.exception;
      throw (RuntimeException) computation.exception;
    }

    double[] axis = computation.axis;
    classNode.split(axis);

    splitNoOfIterations = monitorDialog.getIterationCount();
    client.getLogTextArea().append("Node " + getClassNode().getNode().getSerialNumber() +
            " split using projection index " + splitProjectionIndex + " with " +
            splitNoOfIterations + " iterations.");
  }
  
  public void newPoints() {
    if (infoTextArea != null)
      updateInfo();
  }
  
  public void splitValueChanged() {
    if (infoTextArea != null)
      updateInfo();
  }
  
  public void childSplitValueChanged() {
  }
  
  public void hideInfo() {
    infoFrame.dispose();
    infoFrame = null;
    infoTextArea = null;
  }
  
  public void showInfo() {
    if (infoFrame == null) {
      infoFrame = new Window(client.getFrame());
      infoTextArea = new TextArea(classNode.getDimensionCount() + 1, 23);
      infoTextArea.setFont(new Font("Monospaced", 0, 10));
      updateInfo();
      infoTextArea.setEditable(false);
      infoFrame.add(infoTextArea);
      infoFrame.pack();
      Point location = getComponent().getLocationOnScreen();
      Dimension nodeSize = getComponent().getSize();
      Dimension size = infoFrame.getSize();
      infoFrame.setLocation(location.x + (nodeSize.width - size.width) / 2,
                            location.y + nodeSize.height);
    }
    infoFrame.show();
  }
  
  private static void appendChars(StringBuffer b, char c, int n) {
    while (n-- > 0)
      b.append(c);
  }
  
  private void updateInfo() {
    String[] paramNames = client.getPointsSourceProvider().getParameterNames();
    StringBuffer info = new StringBuffer("   Mean      Stddev\n");
    int ndims = classNode.getDimensionCount();
    for (int j = 0; j < ndims; j++) {
      if (j > 0)
        info.append('\n');
      info.append(paramNames[j]);
      appendChars(info, ' ', 3 - paramNames[j].length());
      info.append(TextTools.formatScientific(classNode.getMean(j)));
      info.append(' ');
      info.append(TextTools.formatScientific(classNode.getStandardDeviation(j)));
    }

    if (!this.splitProjectionIndex.equals("N/A")) {
      info.append("\n\n   Split info\n");
      info.append("Projection index: ").append(splitProjectionIndex).append("\n");
      info.append("Number of iterations: ").append(splitNoOfIterations);
    }
    
    infoTextArea.setText(info.toString());
  }
  
  public boolean infoIsShowing() {
    return infoFrame != null;
  }
}
