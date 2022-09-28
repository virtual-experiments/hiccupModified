package hicupp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Hicupp extends JFrame {
  private final TextField dataFileTextField = new TextField();
  private final List columnsList = new List();
  private final TextField axisTextField = new TextField();

  public Hicupp() {
    super("Hicupp");

    setLayout(new BorderLayout(6, 6));
    JPanel dataFilePanel = new JPanel();
    add(dataFilePanel, BorderLayout.NORTH);
    JPanel columnsPanel = new JPanel();
    add(columnsPanel, BorderLayout.CENTER);
    JPanel axisPanel = new JPanel();
    add(axisPanel, BorderLayout.SOUTH);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    setBackground(SystemColor.control);
    
    dataFilePanel.setLayout(new BorderLayout());
    JLabel dataFileLabel = new JLabel();
    dataFilePanel.add(dataFileLabel, BorderLayout.WEST);
    dataFilePanel.add(dataFileTextField, BorderLayout.CENTER);
    JButton browseButton = new JButton();
    dataFilePanel.add(browseButton, BorderLayout.EAST);
    
    dataFileLabel.setText("Data file: ");
    
    browseButton.setText("Browse...");
    browseButton.addActionListener(e -> {
      FileDialog fileDialog = new FileDialog(Hicupp.this, "Choose a Data File", FileDialog.LOAD);
      String filename = dataFileTextField.getText();
      if (!filename.equals("")) {
        File file = new File(filename);
        if (file.getParent() != null)
          fileDialog.setDirectory(file.getParent());
        fileDialog.setFile(file.getName());
      }
      fileDialog.setVisible(true);
      if (fileDialog.getFile() != null)
        dataFileTextField.setText(new File(fileDialog.getDirectory(), fileDialog.getFile()).toString());
    });
    
    columnsPanel.setLayout(new BorderLayout());
    JPanel columnsHeaderPanel = new JPanel();
    columnsPanel.add(columnsHeaderPanel, BorderLayout.NORTH);
    columnsPanel.add(columnsList, BorderLayout.CENTER);
    
    columnsHeaderPanel.setLayout(new BorderLayout());
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
    
    axisPanel.setLayout(new BorderLayout());
    JLabel axisLabel = new JLabel();
    axisPanel.add(axisLabel, BorderLayout.WEST);
    axisPanel.add(axisTextField, BorderLayout.CENTER);
    JButton computeAxisButton = new JButton();
    axisPanel.add(computeAxisButton, BorderLayout.EAST);
    
    axisLabel.setText("Axis: ");
    
    axisTextField.setEditable(false);
    
    computeAxisButton.setText("Compute");
    computeAxisButton.addActionListener(e -> computeAxis());
    
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
      StringBuilder buffer = new StringBuilder();
      buffer.append(axis[0]);
      for (int i = 1; i < axis.length; i++) {
        buffer.append(' ');
        buffer.append(axis[i]);
      }
      axisTextField.setText(buffer.toString());
      
      double[][] planarProjections = PrincipalPlaneFinder.projectOntoPrincipalPlane(points, axis);
      showPoints(planarProjections);
    } catch (IOException e) {
      axisTextField.setText("Could not read data file: " + e);
    } catch (NoConvergenceException e) {
      axisTextField.setText("Could not compute the axis: " + e);
    } catch (CancellationException e) {
      // Does not occur.
    }
  }
  
	public static void main(String[] args) {
    new Hicupp().setVisible(true);
	}
  
  private void showPoints(double[][] coords) {
    JFrame frame = new JFrame("Plot - Hicupp");
    PointsPlot plot = new PointsPlot();
    plot.setCoords(coords);
    frame.add(plot);
    frame.pack();
    frame.setVisible(true);
  }
}
