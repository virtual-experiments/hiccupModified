package imageformats;

public class BITMAPINFOHEADER {
  /**
   * The number of bytes this structure occupies in a byte array.
   */
  public static final int size = 40;
  
  /**
   * Specifies the width of the bitmap, in pixels.
   */
  public int biWidth;
  
  /**
   * Specifies the height of the bitmap, in pixels.
   * If biHeight is positive, the bitmap is a bottom-up DIB and its origin is the
   * lower left corner. If biHeight is negative, the bitmap is a top-down DIB and its
   * origin is the upper-left corner.
   */
  public int biHeight;
  
  /**
   * Specifies the number of bits per pixel.
   */
  public short biBitCount;
  
  /**
   * Specifies the type of compression for a compressed bottom-up bitmap
   * (top-down DIBs cannot be compressed).
   * Must be one of: BI_RGB, BI_xxx (see Platform SDK documentation).
   */
  public int biCompression;
  
  public static final int BI_RGB = 0;
  
  /**
   * Specifies the size, in bytes, of the image. This may be set to zero for BI_RGB
   * bitmaps.
   */
  public int biSizeImage;
  
  /**
   * Specifies the horizontal resolution, in pixels per meter, of the target device
   * for the bitmap.
   */
  public int biXPelsPerMeter;
  
  /**
   * Specifies the vertical resolution, in pixels per meter, of the target device
   * for the bitmap.
   */
  public int biYPelsPerMeter;
  
  /**
   * If this value is zero, the bitmap uses the maximum number of colors corresponding
   * to the value of the biBitCount member for the compression mode specified by
   * biCompression.
   */
  public int biClrUsed;
  
  /**
   * If this value is zero, all colors are required.
   */
  public int biClrImportant;
  
  public static boolean recognize(byte[] buffer, int offset) {
    return LittleEndian.readInt(buffer, offset) >= size;
  }
  
  public void read(byte[] buffer, int offset) {
    biWidth = LittleEndian.readInt(buffer, offset + 4);
    biHeight = LittleEndian.readInt(buffer, offset + 8);
    // WORD biPlanes
    biBitCount = LittleEndian.readShort(buffer, offset + 14);
    biCompression = LittleEndian.readInt(buffer, offset + 16);
    biSizeImage = LittleEndian.readInt(buffer, offset + 20);
    biXPelsPerMeter = LittleEndian.readInt(buffer, offset + 24);
    biYPelsPerMeter = LittleEndian.readInt(buffer, offset + 28);
    biClrUsed = LittleEndian.readInt(buffer, offset + 32);
    biClrImportant = LittleEndian.readInt(buffer, offset + 36);
  }
  
  public void write(byte[] buffer, int offset) {
    LittleEndian.writeInt(buffer, offset, size);
    LittleEndian.writeInt(buffer, offset + 4, biWidth);
    LittleEndian.writeInt(buffer, offset + 8, biHeight);
    LittleEndian.writeShort(buffer, offset + 12, (short) 1);
    LittleEndian.writeShort(buffer, offset + 14, biBitCount);
    LittleEndian.writeInt(buffer, offset + 16, biCompression);
    LittleEndian.writeInt(buffer, offset + 20, biSizeImage);
    LittleEndian.writeInt(buffer, offset + 24, biXPelsPerMeter);
    LittleEndian.writeInt(buffer, offset + 28, biYPelsPerMeter);
    LittleEndian.writeInt(buffer, offset + 32, biClrUsed);
    LittleEndian.writeInt(buffer, offset + 36, biClrImportant);
  }
}
