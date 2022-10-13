package interactivehicupp;

import java.awt.*;
import java.awt.event.*;

import hicupp.*;

import javax.swing.*;

public class MonitorDialog extends JDialog implements Monitor {
  private final JLabel introLabel = new JLabel();
  private final JLabel iterationsLabel = new JLabel();
  private final JLabel evaluationsLabel = new JLabel();
  private final JButton cancelButton = new JButton();
  
  private Runnable computation;
  private JTextArea logTextArea;
  
  private volatile boolean cancellationRequested;
  private volatile int iterationCount;
  private volatile int evaluationCount;
  
  public MonitorDialog(Frame owner) {
    super(owner, "Interactive Hicupp", true);
    
    setLayout(new BorderLayout());
    add(introLabel, BorderLayout.NORTH);
    JPanel statisticsPanel = new JPanel();
    add(statisticsPanel, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel();
    add(buttonPanel, BorderLayout.SOUTH);
    
    introLabel.setText("Computing the split axis by maximizing the projection index function...");
    
    statisticsPanel.setLayout(new GridLayout(0, 2));
    JLabel iterationsLabelLabel = new JLabel();
    statisticsPanel.add(iterationsLabelLabel);
    statisticsPanel.add(iterationsLabel);
    JLabel evaluationsLabelLabel = new JLabel();
    statisticsPanel.add(evaluationsLabelLabel);
    statisticsPanel.add(evaluationsLabel);
    
    iterationsLabelLabel.setText("Iterations: ");
    iterationsLabel.setText("0");
    evaluationsLabelLabel.setText("Evaluations: ");
    evaluationsLabel.setText("0");
    
    buttonPanel.setLayout(new FlowLayout());
    buttonPanel.add(cancelButton);
    
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(e -> {
      cancellationRequested = true;
      introLabel.setText("Please wait while the computation is cancelled...");
      cancelButton.setEnabled(false);
    });
    
    pack();
    Dimension dialogSize = getSize();
    Dimension screenSize = getToolkit().getScreenSize();
    
    setLocation((screenSize.width - dialogSize.width) / 2,
                (screenSize.height - dialogSize.height) / 2);
    
    addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        new Thread(() -> {
          computation.run();
          postEvent(doneEventID);
        }).start();
      }
    });
  }
  
  public void processEvent(AWTEvent e) {
    if (e instanceof CustomEvent) {
      if (e.getID() == doneEventID)
        dispose();
      else {
        // assert(e.getID() == updateEventID);
        iterationsLabel.setText(Integer.toString(iterationCount));
        evaluationsLabel.setText(Integer.toString(evaluationCount));
      }
    } else
      super.processEvent(e);
  }
  
  private static final int doneEventID = AWTEvent.RESERVED_ID_MAX + 8473;
  private static final int updateEventID = AWTEvent.RESERVED_ID_MAX + 8474;
  
  private class CustomEvent extends AWTEvent {
    public CustomEvent(int id) {
      super(MonitorDialog.this, id);
    }
  }
  
  public void show(final Runnable computation, JTextArea logTextArea) {
    this.computation = computation;
    this.logTextArea = logTextArea;
    
    setVisible(true);
  }
  
  private void postEvent(int id) {
    getToolkit().getSystemEventQueue().postEvent(new CustomEvent(id));
  }

  @Override
  public void continuing() throws CancellationException {
    if (cancellationRequested)
      throw new CancellationException();
  }

  @Override
  public void iterationStarted(int iterationNumber) {
    iterationCount = iterationNumber;
    postEvent(updateEventID);
  }

  @Override
  public void evaluationStarted() {
    evaluationCount++;
    postEvent(updateEventID);
  }

  @Override
  public void writeLine(String text) {
    if (logTextArea != null)
      logTextArea.append(text + "\n");
  }

  public int getIterationCount() {
    return iterationCount;
  }
}
