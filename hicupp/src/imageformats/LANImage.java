package imageformats;

import java.io.*;

public final class LANImage {
  private int bandsCount;
  private int width;
  private byte[] bytes;
  
  public LANImage(String filename) throws IOException {
    RandomAccessFile f = new RandomAccessFile(filename, "r");
    f.seek(8);
    bandsCount = f.read() | f.read() << 8;
    f.seek(16);
    width = f.read() | f.read() << 8;
    f.seek(20);
    int height = f.read() | f.read() << 8;
    bytes = new byte[bandsCount * width * height];
    f.seek(128);
    if (f.read(bytes) < bytes.length)
      throw new IOException("Premature end of file.");
    f.close();
  }
  
  public RGBAImage toRGBAImage() {
    if (bandsCount != 3)
      throw new IllegalStateException("Can only convert images with three bands.");
    int[] pixels = new int[bytes.length / 3];
    int i = 0;
    int g = 0;
    int r = width;
    int b = 2 * width;
    while (i < pixels.length) {
      int pixel = 0xff000000 |
                  ((bytes[r++] & 0xff) << 16) |
                  ((bytes[g++] & 0xff) << 8) |
                  (bytes[b++] & 0xff);
      pixels[i++] = pixel;
      if (i % width == 0) {
        r += 2 * width;
        g += 2 * width;
        b += 2 * width;
      }
    }
    return new RGBAImage(pixels, width);
  }
}
