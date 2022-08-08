package interactivehicupp;

import java.awt.*;
import java.awt.event.*;

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

  private String splitProjection;
  private String optimisationAlgorithm;
  private int splitNoOfIterations;
  
  AbstractNodeView(PointsSourceClient client, SplitView parent, ClassNode classNode) {
    this.client = (TreeDocument) client;
    this.parent = parent;
    this.classNode = classNode;

    Split splitChild = classNode.getNode().getChild();

    if (splitChild != null) {
      int projectionIndex = splitChild.getSplitProjectionIndex();
      this.splitProjection = (projectionIndex == -1) ?
              "N/A" : ProjectionIndexFunction.getProjectionIndexNames()[projectionIndex];

      int optimisationIndex = splitChild.getOptimisationAlgorithmIndex();
      this.optimisationAlgorithm = (optimisationIndex == -1) ?
              "N/A" : FunctionMaximizer.getAlgorithmNames()[optimisationIndex];

      this.splitNoOfIterations = classNode.getNode().getChild().getSplitIterations();
    } else {
      this.splitProjection = "N/A";
      this.optimisationAlgorithm = "N/A";
      this.splitNoOfIterations = 0;
    }
    
    classNode.addObserver((observable, info) -> {
      if (info == "Split") {
        child = createChild();
      } else if (info == "Prune") {
        this.splitProjection = "N/A";
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
                                    client.getAlgorithmIndex(),
                                    classNode,
                                    monitorDialog);
        } catch (Exception e) {
          exception = e;
        }
      }
    }
    
    Computation computation = new Computation();

    splitProjection = ProjectionIndexFunction.getProjectionIndexNames()[client.getProjectionIndex()];
    optimisationAlgorithm = FunctionMaximizer.getAlgorithmNames()[client.getAlgorithmIndex()];

    client.getLogTextArea().append("Splitting node " + getClassNode().getNode().getSerialNumber() +
                                   " using projection index " + splitProjection +
                                   " with algorithm " + optimisationAlgorithm + "...\n");
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
            " split using projection index " + splitProjection + " with " +
            splitNoOfIterations + " iterations.\n");

    Split split = classNode.getNode().getChild();
    split.setSplitProjectionIndex(client.getProjectionIndex());
    split.setOptimisationAlgorithmIndex(client.getAlgorithmIndex());
    split.setSplitIterations(splitNoOfIterations);

    if (infoTextArea != null) updateInfo();
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
    if (infoFrame != null) {
      infoFrame.dispose();
      infoFrame = null;
      infoTextArea = null;
    }
  }
  
  public void showInfo() {
    if (infoFrame == null) {
      infoFrame = new Window(client.getFrame());
      infoTextArea = new TextArea(classNode.getDimensionCount() + 1, 23);
      infoTextArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
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

    if (!this.splitProjection.equals("N/A")) {
      infoTextArea.setRows(classNode.getDimensionCount() + 6);
      info.append("\n\n   Split info\n");
      info.append("Projection index: ").append(splitProjection).append("\n");
      StringBuilder algorithm = new StringBuilder();
      algorithm.append("Optimisation algorithm: ").append(optimisationAlgorithm);
      infoTextArea.setColumns(algorithm.length() + 1);
      info.append(algorithm).append("\n");
      info.append("Number of iterations: ").append(splitNoOfIterations);
    } else infoTextArea.setRows(classNode.getDimensionCount() + 1);
    
    infoTextArea.setText(info.toString());
  }
  
  public boolean infoIsShowing() {
    return infoFrame != null;
  }
}
