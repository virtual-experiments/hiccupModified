package imageformats;

public class LittleEndian {
  public static short readShort(byte[] buffer, int offset) {
    return (short) ((buffer[offset] & 0xff) | (buffer[offset + 1] << 8));
  }
  
  public static int readInt(byte[] buffer, int offset) {
    return (buffer[offset] & 0xff) |
           ((buffer[offset + 1] & 0xff) << 8) |
           ((buffer[offset + 2] & 0xff) << 16) |
           (buffer[offset + 3] << 24);
  }
  
  public static void writeShort(byte[] buffer, int offset, short value) {
    buffer[offset] = (byte) value;
    buffer[offset + 1] = (byte) (value >> 8);
  }
  
  public static void writeInt(byte[] buffer, int offset, int value) {
    buffer[offset] = (byte) value;
    buffer[offset + 1] = (byte) (value >> 8);
    buffer[offset + 2] = (byte) (value >> 16);
    buffer[offset + 3] = (byte) (value >> 24);
  }
}
