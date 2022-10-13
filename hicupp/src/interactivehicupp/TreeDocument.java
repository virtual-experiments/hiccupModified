package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import hicupp.*;
import hicupp.algorithms.AlgorithmParameters;
import hicupp.trees.*;

import javax.swing.*;

public class TreeDocument extends JPanel implements Document, PointsSourceClient {

  private final PointsSourceProvider pointsSourceProvider;

  private NodeView displayRoot;
  private int projectionIndex = ProjectionIndexFunction.FRIEDMANS_PROJECTION_INDEX;
  private int algorithmIndex = FunctionMaximizer.SIMPLEX_ALGORITHM_INDEX;
  private AlgorithmParameters algorithmParameters;

  private DocumentChangeListener changeListener;

  private final JMenu toolsMenu = new JMenu();
  private final JMenu goMenu = new JMenu();
  private final JMenuItem goToRootMenuItem = new JMenuItem();
  private final JMenuItem goToParentMenuItem = new JMenuItem();
  private final JMenuItem goToLeftChildMenuItem = new JMenuItem();
  private final JMenuItem goToRightChildMenuItem = new JMenuItem();
  private final JFrame logFrame = new JFrame();
  private final JTextArea logTextArea = new JTextArea();
  private final JPopupMenu nodePopupMenu = new JPopupMenu();
  private final RadioMenuTools projectionIndexMenu;
  private final RadioMenuTools optimisationAlgorithmMenu;

  private static JFrame getFrameAncestor(Component c) {
    while (!(c instanceof JFrame))
      if (c == null) return null;
      else c = c.getParent();
    return (JFrame) c;
  }

  int getProjectionIndex() {
    return projectionIndex;
  }

  int getAlgorithmIndex() {
    return algorithmIndex;
  }

  public AlgorithmParameters getAlgorithmParameters() {
    return algorithmParameters;
  }

  public void setAlgorithmParameters(AlgorithmParameters parameters) {
    algorithmParameters = parameters;
  }

  public JFrame getFrame() {
    return getFrameAncestor(this);
  }

  @Override
  public JTextArea getLogTextArea() {
    return logTextArea;
  }

  public TreeDocument(PointsSourceType pointsSourceType, String filename)
          throws IOException {
    this(pointsSourceType, TreeFileFormat.loadTree(filename));
  }

  public TreeDocument(PointsSourceType pointsSourceType) {
    this(pointsSourceType, new Tree());
  }

