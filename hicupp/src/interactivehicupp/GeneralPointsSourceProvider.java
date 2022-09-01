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

  private int numberOfDigitsAfterDecimal = 2;
  private int numberOfTermsLimit = -1;
  
  private class GeneralNodeView extends AbstractNodeView {
    private final Label component = new Label();
    private Inspector inspector = null;
    
    public GeneralNodeView(SplitView parent, ClassNode classNode) {
      super(GeneralPointsSourceProvider.this.client, parent, classNode);
      initChild();
      initComponent();
      newPoints();
    }

    @Override
    SplitView createChild() {
      SplitView splitView = new SplitView(GeneralNodeView::new, this, getClassNode().getChild(), parameterNames);
      splitView.setEquationNumberFormat(numberOfDigitsAfterDecimal);
      splitView.setLimitNumberOfTerms(numberOfTermsLimit);

      return splitView;
    }
    
    public Component getComponent() {
      return component;
    }
    
    public void newPoints() {
      super.newPoints();
      setEvaluationTime();
      component.setText(getClassNode().getPointCount() + " points");

      if (inspector != null) inspector.setTextArea(matrixToString());
    }

    @Override
    void addNodePopupMenuItems(PopupMenu popupMenu) {
      final MenuItem inspectMenuItem = new MenuItem("Inspect");
      inspectMenuItem.addActionListener(e -> inspect());
      popupMenu.add(inspectMenuItem);
    }

    @Override
    public void split() throws NoConvergenceException, CancellationException {
      super.split();
    }

    private void inspect() {
      if (inspector == null) {
        inspector = new Inspector(getClassNode().getNode().getSerialNumber());
        inspector.setTextArea(matrixToString());
        inspector.setVisible(true);
      }
    }

    private String[][] matrixToString() {
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

      return pointsString;
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

    {
      pointsMenu.setLabel("Points");
      pointsMenu.setFont(new Font("Menu", Font.PLAIN, 14));

      MenuItem pointsLoadPointsMenuItem = new MenuItem("Load Points From ASCII File...");
      pointsLoadPointsMenuItem.addActionListener(e -> loadPoints());
      pointsMenu.add(pointsLoadPointsMenuItem);

      MenuItem pointsFromCSV = new MenuItem("Load Points From CSV...");
      pointsMenu.add(pointsFromCSV);
    }

    {
      viewMenu.setLabel("View");
      viewMenu.setFont(new Font("Menu", Font.PLAIN, 14));

      MenuItem numberOfDecimalPointsMenuItem = new MenuItem("Set number of digits after the decimal point");
      numberOfDecimalPointsMenuItem.addActionListener(e -> setNumberOfDecimalPoints());
      viewMenu.add(numberOfDecimalPointsMenuItem);

      MenuItem numberOfTermsMenuItem = new MenuItem("Set number of terms in decision tree");
      numberOfTermsMenuItem.addActionListener(e -> setNumberOfTermsLimit());
      viewMenu.add(numberOfTermsMenuItem);
    }

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
    menuBar.add(viewMenu);
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

  public void setNumberOfTermsLimit() {
    boolean limit = numberOfTermsLimit != -1;

    // elements
    final Dialog dialog = new Dialog(client.getFrame(), "Set Terms Limit");

    final Checkbox checkboxLimit = new Checkbox("Limit number of terms in decision tree", limit);

    final TextField fieldLimit = new TextField(Integer.toString((limit)? numberOfTermsLimit : 3));
    fieldLimit.setEnabled(limit);

    final Button buttonOk = new Button("Ok");
    final Button buttonCancel = new Button("Cancel");

    // organise
    final Panel panelButtons = new Panel(new FlowLayout(FlowLayout.RIGHT));
    panelButtons.add(buttonOk);
    panelButtons.add(buttonCancel);

    dialog.setLayout(new BorderLayout());
    dialog.add(checkboxLimit, BorderLayout.NORTH);
    dialog.add(fieldLimit, BorderLayout.CENTER);
    dialog.add(panelButtons, BorderLayout.SOUTH);

    // events
    checkboxLimit.addItemListener(e -> fieldLimit.setEnabled(checkboxLimit.getState()));

    buttonCancel.addActionListener(e -> dialog.dispose());

    buttonOk.addActionListener(e -> {
      try {
        int newLimit = Integer.parseInt(fieldLimit.getText());
        if (newLimit <= 0)
          MessageBox.showMessage(client.getFrame(),
                  "Limit must be a positive integer.", "Interactive Hicupp");
        else {
          if (checkboxLimit.getState()) numberOfTermsLimit = newLimit;
          else numberOfTermsLimit = -1;

          if (root.getChild() != null)
            root.getChild().setLimitNumberOfTerms(numberOfTermsLimit);

          client.layoutTree();
          dialog.dispose();
        }
      } catch (NumberFormatException exception) {
        MessageBox.showMessage(client.getFrame(),
                "What you entered is not a full number.", "Interactive Hicupp");
      }
    });

    // show
    dialog.pack();
    Dimension dialogSize = dialog.getSize();
    Dimension screenSize = client.getFrame().getToolkit().getScreenSize();
    dialog.setLocation((screenSize.width - dialogSize.width) / 2,
            (screenSize.height - dialogSize.height) / 2);
    dialog.setVisible(true);
  }

  public void setNumberOfDecimalPoints() {
    // elements
    final Dialog dialog = new Dialog(client.getFrame(), "Set Number of Digits After The Decimal Point");

    final Label label = new Label("Number of digits: ");

    final TextField fieldDecimalPoints = new TextField(Integer.toString(numberOfDigitsAfterDecimal));

    final Button buttonOk = new Button("Ok");
    final Button buttonCancel = new Button("Cancel");

    // organise
    final Panel panelButtons = new Panel(new FlowLayout(FlowLayout.RIGHT));
    panelButtons.add(buttonOk);
    panelButtons.add(buttonCancel);

    dialog.setLayout(new BorderLayout());
    dialog.add(label, BorderLayout.WEST);
    dialog.add(fieldDecimalPoints, BorderLayout.CENTER);
    dialog.add(panelButtons, BorderLayout.SOUTH);

    // events
    buttonCancel.addActionListener(e -> dialog.dispose());

    buttonOk.addActionListener(e -> {
      try {
        int newLimit = Integer.parseInt(fieldDecimalPoints.getText());
        if (newLimit <= 0) throw new NumberFormatException();
        else {
          numberOfDigitsAfterDecimal = newLimit;

          if (root.getChild() != null)
            root.getChild().setEquationNumberFormat(numberOfDigitsAfterDecimal);

          client.layoutTree();
          dialog.dispose();
        }
      } catch (NumberFormatException exception) {
        MessageBox.showMessage(client.getFrame(),
                "What you entered is not a positive integer.", "Interactive Hicupp");
      }
    });

    // show
    dialog.pack();
    Dimension dialogSize = dialog.getSize();
    Dimension screenSize = client.getFrame().getToolkit().getScreenSize();
    dialog.setLocation((screenSize.width - dialogSize.width) / 2,
            (screenSize.height - dialogSize.height) / 2);
    dialog.setVisible(true);
  }
}
