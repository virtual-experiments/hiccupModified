package imageformats;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

public class ImageViewer extends Canvas {
  private int width, height;
  private Image image;

  public ImageViewer(RGBAImage pixels) {
    width = pixels.getWidth();
    height = pixels.getHeight();
    image = createImage(new MemoryImageSource(width, height, pixels.getPixels(), 0, width));
  }
  
  public void update(Graphics g) {
    paint(g);
  }
  
  public void paint(Graphics g) {
    g.drawImage(image, 0, 0, this);
  }
  
  public Dimension getPreferredSize() {
    return new Dimension(width, height);
  }
}
