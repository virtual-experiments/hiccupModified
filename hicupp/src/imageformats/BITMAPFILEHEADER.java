package imageformats;

public class BITMAPFILEHEADER {
  /**
   * The number of bytes this structure occupies in a byte array.
   */
  public static final int size = 14;
  
  /**
   * Specifies the size, in bytes, of the bitmap file.
   */
  public int bfSize;
  
  /**
   * Specifies the offset, in bytes, from the BITMAPFILEHEADER structure to the bitmap
   * bits.
   */
  public int bfOffBits;

  public static boolean recognize(byte[] buffer, int offset) {
    return buffer[offset] == 0x42 && buffer[offset + 1] == 0x4D;
  }
    
  public void read(byte[] buffer, int offset) {
    bfSize = LittleEndian.readInt(buffer, offset + 2);
    
    bfOffBits = LittleEndian.readInt(buffer, offset + 10);
  }
  
  public void write(byte[] buffer, int offset) {
    LittleEndian.writeShort(buffer, offset, (short) 0x4D42);
    LittleEndian.writeInt(buffer, offset + 2, bfSize);
    LittleEndian.writeShort(buffer, offset + 6, (short) 0);
    LittleEndian.writeShort(buffer, offset + 8, (short) 0);
    LittleEndian.writeInt(buffer, offset + 10, bfOffBits);
  }
}