  private TreeDocument(PointsSourceType pointsSourceType, Tree tree) {
    this.pointsSourceProvider = pointsSourceType.createPointsSourceProvider(this, tree);

    nodePopupMenu.setFont(DocumentFrame.menuFont);

    JMenuItem configureAlgorithmMenu = new JMenuItem();
    {
      RadioMenuTools.RadioMenuEventListener projectionIndexListener = this::changeProjection;
      String[] projectionLabels = ProjectionIndexFunction.getProjectionIndexNames();
      projectionIndexMenu = RadioMenuTools.createRadioMenu(
              projectionLabels,
              projectionIndex,
              projectionIndexListener);
    }

    {
      RadioMenuTools.RadioMenuEventListener algorithmIndexListener = this::changeAlgorithm;

      String[] optimisationLabel = FunctionMaximizer.getAlgorithmNames();
      optimisationAlgorithmMenu = RadioMenuTools.createRadioMenu(
              optimisationLabel,
              algorithmIndex,
              algorithmIndexListener);

      configureAlgorithmMenu.addActionListener(e -> {
        if (algorithmIndex == FunctionMaximizer.SIMPLEX_ALGORITHM_INDEX)
          MessageBox.showMessage(getFrame(), "No configuration for the simplex algorithm.",
                  "Interactive Hicupp");
        else
          changeAlgorithm(algorithmIndex);
      });
    }

    projectionIndexMenu.setText("Projection Index");
    optimisationAlgorithmMenu.setText("Optimization Algorithm");
    configureAlgorithmMenu.setText("Configure Optimization Algorithm");

    JMenuItem redrawTreeMenu = new JMenuItem();
    redrawTreeMenu.setText("Redraw tree");
    redrawTreeMenu.addActionListener(e -> redraw());

    JMenu resizeHistogram;
    {
      RadioMenuTools.RadioMenuEventListener listener = index -> {
        histogramZoomIndex = index;
        changeHistogramSize();
      };

      resizeHistogram = RadioMenuTools.createRadioMenu(
              histogramZoomFactors,
              histogramZoomDefaultIndex,
              listener);
    }

    resizeHistogram.setText("Resize histogram");

    toolsMenu.setText("Tools");
    toolsMenu.add(projectionIndexMenu);
    toolsMenu.add(optimisationAlgorithmMenu);
    toolsMenu.add(configureAlgorithmMenu);
    toolsMenu.addSeparator();
    toolsMenu.add(redrawTreeMenu);
    toolsMenu.add(resizeHistogram);

    goMenu.setText("Go");
    goMenu.add(goToRootMenuItem);
    goMenu.add(goToParentMenuItem);
    goMenu.add(goToLeftChildMenuItem);
    goMenu.add(goToRightChildMenuItem);

    goToRootMenuItem.setText("Go To Root");
    goToRootMenuItem.addActionListener(e -> goTo(pointsSourceProvider.getRoot()));
    goToParentMenuItem.setText("Go To Parent");
    goToParentMenuItem.addActionListener(e -> goTo(displayRoot.getParentSplitView().getParentNodeView()));
    goToLeftChildMenuItem.setText("Go To Left Child");
    goToLeftChildMenuItem.addActionListener(e -> goTo(displayRoot.getChild().getLeftChild()));
    goToRightChildMenuItem.setText("Go To Right Child");
    goToRightChildMenuItem.addActionListener(e -> goTo(displayRoot.getChild().getRightChild()));

    logFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        logFrame.setVisible(false);
      }
    });

    {
      JScrollPane scrollPane = new JScrollPane(logTextArea,
              JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
              JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      logFrame.add(scrollPane);
      logTextArea.setEditable(false);
      logFrame.setTitle("Log Window - Interactive Hicupp");
      JMenuBar menuBar = new JMenuBar();
      menuBar.setFont(DocumentFrame.menuFont);
      JMenu fileMenu = new JMenu("File");
      JMenuItem save = new JMenuItem("Save...");
      save.addActionListener(e -> {
        FileDialog fileDialog = new FileDialog(getFrame(), "Save Log As", FileDialog.SAVE);
        fileDialog.setVisible(true);
        if (fileDialog.getFile() != null) {
          try {
            String filename = fileDialog.getFile();
            if (!filename.endsWith(".txt")) filename += ".txt";
            Writer writer = new FileWriter(new File(fileDialog.getDirectory(), filename));
            writer.write(logTextArea.getText());
            writer.close();
          } catch (IOException ex) {
            MessageBox.showMessage(getFrame(), "Could not save the log: " + ex, "Interactive Hicupp");
          }
        }
      });
      fileMenu.add(save);
      JMenu menu = new JMenu("Edit");
      JMenuItem clear = new JMenuItem("Clear");
      menuBar.add(fileMenu);
      menuBar.add(menu);
      menu.add(clear);
      clear.addActionListener(e -> logTextArea.setText(""));
      logFrame.setJMenuBar(menuBar);
      logFrame.setSize(400, 400);
      logFrame.setVisible(true);
    }

    setBackground(Color.white);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        redraw();
      }
    });

    tree.addObserver((observable, object) -> {
      if (changeListener != null)
        changeListener.documentChanged();
    });

    goTo(pointsSourceProvider.getRoot());

    // ask if user want to load
    if (TreeFileFormat.inputFileExists) {
      int result = MessageBox.showMessage(null, "Do you want to load input file from tree?", "Open input file", new String[] {"Yes", "No"} );

      if (result == 0) { // load
        this.pointsSourceProvider.loadFile(TreeFileFormat.filename.toString());
        getLogTextArea().append("Loaded file with input file " + TreeFileFormat.filename + " with type " + TreeFileFormat.fileExtension
                + " and size " + TreeFileFormat.fileSize + "kB.\n");
      } else
        getLogTextArea().append("Loaded tree without input file.\n");
    } else if (this.pointsSourceProvider instanceof ImagePointsSourceProvider imagePointsSourceProvider)
      imagePointsSourceProvider.loadDefaultImage();

    Split rootSplit = tree.getRoot().getChild();
    if (rootSplit != null) {
      projectionIndex = rootSplit.getSplitProjectionIndex();
      projectionIndexMenu.setChosenItem(projectionIndex);

      algorithmIndex = rootSplit.getOptimisationAlgorithmIndex();
      optimisationAlgorithmMenu.setChosenItem(algorithmIndex);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(1600, 900);
  }

  @Override
  public JPopupMenu createNodePopupMenu(final NodeView selectedNode) {
    nodePopupMenu.removeAll();

    final JMenuItem splitMenuItem = new JMenuItem();
    final JMenuItem pruneMenuItem = new JMenuItem();
    final JMenuItem goToNodeMenuItem = new JMenuItem();
    final JMenuItem showInfoMenuItem = new JMenuItem();

    boolean split = selectedNode.getChild() == null;

    splitMenuItem.setText("Split");
    splitMenuItem.setEnabled(split);
    splitMenuItem.addActionListener(e -> {
      try {
        selectedNode.split();
        rebuildComponentStructure();
        layoutTree();
        updateGoMenu();
        repaint();
      } catch (NoConvergenceException ex) {
        MessageBox.showMessage(getFrameAncestor(TreeDocument.this), "Could not split the node: " + ex,
                "Interactive Hicupp");
      } catch (CancellationException ignored) { }
    });
    pruneMenuItem.setText("Prune");
    pruneMenuItem.setEnabled(!split);
    pruneMenuItem.addActionListener(e -> {
      selectedNode.getClassNode().getNode().prune();
      rebuildComponentStructure();
      layoutTree();
      updateGoMenu();
      repaint();
    });
    goToNodeMenuItem.setText("Go To Node");
    goToNodeMenuItem.addActionListener(e -> goTo(selectedNode));
    showInfoMenuItem.setText(selectedNode.infoIsShowing() ? "Hide Info" : "Show Info");
    showInfoMenuItem.addActionListener(e -> {
      if (selectedNode.infoIsShowing())
        selectedNode.hideInfo();
      else
        selectedNode.showInfo();
    });

    nodePopupMenu.add(splitMenuItem);
    nodePopupMenu.add(pruneMenuItem);
    nodePopupMenu.add(goToNodeMenuItem);
    nodePopupMenu.add(showInfoMenuItem);

    return nodePopupMenu;
  }

  private void rebuildComponentStructure() {
    removeAll();

    add(nodePopupMenu);
    SplitView.addSubtreeToContainer(displayRoot, this);
  }

  @Override
  public void layoutTree() {
    Dimension size = getSize();
    int top = displayRoot == pointsSourceProvider.getRoot() ? 0 : 10;
    SplitView.layoutSubtree(displayRoot, 0, top, size.width);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (displayRoot != pointsSourceProvider.getRoot()) {
      Rectangle bounds = displayRoot.getComponent().getBounds();
      g.setColor(Color.black);
      int center = bounds.x + bounds.width / 2;
      g.drawLine(center, 0, center, bounds.y);
    }
    layoutTree();
    SplitView.paintSubtree(displayRoot, g);
  }

  private void goTo(NodeView nodeView) {
    DocumentFrame.hideAllInfo(displayRoot);
    displayRoot = nodeView;
    rebuildComponentStructure();
    layoutTree();
    goToRootMenuItem.setEnabled(nodeView != pointsSourceProvider.getRoot());
    goToParentMenuItem.setEnabled(nodeView != pointsSourceProvider.getRoot());
    updateGoMenu();
    repaint();
  }

  private void updateGoMenu() {
    goToLeftChildMenuItem.setEnabled(displayRoot.getChild() != null);
    goToRightChildMenuItem.setEnabled(displayRoot.getChild() != null);
  }

  @Override
  public void addMenuBarItems(JMenuBar menuBar) {
    pointsSourceProvider.addMenuBarItems(menuBar);
    menuBar.add(goMenu);
    menuBar.add(toolsMenu);
  }

  @Override
  public void addChangeListener(DocumentChangeListener listener) {
    changeListener = listener;
  }

  @Override
  public Container getContainer() {
    return this;
  }

  @Override
  public void save(String filename) throws IOException {
    TreeFileFormat.saveTree(pointsSourceProvider, filename);
  }

  @Override
  public NodeView getRoot() {
    return displayRoot;
  }

  private void redraw() {
    layoutTree();
    repaint();
  }

  @Override
  public void exportCSV(String title) {
    NodeView root = pointsSourceProvider.getRoot();

    if (root.getChild() != null)
      ExportAsCSV.export(getFrame(), title, root.getChild());
    else
      MessageBox.showMessage(getFrame(), "Tree is empty.", "Export as CSV");
  }

  PointsSourceProvider getPointsSourceProvider() {
    return pointsSourceProvider;
  }

  private void changeAlgorithm(int index) {
    if (pointsSourceProvider.getRoot().getChild() != null) {
      int result = MessageBox.showMessage(getFrame(),
              "Tree already have child nodes, changing the algorithm is not recommended.",
              "Change algorithm",
              new String[]{"Change anyway", "Cancel"});

      if (result == 1) {  // cancel
        optimisationAlgorithmMenu.setChosenItem(algorithmIndex);
        return;
      }
    }

    AlgorithmParametersUI.Response response = new AlgorithmParametersUI.Response() {
      @Override
      public void confirm() {
        algorithmIndex = index;
      }

      @Override
      public void cancel() {
        optimisationAlgorithmMenu.setChosenItem(algorithmIndex);
      }
    };

    AlgorithmParametersUI.createParams(this, index, response);
  }

  private void changeProjection(int index) {
    if (pointsSourceProvider.getRoot().getChild() != null) {
      int result = MessageBox.showMessage(getFrame(),
              "Tree already have child nodes, changing the projection is not recommended.",
              "Change projection",
              new String[]{"Change anyway", "Cancel"});

      if (result == 1) {  // cancel
        projectionIndexMenu.setChosenItem(projectionIndex);
        return;
      }
    }

    projectionIndex = index;
    ((AbstractNodeView) pointsSourceProvider.getRoot()).setEvaluationTime();
  }

  public static final String[] histogramZoomFactors = new String[] { "0.25", "0.5", "0.75", "1.0", "1.5", "2.0" };
  public static final int histogramZoomDefaultIndex = 3;
  private int histogramZoomIndex = histogramZoomDefaultIndex;

  private void changeHistogramSize() {
    if (displayRoot.getChild() != null) {
      float factor = Float.parseFloat(histogramZoomFactors[histogramZoomIndex]);
      displayRoot.getChild().resizeHistogramView(factor);
      redraw();
    }
  }

  @Override
  public float getHistogramZoom() {
    return Float.parseFloat(histogramZoomFactors[histogramZoomIndex]);
  }
}