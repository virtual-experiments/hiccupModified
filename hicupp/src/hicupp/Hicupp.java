package hicupp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Hicupp extends Frame {
  private final Panel dataFilePanel = new Panel();
  private final Label dataFileLabel = new Label();
  private final TextField dataFileTextField = new TextField();
  private final Button browseButton = new Button();
  private final Panel columnsPanel = new Panel();
  private final Panel columnsHeaderPanel = new Panel();
  private final Label columnsLabel = new Label();
  private final List columnsList = new List();
  private final Button addColumnButton = new Button();
  private final Panel axisPanel = new Panel();
  private final Label axisLabel = new Label();
  private final TextField axisTextField = new TextField();
  private final Button computeAxisButton = new Button();

  public Hicupp() {
    super("Hicupp");

    setLayout(new BorderLayout(6, 6));
    add(dataFilePanel, BorderLayout.NORTH);
    add(columnsPanel, BorderLayout.CENTER);
    add(axisPanel, BorderLayout.SOUTH);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    setBackground(SystemColor.control);
    
    dataFilePanel.setLayout(new BorderLayout());
    dataFilePanel.add(dataFileLabel, BorderLayout.WEST);
    dataFilePanel.add(dataFileTextField, BorderLayout.CENTER);
    dataFilePanel.add(browseButton, BorderLayout.EAST);
    
    dataFileLabel.setText("Data file: ");
    
    browseButton.setLabel("Browse...");
    browseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FileDialog fileDialog = new FileDialog(Hicupp.this, "Choose a Data File", FileDialog.LOAD);
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
    
    columnsPanel.setLayout(new BorderLayout());
    columnsPanel.add(columnsHeaderPanel, BorderLayout.NORTH);
    columnsPanel.add(columnsList, BorderLayout.CENTER);
    
    columnsHeaderPanel.setLayout(new BorderLayout());
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
    
    axisPanel.setLayout(new BorderLayout());
    axisPanel.add(axisLabel, BorderLayout.WEST);
    axisPanel.add(axisTextField, BorderLayout.CENTER);
    axisPanel.add(computeAxisButton, BorderLayout.EAST);
    
    axisLabel.setText("Axis: ");
    
    axisTextField.setEditable(false);
    
    computeAxisButton.setLabel("Compute");
    computeAxisButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        computeAxis();
      }
    });
    
    pack();
  }
  
  private void computeAxis() {
    try {
      int[] columns = columnsList.getSelectedIndexes();
      if (columns.length < 2) {
        axisTextField.setText("Select at least two columns!");
        return;
      }
      double[] matrix = MatrixFileFormat.readMatrix(dataFileTextField.getText(), false, columns);
      SetOfPoints points = new ArraySetOfPoints(columns.length, matrix);
      double[] axis = Clusterer.findAxis(ProjectionIndexFunction.FRIEDMANS_PROJECTION_INDEX, points, null);
      StringBuffer buffer = new StringBuffer();
      buffer.append(axis[0]);
      for (int i = 1; i < axis.length; i++) {
        buffer.append(' ');
        buffer.append(axis[i]);
      }
      axisTextField.setText(buffer.toString());
      
      double[][] planarProjections = PrincipalPlaneFinder.projectOntoPrincipalPlane(points, axis);
      showPoints(planarProjections);
    } catch (IOException e) {
      axisTextField.setText("Could not read data file: " + e.toString());
    } catch (NoConvergenceException e) {
      axisTextField.setText("Could not compute the axis: " + e.toString());
    } catch (CancellationException e) {
      // Does not occur.
    }
  }
  
	public static void main(String[] args) {
    new Hicupp().setVisible(true);
	}
  
  private void showPoints(double[][] coords) {
    Frame frame = new Frame("Plot - Hicupp");
    PointsPlot plot = new PointsPlot();
    plot.setCoords(coords);
    frame.add(plot);
    frame.pack();
    frame.setVisible(true);
  }
}
