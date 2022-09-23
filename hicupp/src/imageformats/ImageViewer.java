package imageformats;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class ImageViewer extends JPanel {
  private final int width, height;
  private final Image image;

  public ImageViewer(RGBAImage pixels) {
    width = pixels.getWidth();
    height = pixels.getHeight();
    image = createImage(new MemoryImageSource(width, height, pixels.getPixels(), 0, width));
  }

  @Override
  public void update(Graphics g) {
    paintComponent(g);
  }

  @Override
  public void paintComponent(Graphics g) {
    g.drawImage(image, 0, 0, this);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(width, height);
  }
}
