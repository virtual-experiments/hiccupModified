package hicupp;

import java.awt.*;
import java.io.*;
import imageformats.*;

public class PointsPlotFrame extends Frame {
  private PointsPlot pointsPlot = new PointsPlot();
  
  public PointsPlotFrame(String title) {
    super(title);
    MenuBar menuBar = new MenuBar();
    menuBar.setFont(new Font("MenuFont", Font.PLAIN, 14));
    Menu fileMenu = new Menu("File");
    MenuItem saveMenuItem = new MenuItem("Save...");
    saveMenuItem.addActionListener(e -> {
      FileDialog fileDialog = new FileDialog(PointsPlotFrame.this, "Save Plot BMP As...", FileDialog.SAVE);
      fileDialog.setFile("*.bmp");
      fileDialog.setVisible(true);
      if (fileDialog.getFile() != null) {
        try {
          String filename = fileDialog.getFile();
          if (!filename.endsWith(".bmp")) filename += ".bmp";
          RGBAImage image = pointsPlot.createRGBAImage();
          BMPFileFormat.writeImage(new File(fileDialog.getDirectory(), filename).toString(), image);
        } catch (IOException ex) {
          interactivehicupp.MessageBox.showMessage(PointsPlotFrame.this, "Could not save plot BMP: " + ex, "Hicupp");
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
