package hicupp;

import interactivehicupp.TextTools;

import Jama.Matrix;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

public class StructureExplorer extends Frame {
  private final Panel mainPanel = new Panel();
  private final Panel labelsPanel = new Panel();
  private final Panel listsPanel = new Panel();
  private final Label horizontalAxisLabel = new Label();
  private final List horizontalAxisList = new List(8);
  private final Label verticalAxisLabel = new Label();
  private final List verticalAxisList = new List(8);
  private final Panel showPanel = new Panel();
  private final Button showButton = new Button();
  private final Button saveButton = new Button();
  private final Vector plotFrames = new Vector();
  
  private final Matrix structureBasis;
  private final Matrix points;
  
  public StructureExplorer(SetOfPoints points, final Matrix structureBasis, final String title) {
    super(title);
    
    this.structureBasis = structureBasis;
    this.points = MatrixTools.setOfPointsToMatrix(points);
    
    labelsPanel.setLayout(new GridLayout(1, 0, 6, 6));
    labelsPanel.add(horizontalAxisLabel);
    labelsPanel.add(verticalAxisLabel);
    
    listsPanel.setLayout(new GridLayout(1, 0, 6, 6));
    listsPanel.add(horizontalAxisList);
    listsPanel.add(verticalAxisList);
    
    horizontalAxisLabel.setText("Horizontal:");
    verticalAxisLabel.setText("Vertical:");
    
    for (int i = 0; i < structureBasis.getRowDimension(); i++) {
      StringBuffer buffer = new StringBuffer();
      buffer.append(i);
      buffer.append(": ");
      for (int j = 0; j < structureBasis.getColumnDimension(); j++) {
        if (j > 0)
          buffer.append(", ");
        buffer.append(TextTools.formatScientific(structureBasis.get(i, j)));
      }
      String line = buffer.toString();
      
      horizontalAxisList.add(line);
      verticalAxisList.add(line);
    }
    
    showButton.setLabel("Show Projection");
    showButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showProjection();
      }
    });
    
    saveButton.setLabel("Save Axes");
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FileDialog fileDialog = new FileDialog(StructureExplorer.this, "Save Axes As...", FileDialog.SAVE);
        fileDialog.show();
        if (fileDialog.getFile() != null) {
          try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(new File(fileDialog.getDirectory(), fileDialog.getFile())));
            printWriter.println(title);
            for (int i = 0; i < structureBasis.getRowDimension(); i++) {
              for (int j = 0; j < structureBasis.getColumnDimension(); j++) {
                if (j > 0)
                  printWriter.print(' ');
                printWriter.print(structureBasis.get(i, j));
              }
              printWriter.println();
            }
            printWriter.close();
          } catch (IOException ex) {
            interactivehicupp.MessageBox.showMessage(StructureExplorer.this, "Could not save axes: " + ex, "Hicupp");
          }
        }
      }
    });
    
    LayoutTools.addWithMargin(this, mainPanel, 8);
    
    showPanel.setLayout(new BorderLayout(6, 6));
    showPanel.add(labelsPanel, BorderLayout.NORTH);
    showPanel.add(listsPanel, BorderLayout.CENTER);
    showPanel.add(showButton, BorderLayout.SOUTH);
    
    mainPanel.add(saveButton);
    mainPanel.add(showPanel);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        for (int i = 0; i < plotFrames.size(); i++)
          ((Frame) plotFrames.elementAt(i)).dispose();
        dispose();
      }
    });
    
    setBackground(SystemColor.control);
    
    pack();
    show();
  }
  
  private void showProjection() {
    int x = horizontalAxisList.getSelectedIndex();
    int y = verticalAxisList.getSelectedIndex();
    Matrix transformationMatrix = structureBasis.getMatrix(new int[] {x, y}, 0, structureBasis.getColumnDimension() - 1).transpose();
    Matrix projection = points.times(transformationMatrix);
    final Frame frame = new PointsPlotFrame("Plot (" + x + ", " + y + ")",
                                            projection.getArray());
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        frame.dispose();
        plotFrames.removeElement(frame);
      }
    });
    frame.show();
    plotFrames.addElement(frame);
  }
}
