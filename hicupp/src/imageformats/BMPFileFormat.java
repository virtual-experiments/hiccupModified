package imageformats;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class BMPFileFormat {
  /**
   * Writes the image to a file in the BMP format.
   * The header consists of a BITMAPFILEHEADER, followed by a BITMAPCOREHEADER.
   */
  public static void writeImage(String filename, RGBAImage image) throws IOException {
    int width = image.getWidth();
    int height = image.getHeight();
    int scanSize = width * 3;
    int paddedScanSize = scanSize + (scanSize % 4 == 0 ? 0 : 4 - scanSize % 4);
    int fileSize = 128 + paddedScanSize * image.getHeight() * 3;

    BITMAPFILEHEADER fileHeader = new BITMAPFILEHEADER();
    fileHeader.bfSize = fileSize;
    fileHeader.bfOffBits = 128;
    
    BITMAPCOREHEADER coreHeader = new BITMAPCOREHEADER();
    coreHeader.bcWidth = (short) width;
    coreHeader.bcHeight = (short) height;
    coreHeader.bcBitCount = 24;
                       
    byte[] header = new byte[BITMAPFILEHEADER.size + BITMAPCOREHEADER.size];
    fileHeader.write(header, 0);
    coreHeader.write(header, fileHeader.size);
    
    RandomAccessFile f = new RandomAccessFile(filename, "rw");
    f.write(header);
    f.seek(fileHeader.bfOffBits);
    
    int[] pixels = image.getPixels();
    byte[] buffer = new byte[paddedScanSize];
    for (int k = pixels.length; k > 0; ) {
      k -= width;
      int i = k;
      for (int j = 0; j < scanSize; ) {
        int pixel = pixels[i++];
        buffer[j++] = (byte) pixel;
        buffer[j++] = (byte) (pixel >> 8);
        buffer[j++] = (byte) (pixel >> 16);
      }
      f.write(buffer);
    }
    
    f.close();
  }

  public static RandomAccessFile convertImage(String filename) throws IOException {
    File file = new File(filename);
    BufferedImage bufferedImage = ImageIO.read(file);

    if (bufferedImage == null) throw new IOException("Loaded a non image file.");

    System.out.println("Attempting to convert image.");
    ImageIO.write(bufferedImage, "bmp", new File("./temp.bmp"));
    System.out.println("BMP written successfully.");

    return new RandomAccessFile("./temp.bmp", "r");
  }

  public static RGBAImage readImage(String filename) throws IOException {
    RandomAccessFile file = new RandomAccessFile(filename, "r");
    
    byte[] fileHeaderBuffer = new byte[BITMAPFILEHEADER.size];
    file.readFully(fileHeaderBuffer);
    
    if (!BITMAPFILEHEADER.recognize(fileHeaderBuffer, 0)) { // convert first
//      throw new IOException("The file is not a BMP file.");
      file = convertImage(filename);
      fileHeaderBuffer = new byte[BITMAPFILEHEADER.size];
      file.readFully(fileHeaderBuffer);
      if (!BITMAPFILEHEADER.recognize(fileHeaderBuffer, 0)) // unable to convert
        throw  new IOException("Unable to convert to BMP file.");
    }
    
    BITMAPFILEHEADER fileHeader = new BITMAPFILEHEADER();
    fileHeader.read(fileHeaderBuffer, 0);
    
    byte[] infoHeaderBuffer = new byte[fileHeader.bfOffBits - BITMAPFILEHEADER.size];
    file.readFully(infoHeaderBuffer);
    
    boolean topDown;
    int width;
    int height;
    int bitCount;
    
    if (BITMAPCOREHEADER.recognize(infoHeaderBuffer, 0)) {
      BITMAPCOREHEADER coreHeader = new BITMAPCOREHEADER();
      coreHeader.read(infoHeaderBuffer, 0);
      
      topDown = false;
      width = coreHeader.bcWidth;
      height = coreHeader.bcHeight;
      bitCount = coreHeader.bcBitCount;
    } else 
      // Insert BITMAPV5HEADER, BITMAPV4HEADER here if desired.
      if (BITMAPINFOHEADER.recognize(infoHeaderBuffer, 0)) {
      BITMAPINFOHEADER infoHeader = new BITMAPINFOHEADER();
      infoHeader.read(infoHeaderBuffer, 0);
      
      topDown = infoHeader.biHeight < 0;
      width = infoHeader.biWidth;
      height = Math.abs(infoHeader.biHeight);
      bitCount = infoHeader.biBitCount;
      
      if (infoHeader.biCompression != BITMAPINFOHEADER.BI_RGB)
        throw new IOException("Unsupported compression method: " +
                              infoHeader.biCompression +
                              "; only uncompressed images supported.");
    } else
      throw new IOException("BMP file format version currently not supported.");
    
    RGBAImage image;
    if (bitCount == 24)
      image = read24BitImageBits(file, topDown, width, height);
    else if (bitCount == 32)
      image = read32BitImageBits(file, topDown, width, height);
    else
      throw new IOException("Unsupported number of bits per pixel: " + bitCount);
    
    file.close();
    
    return image;
  }
  
  private static RGBAImage read24BitImageBits(RandomAccessFile file,
                                              boolean topDown,
                                              int width,
                                              int height) throws IOException {
    int pixelCount = width * height;
    int[] pixels = new int[pixelCount];
    int scanLine = width * 3;
    int bufferLength = scanLine % 4 == 0 ? scanLine : scanLine + 4 - scanLine % 4;
    byte[] buffer = new byte[bufferLength];
    for (int line = 0; line < height; line++) {
      file.readFully(buffer);
      int i = (topDown ? line : (height - line - 1)) * width;
      for (int k = 0; k < scanLine; ) {
        int pixel = (buffer[k++] & 0xff) | ((buffer[k++] & 0xff) << 8) | (buffer[k++] << 16) | 0xff000000;
        pixels[i++] = pixel;
      }
    }
    
    return new RGBAImage(pixels, width);
  }
  
  private static RGBAImage read32BitImageBits(RandomAccessFile file,
                                              boolean topDown,
                                              int width,
                                              int height) throws IOException {
    int pixelCount = width * height;
    int[] pixels = new int[pixelCount];
    int bufferLength = width * 4;
    byte[] buffer = new byte[bufferLength];
    for (int line = 0; line < height; line++) {
      file.readFully(buffer);
      int i = (topDown ? line : (height - line - 1)) * width;
      for (int k = 0; k < bufferLength; ) {
        int pixel = (buffer[k++] & 0xff) | ((buffer[k++] & 0xff) << 8) | (buffer[k++] << 16) | 0xff000000;
        pixels[i] = pixel;
        k++;
      }
    }
    
    return new RGBAImage(pixels, width);
  }
}
