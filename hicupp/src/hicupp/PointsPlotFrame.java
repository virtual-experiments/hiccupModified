package hicupp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import imageformats.*;

public class PointsPlotFrame extends Frame {
  private PointsPlot pointsPlot = new PointsPlot();
  
  public PointsPlotFrame(String title) {
    super(title);
    MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");
    MenuItem saveMenuItem = new MenuItem("Save...");
    saveMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FileDialog fileDialog = new FileDialog(PointsPlotFrame.this, "Save Plot BMP As...", FileDialog.SAVE);
        fileDialog.show();
        if (fileDialog.getFile() != null) {
          try {
            RGBAImage image = pointsPlot.createRGBAImage();
            BMPFileFormat.writeImage(new File(fileDialog.getDirectory(), fileDialog.getFile()).toString(), image);
          } catch (IOException ex) {
            interactivehicupp.MessageBox.showMessage(PointsPlotFrame.this, "Could not save plot BMP: " + ex, "Hicupp");
          }
        }
      }
    });
    fileMenu.add(saveMenuItem);
    menuBar.add(fileMenu);
    setMenuBar(menuBar);
    // LayoutTools.addWithMargin(this, pointsPlot, 8);
    add(pointsPlot, BorderLayout.CENTER);
    setSize(200, 200);
    validate();
  }
  
  public PointsPlotFrame(String title, double[][] coords) {
    this(title);
    pointsPlot.setCoords(coords);
  }
  
  public PointsPlot getPointsPlot() {
    return pointsPlot;
  }
}
