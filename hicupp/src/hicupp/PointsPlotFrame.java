package hicupp;

import java.awt.*;
import java.io.*;
import imageformats.*;
import interactivehicupp.DocumentFrame;

import javax.swing.*;

public class PointsPlotFrame extends JFrame {
  private final PointsPlot pointsPlot = new PointsPlot();
  
  public PointsPlotFrame(String title) {
    super(title);
    JMenuBar menuBar = new JMenuBar();
    menuBar.setFont(DocumentFrame.menuFont);
    JMenu fileMenu = new JMenu("File");
    JMenuItem saveMenuItem = new JMenuItem("Save...");
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
    setJMenuBar(menuBar);
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
