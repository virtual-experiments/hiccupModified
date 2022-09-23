package hicupp;

import interactivehicupp.MessageBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;

public class LoadMatrixDialog extends LoadDialog {
  private final TextField dataFileTextField = new TextField();
  private final JCheckBox skipFirstLineCheckbox = new JCheckBox();
  private final List columnsList = new List(10);

  private int columnsCount;
  private double[] coords;
  private final JFrame parent;

  @Override
  public double[] getCoords() {
    return coords;
  }

  @Override
  public int getColumnsCount() {
    return columnsCount;
  }

  @Override
  public String getFilename() {
    return dataFileTextField.getText();
  }

  @Override
  public String[] getParameterNames() {
    return null;
  }

  @Override
  public int skipFirstLine() {
    return (skipFirstLineCheckbox.isSelected())? 1 : 0;
  }

  @Override
  public String printChosenColumns() {
    StringBuilder builder = new StringBuilder();

    for (int column : columnsList.getSelectedIndexes()) {
      builder.append(column);
      builder.append(" ");
    }

    return builder.toString();
  }

  @Override
  public void load(String filename, int skipFirstLine, int[] chosenColumns) {
    try {
      coords = MatrixFileFormat.readMatrix(dataFileTextField.getText(),
              skipFirstLine == 1,
              chosenColumns);
      columnsCount = chosenColumns.length;

      dataFileTextField.setText(filename);
      skipFirstLineCheckbox.setSelected(skipFirstLine == 1);

      columnsList.removeAll();
      for (int i = 0; i < Arrays.stream(chosenColumns).max().orElse(5); i++)
        columnsList.add("Column " + (i + 1));
      for (int i : chosenColumns) columnsList.select(i);

    } catch (IOException e) {
      MessageBox.showMessage(parent, "Could not read data file: " + e, getTitle());
    }
  }

  @Override
  public void disableColumnsSelection() {
    columnsList.setEnabled(false);
  }

  public LoadMatrixDialog(JFrame parent, String title) {
    super(parent, title, true);
    
    this.parent = parent;

    JPanel mainPanel = new JPanel();
    LayoutTools.addWithMargin(this, mainPanel, 8);
    
    mainPanel.setLayout(new BorderLayout(6, 6));
    JPanel dataFilePanel = new JPanel();
    mainPanel.add(dataFilePanel, BorderLayout.NORTH);
    JPanel columnsPanel = new JPanel();
    mainPanel.add(columnsPanel, BorderLayout.CENTER);
    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        coords = null;
        setVisible(false);
      }
    });
    setBackground(SystemColor.control);
    
    dataFilePanel.setLayout(new BorderLayout(6, 6));
    JLabel dataFileLabel = new JLabel();
    dataFilePanel.add(dataFileLabel, BorderLayout.WEST);
    dataFilePanel.add(dataFileTextField, BorderLayout.CENTER);
    JButton browseButton = new JButton();
    dataFilePanel.add(browseButton, BorderLayout.EAST);
    dataFilePanel.add(skipFirstLineCheckbox, BorderLayout.SOUTH);
    
    dataFileLabel.setText("Data file: ");
    dataFileTextField.setColumns(50);
    
    browseButton.setText("Browse...");
    browseButton.addActionListener(e -> {
      FileDialog fileDialog = new FileDialog(LoadMatrixDialog.this.parent,
                                             "Choose a Data File", FileDialog.LOAD);
      String filename = dataFileTextField.getText();
      if (!filename.equals("")) {
        columnsList.setEnabled(true);
        File file = new File(filename);
        if (file.getParent() != null)
          fileDialog.setDirectory(file.getParent());
        fileDialog.setFile(file.getName());
      }
      fileDialog.setVisible(true);
      if (fileDialog.getFile() != null)
        dataFileTextField.setText(new File(fileDialog.getDirectory(), fileDialog.getFile()).toString());
    });

    skipFirstLineCheckbox.setText("Skip First Line");
    
    columnsPanel.setLayout(new BorderLayout(6, 6));
    JPanel columnsHeaderPanel = new JPanel();
    columnsPanel.add(columnsHeaderPanel, BorderLayout.NORTH);
    columnsPanel.add(columnsList, BorderLayout.CENTER);
    
    columnsHeaderPanel.setLayout(new BorderLayout(6, 6));
    JLabel columnsLabel = new JLabel();
    columnsHeaderPanel.add(columnsLabel, BorderLayout.CENTER);
    JButton addColumnButton = new JButton();
    columnsHeaderPanel.add(addColumnButton, BorderLayout.EAST);
    
    columnsLabel.setText("Columns:");
    
    columnsList.setMultipleMode(true);
    for (int i = 0; i < 5; i++)
      columnsList.add("Column " + (i + 1));
    
    addColumnButton.setText("Add column");
    addColumnButton.addActionListener(e -> columnsList.add("Column " + (columnsList.getItemCount() + 1)));

    JButton loadPointsButton = new JButton("Load Points");
    loadPointsButton.addActionListener(e -> loadPoints());

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> {
      coords = null;
      setVisible(false);
    });
    
    buttonsPanel.add(loadPointsButton);
    buttonsPanel.add(cancelButton);
    
    pack();
    Dimension screenSize = getToolkit().getScreenSize();
    Dimension size = getSize();
    setLocation((screenSize.width - size.width) / 2,
                (screenSize.height - size.height) / 2);
  }
  
  private void loadPoints() {
    try {
      int[] columns = columnsList.getSelectedIndexes();
      if (columns.length < 2) {
        MessageBox.showMessage(parent, "Select at least two columns!", getTitle());
        return;
      }
      boolean skipFirstLine = skipFirstLineCheckbox.isSelected();
      coords = MatrixFileFormat.readMatrix(dataFileTextField.getText(),
                                                    skipFirstLine,
                                                    columns);
      columnsCount = columns.length;
      setVisible(false);
    } catch (IOException e) {
      MessageBox.showMessage(parent, "Could not read data file: " + e, getTitle());
    }
  }
}
