package hicupp;

import interactivehicupp.TextTools;

import Jama.Matrix;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

public class StructureExplorer extends JFrame {
  private final JPanel mainPanel = new JPanel();
  private final JPanel labelsPanel = new JPanel();
  private final JPanel listsPanel = new JPanel();
  private final JLabel horizontalAxisLabel = new JLabel();
  private final List horizontalAxisList = new List(8);
  private final JLabel verticalAxisLabel = new JLabel();
  private final List verticalAxisList = new List(8);
  private final JPanel showPanel = new JPanel();
  private final JButton showButton = new JButton();
  private final JButton saveButton = new JButton();
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
      StringBuilder buffer = new StringBuilder();
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
    
    showButton.setText("Show Projection");
    showButton.addActionListener(e -> showProjection());
    
    saveButton.setText("Save Axes");
    saveButton.addActionListener(e -> {
      FileDialog fileDialog = new FileDialog(StructureExplorer.this, "Save Axes As...", FileDialog.SAVE);
      fileDialog.setVisible(true);
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
          ((JFrame) plotFrames.elementAt(i)).dispose();
        dispose();
      }
    });
    
    setBackground(SystemColor.control);
    
    pack();
    setVisible(true);
  }
  
  private void showProjection() {
    int x = horizontalAxisList.getSelectedIndex();
    int y = verticalAxisList.getSelectedIndex();
    Matrix transformationMatrix = structureBasis.getMatrix(new int[] {x, y}, 0, structureBasis.getColumnDimension() - 1).transpose();
    Matrix projection = points.times(transformationMatrix);
    final JFrame frame = new PointsPlotFrame("Plot (" + x + ", " + y + ")",
                                            projection.getArray());
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        frame.dispose();
        plotFrames.removeElement(frame);
      }
    });
    frame.setVisible(true);
    plotFrames.addElement(frame);
  }
}
