package imageformats;

import java.io.*;

public class LANToBMP {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("LANToBMP -- Converts a LAN (ERDAS) file to BMP");
      System.err.println();
      System.err.println("Syntax: lantobmp LANFILE BMPFILE");
      return;
    }
    
    String lanFilename = args[0];
    String bmpFilename;
    if (args.length > 1)
      bmpFilename = args[1];
    else {
      if (lanFilename.endsWith(".lan"))
        bmpFilename = lanFilename.substring(0, lanFilename.length() - 3) + "bmp";
      else
        bmpFilename = lanFilename + ".bmp";
    }
    
    try {
      RGBAImage image = new LANImage(lanFilename).toRGBAImage();
      BMPFileFormat.writeImage(bmpFilename, image);
    } catch (IOException e) {
      System.err.println("Could not convert file:");
      e.printStackTrace();
    }
  }
}
