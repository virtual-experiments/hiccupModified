package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

import hicupp.*;
import hicupp.classify.*;
import hicupp.trees.*;

public class GeneralPointsSourceProvider implements PointsSourceProvider {
  private final Menu pointsMenu = new Menu();
  private final Menu viewMenu = new Menu();
  private final MenuItem pointsLoadPointsMenuItem = new MenuItem();
  private LoadMatrixDialog loadMatrixDialog;
  
  private final PointsSourceClient client;
  private final ClassTree classTree;
  private final GeneralNodeView root;
  
  private double[] coords;
  private int ndims;
  private SetOfPoints points;
  private String[] parameterNames;

  private String chosenFile = null;
  private String metadata = "N/A\nN/A";
  
  private class GeneralNodeView extends AbstractNodeView {
    private final Label component = new Label();
    private Inspector inspector = null;
    
    public GeneralNodeView(SplitView parent, ClassNode classNode) {
      super(GeneralPointsSourceProvider.this.client, parent, classNode);
      initChild();
      initComponent();
      newPoints();
    }
    
    SplitView createChild() {
      return new SplitView(GeneralNodeView::new, this, getClassNode().getChild(), parameterNames);
    }
    
    public Component getComponent() {
      return component;
    }
    
    public void newPoints() {
      super.newPoints();
      setEvaluationTime();
      component.setText(getClassNode().getPointCount() + " points");
    }

    @Override
    void addNodePopupMenuItems(PopupMenu popupMenu) {
      final MenuItem inspectMenuItem = new MenuItem("Inspect");
      inspectMenuItem.addActionListener(e -> inspect());
      popupMenu.add(inspectMenuItem);
    }

    private void inspect() {
      int pointCount = points.getPointCount();
      String[][] pointsString = new String[ndims][pointCount];
      NumberFormat format = new DecimalFormat("##0.00");

      for (int i = 0; i < ndims; i++) {
        for (int j = 0; j < pointCount; j++) {
          int index = i * pointCount + j;
          pointsString[i][j] = (getClassNode().containsPointAtIndex(j))?
                  format.format(coords[index]) : "N/A";
        }
      }

      if (inspector == null) {
        inspector = new Inspector(getClassNode().getNode().getSerialNumber());
        inspector.setTextArea(pointsString);
        inspector.setVisible(true);
      }
    }

    private class Inspector extends Frame {
      private final TextArea textArea;

      public Inspector(int nodeNumber) {
        super("Node " + nodeNumber);

        textArea = new TextArea();
        textArea.setEditable(false);
        add(textArea, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            setVisible(false);
            GeneralNodeView.this.inspector = null;
          }
        });
      }

      void setTextArea(String[][] pointsString) {
        StringBuilder builder = new StringBuilder();
        int columns = 0;
        int rows = 0;

        for (String[] row : pointsString) {
          StringBuilder newRow = new StringBuilder();
          for (String element : row) {
            newRow.append(element).append("\t");
          }
          columns = Math.max(columns, newRow.length());
          rows++;
          builder.append(newRow).append("\n");
        }

        textArea.setColumns(columns * 2);
        textArea.setRows(rows + 1);
        textArea.setText(builder.toString());
      }

      @Override
      public void setVisible(boolean b) {
        super.setVisible(b);

        if (b) {
          pack();
          Dimension frameSize = getSize();
          Dimension screenSize = getToolkit().getScreenSize();

          setLocation((screenSize.width - frameSize.width) / 2,
                  (screenSize.height - frameSize.height) / 2);
        }
      }
    }
  }

  private void generateDefaultMatrix() {
    coords = new double[] {
      0,  0,  1,  0,  0,  1,  1,  1,  2,  2,
      8,  8,  9,  9,  9,  10, 10, 9,  10, 10
    };
    ndims = 2;
    points = new ArraySetOfPoints(ndims, coords);
    generateDefaultParameterNames();
  }
  
  private void generateDefaultParameterNames() {
    parameterNames = new String[ndims];
    for (int i = 0; i < ndims; i++)
      parameterNames[i] = "p" + i;
  }
  
  private void loadPoints() {
    if (loadMatrixDialog == null)
      loadMatrixDialog = new LoadMatrixDialog(client.getFrame(),
                                              "Load Points from ASCII File");
    loadMatrixDialog.setVisible(true);
    double[] coords = loadMatrixDialog.getCoords();
    if (coords != null) {
      int ndims = loadMatrixDialog.getColumnsCount();
      if (classTree.getRoot().getNode().getChild() != null && ndims != this.ndims)
        MessageBox.showMessage(client.getFrame(),
                "Cannot load points: number of dimensions incompatible with split rules in tree.",
                "Interactive Hicupp");
      else {
        this.ndims = ndims;
        points = new ArraySetOfPoints(ndims, coords);
        generateDefaultParameterNames();
        classTree.setPoints(points);
      }
    }
  }
  
  public GeneralPointsSourceProvider(PointsSourceClient client, Tree tree) {
    this.client = client;
    
    pointsMenu.setLabel("Points");
    pointsMenu.setFont(new Font("Menu", Font.PLAIN, 14));
    pointsMenu.add(pointsLoadPointsMenuItem);
    
    pointsLoadPointsMenuItem.setLabel("Load Points From ASCII File...");
    pointsLoadPointsMenuItem.addActionListener(e -> loadPoints());

    generateDefaultMatrix();
    
    classTree = new ClassTree(tree, points);
    root = new GeneralNodeView(null, classTree.getRoot());
  }

  @Override
  public NodeView getRoot() {
    return root;
  }

  @Override
  public void addMenuBarItems(MenuBar menuBar) {
    menuBar.add(pointsMenu);
  }

  @Override
  public String getSourceFile() {
    return chosenFile;
  }

  @Override
  public String getMetadata() {
    return metadata;
  }

  @Override
  public void loadFile(String filename) {
    chosenFile = filename;
    setMetadata();
  }

  private void setMetadata() {
    try {
      Path path = Paths.get(chosenFile);

      long kilobytes = Files.size(path) / 1024;
      String type = Optional.of(chosenFile)
              .filter(f -> f.contains("."))
              .map(f -> f.substring(chosenFile.lastIndexOf(".") + 1))
              .orElse("N/A");

      metadata = kilobytes + "\n" + type;
    } catch (IOException | NullPointerException exception) {
      metadata = "N/A\nN/A";
      exception.printStackTrace();
    }
  }
  
  public String[] getParameterNames() {
    return parameterNames;
  }
}
