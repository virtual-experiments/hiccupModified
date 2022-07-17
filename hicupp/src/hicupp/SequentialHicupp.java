package hicupp;

import interactivehicupp.MessageBox;
import interactivehicupp.MonitorDialog;

import Jama.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SequentialHicupp extends Frame {
  private final LoadMatrixDialog loadMatrixDialog = new LoadMatrixDialog(this, "Load Points");
  private final Panel mainPanel = new Panel();
  private final Panel dimensionCountPanel = new Panel();
  private final Label dimensionCountLabel = new Label();
  private final Scrollbar dimensionCountScrollbar;
  private final Panel indexesPanel = new Panel();
  private final Label indexLabel = new Label();
  private final List indexesList = new List(10);
  private final Button goButton = new Button();
  private final TextArea logTextArea = new TextArea();
  private final Frame logFrame = new Frame();
                     
  private void updateDimensionCountLabel() {
    dimensionCountLabel.setText("Max. Dimensions: " + dimensionCountScrollbar.getValue());
  }
  
  public SequentialHicupp() {
    super("Clustering using Sequential Projection Pursuit");

    LayoutTools.addWithMargin(this, mainPanel, 8);
    
    dimensionCountScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, 3, 1, 1, 9 + 1);
    dimensionCountScrollbar.addAdjustmentListener(new AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent e) {
        updateDimensionCountLabel();
      }
    });
    updateDimensionCountLabel();

    dimensionCountPanel.setLayout(new BorderLayout());
    dimensionCountPanel.add(dimensionCountLabel, BorderLayout.CENTER);
    dimensionCountPanel.add(dimensionCountScrollbar, BorderLayout.EAST);
    
    indexesPanel.setLayout(new BorderLayout());
    indexesPanel.add(indexLabel, BorderLayout.WEST);
    indexesPanel.add(indexesList, BorderLayout.CENTER);
    
    mainPanel.setLayout(new BorderLayout(6, 6));
    mainPanel.add(dimensionCountPanel, BorderLayout.NORTH);
    mainPanel.add(indexesPanel, BorderLayout.CENTER);
    mainPanel.add(goButton, BorderLayout.SOUTH);
    
    indexLabel.setText("Projection index:");
    String[] names = ProjectionIndexFunction.getProjectionIndexNames();
    for (int i = 0; i < names.length; i++)
      indexesList.add(names[i]);
    
    goButton.setLabel("Load Points and Show Structure Explorer");
    goButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        computeStructureBasis();
      }
    });
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    
    setBackground(SystemColor.control);
    
    {
      logFrame.add(logTextArea, BorderLayout.CENTER);
      logFrame.setTitle("Log Window");
      MenuBar menuBar = new MenuBar();
      Menu fileMenu = new Menu("File");
      MenuItem save = new MenuItem("Save...");
      save.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          FileDialog fileDialog = new FileDialog(logFrame, "Save Log As", FileDialog.SAVE);
          fileDialog.show();
          if (fileDialog.getFile() != null) {
            try {
              Writer writer = new FileWriter(new File(fileDialog.getDirectory(), fileDialog.getFile()));
              writer.write(logTextArea.getText());
              writer.close();
            } catch (IOException ex) {
              MessageBox.showMessage(logFrame, "Could not save the log: " + ex, "Hicupp");
            }
          }
        }
      });
      fileMenu.add(save);
      Menu menu = new Menu("Edit");
      MenuItem clear = new MenuItem("Clear");
      menuBar.add(fileMenu);
      menuBar.add(menu);
      menu.add(clear);
      clear.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          logTextArea.setText("");
        }
      });
      logFrame.setMenuBar(menuBar);
      logFrame.pack();
      logFrame.show();
    }
    
    pack();
    Dimension screenSize = getToolkit().getScreenSize();
    Dimension size = getSize();
    setLocation((screenSize.width - size.width) / 2,
                (screenSize.height - size.height) / 2);
  }
  
  private void computeStructureBasis() {
    try {
      loadMatrixDialog.show();
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
      new StructureExplorer(points, structureBasis, title).show();
    } catch (NoConvergenceException e) {
      MessageBox.showMessage(this, "Could not compute the axis: " + e.toString(), "Sequential Hicupp");
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
