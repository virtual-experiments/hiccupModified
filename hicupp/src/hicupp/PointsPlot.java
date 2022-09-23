package hicupp;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import imageformats.RGBAImage;

import javax.swing.*;

public class PointsPlot extends JPanel {
  private double[][] coords;
  private double minx, maxx, miny, maxy;
  private int gridSize;
  private Image image;
  private boolean drawThreshold;
  private double threshold;

  public PointsPlot() {
    setBackground(Color.white);
    setGridSize(100);
    
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
          setGridSize(gridSize * 2);
        else
          setGridSize(gridSize / 2);
      }
    });
  }
  
  public void setGridSize(int value) {
    gridSize = value;
    if (coords != null)
      updatePixels();
    repaint();
  }
  
  public void setThreshold(double threshold) {
    drawThreshold = true;
    this.threshold = threshold;
    repaint();
  }
  
  public RGBAImage createRGBAImage() {
    int[] gridPixels = new int[gridSize * gridSize];
    int maxCount = 0;
    for (double[] point : coords) {
      double x = point[0];
      double y = point[1];
      int gridX = (int) ((x - minx) * (gridSize / (maxx - minx)));
      int gridY = (int) ((y - miny) * (gridSize / (maxy - miny)));
      if (gridX < 0)
        gridX = 0;
      else if (gridSize <= gridX)
        gridX = gridSize - 1;
      if (gridY < 0)
        gridY = 0;
      else if (gridSize <= gridY)
        gridY = gridSize - 1;
      int count = ++gridPixels[gridX + gridSize * gridY];
      if (count > maxCount)
        maxCount = count;
    }
    
    for (int i = 0; i < gridSize * gridSize; i++) {
      int count = gridPixels[i] * 255 / maxCount;
      count = 255 - count;
      gridPixels[i] = 0xff000000 |
                      count |
                      count << 8 |
                      count << 16;
    }
    
    return new RGBAImage(gridPixels, gridSize);
  }
  
  private void updatePixels() {
    RGBAImage rgbaImage = createRGBAImage();
    int[] gridPixels = rgbaImage.getPixels();
    
    image = createImage(new MemoryImageSource(gridSize, gridSize, gridPixels, 0, gridSize));
  }
  
  public void setCoords(double[][] value) {
    coords = value;
    minx = maxx = coords[0][0];
    miny = maxy = coords[0][1];

    for (int k = 1; k < coords.length; k++) {
      double[] point = coords[k];
      double x = point[0];
      double y = point[1];
      if (x < minx)
        minx = x;
      if (x > maxx)
        maxx = x;
      if (y < miny)
        miny = y;
      if (y > maxy)
        maxy = y;
    }
    minx -= (maxx - minx) / 20;
    maxx += (maxx - minx) / 20;
    miny -= (maxy - miny) / 20;
    maxy += (maxy - miny) / 20;
    updatePixels();
    repaint();
  }

  @Override
  public void update(Graphics g) {
    paintComponent(g);
  }
  
  private Image buffer;

  @Override
  public void paintComponent(Graphics g) {
    Dimension size = getSize();

    if (buffer == null ||
        buffer.getWidth(null) != size.width ||
        buffer.getHeight(null) != size.height)
      buffer = createImage(size.width, size.height);
    
    Graphics gr = buffer.getGraphics();
    
    gr.drawImage(image, 0, 0, size.width, size.height, this);
    
    if (drawThreshold && minx <= threshold && threshold <= maxx) {
      int x = (int) ((threshold - minx) * size.width / (maxx - minx));
      gr.setColor(Color.red);
      gr.drawLine(x, 0, x, size.height);
    }
    
    g.drawImage(buffer, 0, 0, null);
  }
}
