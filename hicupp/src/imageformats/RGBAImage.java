package imageformats;

/**
 * An image where each pixel is described by four octets: red, green, blue, and alpha.
 * <p>
 * Alpha indicates transparency: if alpha is 0xff, then the pixel is opaque; if alpha is
 * 0, then the pixel is transparent.</p>
 * <p>
 * Each pixel's octets are packed into an <code>int</code> as follows:</p>
 * <pre>i == ((alpha << 24) | (red << 16) | (green << 8) | blue)</pre>
 * <p>The pixels are stored in an array with first the first line's pixels, then the
 * second line's pixels, etc.</p>
 */
public final class RGBAImage {
  private final int[] pixels;
  private final int width;

  public RGBAImage(int[] pixels, int width) {
    this.pixels = pixels;
    this.width = width;
  }
  
  public int[] getPixels() {
    return pixels;
  }
  
  public int getPixel(int x, int y) {
    return pixels[x + y * width];
  }
  
  public static int getAlpha(int pixelValue) {
    return (pixelValue >> 24) & 0xff;
  }
  
  public static int getRed(int pixelValue) {
    return (pixelValue >> 16) & 0xff;
  }
  
  public static int getGreen(int pixelValue) {
    return (pixelValue >> 8) & 0xff;
  }
  
  public static int getBlue(int pixelValue) {
    return pixelValue & 0xff;
  }
  
  public int getWidth() {
    return width;
  }
  
  public int getHeight() {
    return pixels.length / width;
  }
}
