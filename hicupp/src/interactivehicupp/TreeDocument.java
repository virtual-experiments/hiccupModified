package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import hicupp.*;
import hicupp.algorithms.AlgorithmParameters;
import hicupp.trees.*;

public class TreeDocument extends Panel implements Document, PointsSourceClient {

  private final PointsSourceProvider pointsSourceProvider;

  private NodeView displayRoot;
  private int projectionIndex = ProjectionIndexFunction.FRIEDMANS_PROJECTION_INDEX;
  private int algorithmIndex = FunctionMaximizer.SIMPLEX_ALGORITHM_INDEX;
  private AlgorithmParameters algorithmParameters;

  private DocumentChangeListener changeListener;

  private final Menu toolsMenu = new Menu();
  private final Menu goMenu = new Menu();
  private final MenuItem goToRootMenuItem = new MenuItem();
  private final MenuItem goToParentMenuItem = new MenuItem();
  private final MenuItem goToLeftChildMenuItem = new MenuItem();
  private final MenuItem goToRightChildMenuItem = new MenuItem();
  private final Frame logFrame = new Frame();
  private final TextArea logTextArea = new TextArea();
  private final PopupMenu nodePopupMenu = new PopupMenu();
  private final RadioMenuTools projectionIndexMenu;
  private final RadioMenuTools optimisationAlgorithmMenu;

  private static Frame getFrameAncestor(Component c) {
    while (!(c instanceof Frame))
      if (c == null) return null;
      else c = c.getParent();
    return (Frame) c;
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

  public Frame getFrame() {
    return getFrameAncestor(this);
  }

  @Override
  public TextArea getLogTextArea() {
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

    MenuItem configureAlgorithmMenu = new MenuItem();
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

    projectionIndexMenu.setLabel("Projection Index");
    optimisationAlgorithmMenu.setLabel("Optimization Algorithm");
    configureAlgorithmMenu.setLabel("Configure Optimization Algorithm");

    MenuItem redrawTreeMenu = new MenuItem();
    redrawTreeMenu.setLabel("Redraw tree");
    redrawTreeMenu.addActionListener(e -> redraw());

    Menu resizeHistogram;
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

    resizeHistogram.setLabel("Resize histogram");

    toolsMenu.setLabel("Tools");
    toolsMenu.add(projectionIndexMenu);
    toolsMenu.add(optimisationAlgorithmMenu);
    toolsMenu.add(configureAlgorithmMenu);
    toolsMenu.addSeparator();
    toolsMenu.add(redrawTreeMenu);
    toolsMenu.add(resizeHistogram);

    goMenu.setLabel("Go");
    goMenu.add(goToRootMenuItem);
    goMenu.add(goToParentMenuItem);
    goMenu.add(goToLeftChildMenuItem);
    goMenu.add(goToRightChildMenuItem);

    goToRootMenuItem.setLabel("Go To Root");
    goToRootMenuItem.addActionListener(e -> goTo(pointsSourceProvider.getRoot()));
    goToParentMenuItem.setLabel("Go To Parent");
    goToParentMenuItem.addActionListener(e -> goTo(displayRoot.getParentSplitView().getParentNodeView()));
    goToLeftChildMenuItem.setLabel("Go To Left Child");
    goToLeftChildMenuItem.addActionListener(e -> goTo(displayRoot.getChild().getLeftChild()));
    goToRightChildMenuItem.setLabel("Go To Right Child");
    goToRightChildMenuItem.addActionListener(e -> goTo(displayRoot.getChild().getRightChild()));

    logFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        logFrame.setVisible(false);
      }
    });

    {
      logFrame.add(logTextArea, BorderLayout.CENTER);
      logTextArea.setEditable(false);
      logFrame.setTitle("Log Window - Interactive Hicupp");
      MenuBar menuBar = new MenuBar();
      menuBar.setFont(DocumentFrame.menuFont);
      Menu fileMenu = new Menu("File");
      MenuItem save = new MenuItem("Save...");
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
      Menu menu = new Menu("Edit");
      MenuItem clear = new MenuItem("Clear");
      menuBar.add(fileMenu);
      menuBar.add(menu);
      menu.add(clear);
      clear.addActionListener(e -> logTextArea.setText(""));
      logFrame.setMenuBar(menuBar);
      logFrame.pack();
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
  public PopupMenu createNodePopupMenu(final NodeView selectedNode) {
    nodePopupMenu.removeAll();

    final MenuItem splitMenuItem = new MenuItem();
    final MenuItem pruneMenuItem = new MenuItem();
    final MenuItem goToNodeMenuItem = new MenuItem();
    final MenuItem showInfoMenuItem = new MenuItem();

    boolean split = selectedNode.getChild() == null;

    splitMenuItem.setLabel("Split");
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
    pruneMenuItem.setLabel("Prune");
    pruneMenuItem.setEnabled(!split);
    pruneMenuItem.addActionListener(e -> {
      selectedNode.getClassNode().getNode().prune();
      rebuildComponentStructure();
      layoutTree();
      updateGoMenu();
      repaint();
    });
    goToNodeMenuItem.setLabel("Go To Node");
    goToNodeMenuItem.addActionListener(e -> goTo(selectedNode));
    showInfoMenuItem.setLabel(selectedNode.infoIsShowing() ? "Hide Info" : "Show Info");
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

  public void layoutTree() {
    Dimension size = getSize();
    int top = displayRoot == pointsSourceProvider.getRoot() ? 0 : 10;
    SplitView.layoutSubtree(displayRoot, 0, top, size.width);
  }

  public void paint(Graphics g) {
    if (displayRoot != pointsSourceProvider.getRoot()) {
      Rectangle bounds = displayRoot.getComponent().getBounds();
      g.setColor(Color.black);
      int center = bounds.x + bounds.width / 2;
      g.drawLine(center, 0, center, bounds.y);
    }
    SplitView.paintSubtree(displayRoot, g);
    super.paint(g);
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
  public void addMenuBarItems(MenuBar menuBar) {
    pointsSourceProvider.addMenuBarItems(menuBar);
    menuBar.add(goMenu);
    menuBar.add(toolsMenu);
  }

  @Override
  public void addChangeListener(DocumentChangeListener listener) {
    changeListener = listener;
  }

  @Override
  public Component getComponent() {
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