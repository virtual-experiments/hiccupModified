package interactivehicupp;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class ImagesApplet extends Applet
{
		private final Button startButton = new Button("Start");
		
		public void init() {
				startButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
								ImagesMain.main(new String[0]);
						}
				});
				setLayout(new BorderLayout());
				add(startButton, BorderLayout.CENTER);
				validate();
		}
}
