package imageformats;

public class BITMAPCOREHEADER {
  /**
   * Number of bytes occupied by this structure in a byte array.
   */
  public static final int size = 12;

  /**
   * Specifies the width of the bitmap, in pixels. (Unsigned.)
   */
  public short bcWidth;
  
  /**
   * Specifies the height of the bitmap, in pixels. (Unsigned.)
   */
  public short bcHeight;
  
  /**
   * Specifies the number of bits per pixel. This value must be 1, 4, 8, or 24.
   */
  public short bcBitCount;
  
  public static boolean recognize(byte[] buffer, int offset) {
    return LittleEndian.readInt(buffer, offset) == 12;
  }
  
  public void read(byte[] buffer, int offset) {
    bcWidth = LittleEndian.readShort(buffer, offset + 4);
    bcHeight = LittleEndian.readShort(buffer, offset + 6);
    bcBitCount = LittleEndian.readShort(buffer, offset + 10);
  }
  
  public void write(byte[] buffer, int offset) {
    LittleEndian.writeInt(buffer, offset, 12);
    LittleEndian.writeShort(buffer, offset + 4, bcWidth);
    LittleEndian.writeShort(buffer, offset + 6, bcHeight);
    LittleEndian.writeShort(buffer, offset + 8, (short) 1);
    LittleEndian.writeShort(buffer, offset + 10, bcBitCount);
  }
}
