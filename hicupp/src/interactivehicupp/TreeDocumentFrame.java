package interactivehicupp;

import java.awt.*;
import java.awt.event.*;

public class TreeDocumentFrame {
  private static String[] pointsSourceTypeNames = {
    "Images",
    "General Point Sets"
  };
  private static PointsSourceType[] pointsSourceTypes = {
    new ImagePointsSourceType(),
    new GeneralPointsSourceType()
  };

  public static void main(String[] args) {
    showTreeDocumentFrame(0, true);
  }
  
  static void showTreeDocumentFrame(int pointsSourceTypeIndex, boolean allowSwitching) {
    PointsSourceType pointsSourceType = pointsSourceTypes[pointsSourceTypeIndex];
    String title = "Interactive Hicupp for " + pointsSourceTypeNames[pointsSourceTypeIndex];
    
    TreeDocumentType docType = new TreeDocumentType(pointsSourceType);
    final DocumentFrame frame = new DocumentFrame(docType, title);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (frame.askSaveIfDirty())
          System.exit(0);
      }
    });
    Menu fileMenu = frame.getMenuBar().getMenu(0);
    if (allowSwitching) {
      fileMenu.addSeparator();
      Menu pointsSourceTypesMenu = new Menu("Points Source Types");
      for (int i = 0; i < pointsSourceTypes.length; i++) {
        final int index = i;
        MenuItem item = new MenuItem(pointsSourceTypeNames[i]);
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (frame.askSaveIfDirty()) {
              frame.dispose();
              showTreeDocumentFrame(index, true);
            }
          }
        });
        pointsSourceTypesMenu.add(item);
      }
      fileMenu.add(pointsSourceTypesMenu);
    }
    fileMenu.addSeparator();
    MenuItem fileExitMenuItem = new MenuItem("Exit");
    fileExitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (frame.askSaveIfDirty())
          System.exit(0);
      }
    });
    fileMenu.add(fileExitMenuItem);
    frame.setVisible(true);
  }
}
