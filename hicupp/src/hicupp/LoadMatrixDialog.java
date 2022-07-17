package hicupp;

import interactivehicupp.MessageBox;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LoadMatrixDialog extends Dialog {
  private final Panel mainPanel = new Panel();
  private final Panel dataFilePanel = new Panel();
  private final Label dataFileLabel = new Label();
  private final TextField dataFileTextField = new TextField();
  private final Button browseButton = new Button();
  private final Checkbox skipFirstLineCheckbox = new Checkbox();
  private final Panel columnsPanel = new Panel();
  private final Panel columnsHeaderPanel = new Panel();
  private final Label columnsLabel = new Label();
  private final List columnsList = new List(10);
  private final Button addColumnButton = new Button();
  private final Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
  private final Button loadPointsButton = new Button("Load Points");
  private final Button cancelButton = new Button("Cancel");
  
  private int columnsCount;
  private double[] coords;
  private Frame parent;
  
  public double[] getCoords() {
    return coords;
  }

  public int getColumnsCount() {
    return columnsCount;
  }
  
  public String getFilename() {
    return dataFileTextField.getText();
  }
  
  public LoadMatrixDialog(Frame parent, String title) {
    super(parent, title, true);
    
    this.parent = parent;

    LayoutTools.addWithMargin(this, mainPanel, 8);
    
    mainPanel.setLayout(new BorderLayout(6, 6));
    mainPanel.add(dataFilePanel, BorderLayout.NORTH);
    mainPanel.add(columnsPanel, BorderLayout.CENTER);
    mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        coords = null;
        hide();
      }
    });
    setBackground(SystemColor.control);
    
    dataFilePanel.setLayout(new BorderLayout(6, 6));
    dataFilePanel.add(dataFileLabel, BorderLayout.WEST);
    dataFilePanel.add(dataFileTextField, BorderLayout.CENTER);
    dataFilePanel.add(browseButton, BorderLayout.EAST);
    dataFilePanel.add(skipFirstLineCheckbox, BorderLayout.SOUTH);
    
    dataFileLabel.setText("Data file: ");
    dataFileTextField.setColumns(50);
    
    browseButton.setLabel("Browse...");
    browseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FileDialog fileDialog = new FileDialog(LoadMatrixDialog.this.parent,
                                               "Choose a Data File", FileDialog.LOAD);
        String filename = dataFileTextField.getText();
        if (!filename.equals("")) {
          File file = new File(filename);
          if (file.getParent() != null)
            fileDialog.setDirectory(file.getParent().toString());
          fileDialog.setFile(file.getName());
        }
        fileDialog.show();
        if (fileDialog.getFile() != null)
          dataFileTextField.setText(new File(fileDialog.getDirectory(), fileDialog.getFile()).toString());
      }
    });

    skipFirstLineCheckbox.setLabel("Skip First Line");
    
    columnsPanel.setLayout(new BorderLayout(6, 6));
    columnsPanel.add(columnsHeaderPanel, BorderLayout.NORTH);
    columnsPanel.add(columnsList, BorderLayout.CENTER);
    
    columnsHeaderPanel.setLayout(new BorderLayout(6, 6));
    columnsHeaderPanel.add(columnsLabel, BorderLayout.CENTER);
    columnsHeaderPanel.add(addColumnButton, BorderLayout.EAST);
    
    columnsLabel.setText("Columns:");
    
    columnsList.setMultipleMode(true);
    for (int i = 0; i < 5; i++)
      columnsList.add("Column " + (i + 1));
    
    addColumnButton.setLabel("Add column");
    addColumnButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        columnsList.add("Column " + (columnsList.getItemCount() + 1));
      }
    });
    
    loadPointsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadPoints();
      }
    });
    
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        coords = null;
        hide();
      }
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
      boolean skipFirstLine = skipFirstLineCheckbox.getState();
      coords = MatrixFileFormat.readMatrix(dataFileTextField.getText(),
                                                    skipFirstLine,
                                                    columns);
      columnsCount = columns.length;
      hide();
    } catch (IOException e) {
      MessageBox.showMessage(parent, "Could not read data file: " + e.toString(), getTitle());
    }
  }
}
