package hicupp;

import interactivehicupp.MessageBox;
import interactivehicupp.MonitorDialog;

import Jama.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SequentialHicupp extends JFrame {
  private final LoadMatrixDialog loadMatrixDialog = new LoadMatrixDialog(this, "Load Points");
  private final JLabel dimensionCountLabel = new JLabel();
  private final JScrollBar dimensionCountScrollbar;
  private final List indexesList = new List(10);
  private final JTextArea logTextArea = new JTextArea();
  private final JFrame logFrame = new JFrame();
                     
  private void updateDimensionCountLabel() {
    dimensionCountLabel.setText("Max. Dimensions: " + dimensionCountScrollbar.getValue());
  }
  
  public SequentialHicupp() {
    super("Clustering using Sequential Projection Pursuit");

    JPanel mainPanel = new JPanel();
    LayoutTools.addWithMargin(this, mainPanel, 8);
    
    dimensionCountScrollbar = new JScrollBar(Adjustable.HORIZONTAL, 3, 1, 1, 9 + 1);
    dimensionCountScrollbar.addAdjustmentListener(e -> updateDimensionCountLabel());
    updateDimensionCountLabel();

    JPanel dimensionCountPanel = new JPanel();
    dimensionCountPanel.setLayout(new BorderLayout());
    dimensionCountPanel.add(dimensionCountLabel, BorderLayout.CENTER);
    dimensionCountPanel.add(dimensionCountScrollbar, BorderLayout.EAST);

    JPanel indexesPanel = new JPanel();
    indexesPanel.setLayout(new BorderLayout());
    JLabel indexLabel = new JLabel();
    indexesPanel.add(indexLabel, BorderLayout.WEST);
    indexesPanel.add(indexesList, BorderLayout.CENTER);
    
    mainPanel.setLayout(new BorderLayout(6, 6));
    mainPanel.add(dimensionCountPanel, BorderLayout.NORTH);
    mainPanel.add(indexesPanel, BorderLayout.CENTER);
    JButton goButton = new JButton();
    mainPanel.add(goButton, BorderLayout.SOUTH);
    
    indexLabel.setText("Projection index:");
    String[] names = ProjectionIndexFunction.getProjectionIndexNames();
    for (String name : names) indexesList.add(name);
    
    goButton.setText("Load Points and Show Structure Explorer");
    goButton.addActionListener(e -> computeStructureBasis());
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    
    setBackground(SystemColor.control);
    
    {
      logFrame.add(logTextArea, BorderLayout.CENTER);
      logTextArea.setColumns(30);
      logTextArea.setRows(30);
      logFrame.setTitle("Log Window");
      JMenuBar menuBar = new JMenuBar();
      JMenu fileMenu = new JMenu("File");
      JMenuItem save = new JMenuItem("Save...");
      save.addActionListener(e -> {
        FileDialog fileDialog = new FileDialog(logFrame, "Save Log As", FileDialog.SAVE);
        fileDialog.setVisible(true);
        if (fileDialog.getFile() != null) {
          try {
            Writer writer = new FileWriter(new File(fileDialog.getDirectory(), fileDialog.getFile()));
            writer.write(logTextArea.getText());
            writer.close();
          } catch (IOException ex) {
            MessageBox.showMessage(logFrame, "Could not save the log: " + ex, "Hicupp");
          }
        }
      });
      fileMenu.add(save);
      JMenu menu = new JMenu("Edit");
      JMenuItem clear = new JMenuItem("Clear");
      menuBar.add(fileMenu);
      menuBar.add(menu);
      menu.add(clear);
      clear.addActionListener(e -> logTextArea.setText(""));
      logFrame.setJMenuBar(menuBar);
      logFrame.pack();
      logFrame.setVisible(true);
    }
    
    pack();
    Dimension screenSize = getToolkit().getScreenSize();
    Dimension size = getSize();
    setLocation((screenSize.width - size.width) / 2,
                (screenSize.height - size.height) / 2);
  }
  
  private void computeStructureBasis() {
    try {
      loadMatrixDialog.setVisible(true);
      if (loadMatrixDialog.getCoords() == null)
        return;
      String filename = new File(loadMatrixDialog.getFilename()).getName();
      String title = "Structure of " +
                     filename +
                     " (index: " +
                     ProjectionIndexFunction.getProjectionIndexNames()[indexesList.getSelectedIndex()] +
                     ")";
      logTextArea.append(title + "\n");
      SetOfPoints points = new ArraySetOfPoints(loadMatrixDialog.getColumnsCount(),
                                                loadMatrixDialog.getCoords());
      Matrix structureBasis = computeStructureBasis(points);
      new StructureExplorer(points, structureBasis, title).setVisible(true);
    } catch (NoConvergenceException e) {
      MessageBox.showMessage(this, "Could not compute the axis: " + e, "Sequential Hicupp");
    } catch (CancellationException e) {
      // Does not occur.
    }
  }
  
  private Matrix computeStructureBasis(final SetOfPoints points)
      throws NoConvergenceException, CancellationException {
    
    final MonitorDialog monitorDialog = new MonitorDialog(this);
    
    class Computation implements Runnable {
      public volatile Matrix structureBasis;
      public volatile Exception exception;
      public void run() {
        try {
          structureBasis = StructureBasisFinder.computeStructureBasis(indexesList.getSelectedIndex(),
                                                                      points,
                                                                      dimensionCountScrollbar.getValue(),
                                                                      monitorDialog);
        } catch (Exception e) {
          exception = e;
        }
      }
    }
    
    Computation computation = new Computation();

    monitorDialog.show(computation, logTextArea);

    if (computation.exception != null) {
      if (computation.exception instanceof NoConvergenceException)
        throw (NoConvergenceException) computation.exception;
      if (computation.exception instanceof CancellationException)
        throw (CancellationException) computation.exception;
      throw (RuntimeException) computation.exception;
    }

    return computation.structureBasis;
  }
  
	public static void main(String[] args) {
    new SequentialHicupp().setVisible(true);
	}
}
