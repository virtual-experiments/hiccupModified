package interactivehicupp;

import java.awt.*;
import java.awt.event.*;

public class MessageBox extends Dialog {
  public static void showMessage(Frame owner, String message, String title) {
    showMessage(owner, message, title, new String[] {"OK"});
  }
  
  public static int showMessage(Frame owner, String message, String title, String[] options) {
    MessageBox messageBox = new MessageBox(owner, message, title, options);
    messageBox.show();
    return messageBox.option;
  }
  
  private int option;
  
  private MessageBox(Frame owner, String message, String title, String[] options) {
    super(owner, title, true);
    add(new Label(message), BorderLayout.CENTER);
    Panel buttonPanel = new Panel(new FlowLayout());
    for (int i = 0; i < options.length; i++) {
      Button button = new Button(options[i]);
      final int index = i;
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          option = index;
          dispose();
        }
      });
      buttonPanel.add(button);
    }
    add(buttonPanel, BorderLayout.SOUTH);
    pack();
    Dimension size = getSize();
    Dimension screenSize = getToolkit().getScreenSize();
    setLocation((screenSize.width - size.width) / 2,
                (screenSize.height - size.height) / 2);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        option = -1;
        dispose();
      }
    });
  }
}