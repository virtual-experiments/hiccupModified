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
import java.util.Random;

import hicupp.*;
import hicupp.classify.*;
import hicupp.trees.*;

import javax.swing.*;

public class GeneralPointsSourceProvider implements PointsSourceProvider {
  private final JMenu pointsMenu = new JMenu();
  private final JMenu viewMenu = new JMenu();
  private LoadDialog loadDialog;
  
  private final PointsSourceClient client;
  private final ClassTree classTree;
  private final GeneralNodeView root;
  
  private double[] coords;
  private int ndims;
  private SetOfPoints points;
  private String[] parameterNames;

  private String chosenFile = null;
  private String metadata = "N/A\nN/A\nN/A";

  private int numberOfDigitsAfterDecimal = 2;
  private int numberOfTermsLimit = -1;
  
  private class GeneralNodeView extends AbstractNodeView {
    private final JLabel component = new JLabel();
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
    void addNodePopupMenuItems(JPopupMenu popupMenu) {
      final JMenuItem inspectMenuItem = new JMenuItem("Inspect");
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
      String[][] pointsString = new String[pointCount + 1][ndims + 1];
      String digits = "0".repeat(numberOfDigitsAfterDecimal);
      NumberFormat format = new DecimalFormat("##0." + digits);

      int lengthPoints = (int) (Math.log10(pointCount) + 1);
      for (int i = 0; i < pointCount + 1; i++) {
        if (i == 0) pointsString[i][0] = "_".repeat(lengthPoints);
        else {
          String number = Integer.toString(i);
          String zeroes = "0".repeat(lengthPoints - number.length());
          pointsString[i][0] = zeroes + number;
        }
      }

      int maxLength = 0;
      for (int i = 0; i < pointCount; i++) {
        for (int j = 0; j < ndims; j++) {
          int index = i + pointCount * j;
          String number = format.format(coords[index]);
          pointsString[i + 1][j + 1] = (getClassNode().containsPointAtIndex(i))?
                  number : "_".repeat(number.length());
          if (number.length() > maxLength) maxLength = number.length();
        }
      }

      for (int i = 0; i < ndims; i++) {
        String param = parameterNames[i];
        param = "_".repeat((maxLength - param.length()) / 2) + param + "_".repeat((maxLength - param.length()) / 2);
        pointsString[0][i + 1] = param;
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

        for (String[] row : pointsString) {
          StringBuilder newRow = new StringBuilder();
          for (String element : row) {
            newRow.append(element).append("\t");
          }
          columns = Math.max(columns, newRow.length());
          builder.append(newRow).append("\n");
        }

        textArea.setColumns(columns * 5/2);
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

  private void generateDefaultMatrix(int ndims) {
    coords = new double[ndims * 10];
    Random random = new Random();

    for (int i = 0; i < ndims * 10; i++) {
      int j = random.nextInt(11); // 0 to 10
      coords[i] = j;
    }

    this.ndims = ndims;
    points = new ArraySetOfPoints(ndims, coords);
    generateDefaultParameterNames();
  }
  
  private void generateDefaultParameterNames() {
    parameterNames = new String[ndims];
    for (int i = 0; i < ndims; i++)
      parameterNames[i] = "p" + i;
  }
  
  private void loadPointsFromFile(boolean csv) {
    if (loadDialog == null) {
      if (csv)
        loadDialog = new LoadCSVDialog(client.getFrame(), "Load Points from CSV File");
      else
        loadDialog = new LoadMatrixDialog(client.getFrame(), "Load Points from ASCII File");
    } else if (loadDialog instanceof LoadMatrixDialog && csv)   // from matrix to csv
        loadDialog = new LoadCSVDialog(client.getFrame(), "Load Points from CSV File");
      else if (loadDialog instanceof LoadCSVDialog && !csv)     // from csv to matrix
        loadDialog = new LoadMatrixDialog(client.getFrame(), "Load Points from ASCII File");
      else if (root.getChild() != null)                         // prevent changing dimensions
        loadDialog.disableColumnsSelection();

    loadDialog.setVisible(true);
    loadPoints(loadDialog.getCoords());
  }

  private void loadPointsFromSaveTree() {
    if (TreeFileFormat.fileExtension.toString().equals("csv"))
      loadDialog = new LoadCSVDialog(client.getFrame(), "Load Points from CSV File");
    else
      loadDialog = new LoadMatrixDialog(client.getFrame(), "Load Points from ASCII File");

    loadDialog.load(chosenFile, TreeFileFormat.skipFirstLine, TreeFileFormat.chosenColumns);
    loadPoints(loadDialog.getCoords());
  }

  private void loadPoints(double[] coords) {
    if (coords != null) {
      int ndims = loadDialog.getColumnsCount();
      if (classTree.getRoot().getNode().getChild() != null && ndims != this.ndims)
        MessageBox.showMessage(client.getFrame(),
                "Cannot load points: number of dimensions incompatible with split rules in tree.",
                "Interactive Hicupp");
      else {
        this.ndims = ndims;
        this.coords = coords;
        this.points = new ArraySetOfPoints(ndims, coords);

        String[] newParameters = loadDialog.getParameterNames();
        if (newParameters != null) parameterNames = newParameters;
        else generateDefaultParameterNames();

        if (root.getChild() != null)
          root.getChild().setParameterNames(parameterNames);

        classTree.setPoints(points);
        chosenFile = loadDialog.getFilename();
        setMetadata();
        client.layoutTree();

        JTextArea logTextArea = client.getLogTextArea();
        if (!logTextArea.getText().equals("")) logTextArea.append("\n");
        logTextArea.append("Loaded dataset " + chosenFile + " with " + this.ndims + " dimensions.\n");
      }
    }
  }
  
  public GeneralPointsSourceProvider(PointsSourceClient client, Tree tree) {
    this.client = client;

    {
      pointsMenu.setText("Points");

      JMenuItem pointsLoadPointsMenuItem = new JMenuItem("Load Points From ASCII File...");
      pointsLoadPointsMenuItem.addActionListener(e -> loadPointsFromFile(false));
      pointsMenu.add(pointsLoadPointsMenuItem);

      JMenuItem pointsFromCSVMenuItem = new JMenuItem("Load Points From CSV...");
      pointsFromCSVMenuItem.addActionListener(e -> loadPointsFromFile(true));
      pointsMenu.add(pointsFromCSVMenuItem);
    }

    {
      viewMenu.setText("View");

      JMenuItem numberOfDecimalPointsMenuItem = new JMenuItem("Set number of digits after the decimal point");
      numberOfDecimalPointsMenuItem.addActionListener(e -> setNumberOfDecimalPoints());
      viewMenu.add(numberOfDecimalPointsMenuItem);

      JMenuItem numberOfTermsMenuItem = new JMenuItem("Set number of terms in decision tree");
      numberOfTermsMenuItem.addActionListener(e -> setNumberOfTermsLimit());
      viewMenu.add(numberOfTermsMenuItem);
    }

    generateDefaultMatrix(tree.getNdims());
    
    classTree = new ClassTree(tree, points);
    root = new GeneralNodeView(null, classTree.getRoot());
  }

  @Override
  public NodeView getRoot() {
    return root;
  }

  @Override
  public void addMenuBarItems(JMenuBar menuBar) {
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
    loadPointsFromSaveTree();
  }

  private void setMetadata() {
    try {
      Path path = Paths.get(chosenFile);

      long kilobytes = Files.size(path) / 1024;
      String type = Optional.of(chosenFile)
              .filter(f -> f.contains("."))
              .map(f -> f.substring(chosenFile.lastIndexOf(".") + 1))
              .orElse("N/A");

      metadata = kilobytes + "\n" +
              type + "\n" +
              loadDialog.skipFirstLine() + " " + loadDialog.printChosenColumns();
    } catch (IOException | NullPointerException exception) {
      metadata = "N/A\nN/A\nN/A";
      exception.printStackTrace();
    }
  }
  
  public String[] getParameterNames() {
    return parameterNames;
  }

  public void setNumberOfTermsLimit() {
    boolean limit = numberOfTermsLimit != -1;

    // elements
    final JDialog dialog = new JDialog(client.getFrame(), "Set Terms Limit");

    final JCheckBox checkboxLimit = new JCheckBox("Limit number of terms in decision tree", limit);

    final JTextField fieldLimit = new JTextField(Integer.toString((limit)? numberOfTermsLimit : 3));
    fieldLimit.setEnabled(limit);

    final JButton buttonOk = new JButton("Ok");
    final JButton buttonCancel = new JButton("Cancel");

    // organise
    final JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelButtons.add(buttonOk);
    panelButtons.add(buttonCancel);

    dialog.setLayout(new BorderLayout());
    dialog.add(checkboxLimit, BorderLayout.NORTH);
    dialog.add(fieldLimit, BorderLayout.CENTER);
    dialog.add(panelButtons, BorderLayout.SOUTH);

    // events
    checkboxLimit.addItemListener(e -> fieldLimit.setEnabled(checkboxLimit.isSelected()));

    buttonCancel.addActionListener(e -> dialog.dispose());

    buttonOk.addActionListener(e -> {
      try {
        int newLimit = Integer.parseInt(fieldLimit.getText());
        if (newLimit <= 0)
          MessageBox.showMessage(client.getFrame(),
                  "Limit must be a positive integer.", "Interactive Hicupp");
        else {
          if (checkboxLimit.isSelected()) numberOfTermsLimit = newLimit;
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
    final JDialog dialog = new JDialog(client.getFrame(), "Set Number of Digits After The Decimal Point");

    final JLabel label = new JLabel("Number of digits: ");

    final JTextField fieldDecimalPoints = new JTextField(Integer.toString(numberOfDigitsAfterDecimal));

    final JButton buttonOk = new JButton("Ok");
    final JButton buttonCancel = new JButton("Cancel");

    // organise
    final JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
