package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import hicupp.*;
import hicupp.classify.*;
import hicupp.trees.*;

public class SplitView extends Label {
  private static final int histogramHeight = Toolkit.getDefaultToolkit().getScreenSize().height / 5;
  private static final int histogramWidth = histogramHeight * 2;

  private final ClassSplit classSplit;
  private final NodeView parent;
  private final NodeView leftChild, rightChild;
  private final HistogramView histogramView;
  private String[] parameterNames;
  private PointsPlotFrame pointsPlotFrame;
  private DecisionRule decisionRule;

  private NumberFormat equationNumberFormat = new DecimalFormat("##0.00");
  private int limitNumberOfTerms = -1; // -1 for no limit

  public void showPointsPlot() {
    if (pointsPlotFrame == null) {
      pointsPlotFrame = new PointsPlotFrame("Points Plot - Interactive Hicupp");
      updatePointsPlotCoords();
      pointsPlotFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          pointsPlotFrame.dispose();
          pointsPlotFrame = null;
        }
      });
    }
    pointsPlotFrame.setVisible(true);
  }

  public void showDecisionRule() {
    if (decisionRule == null) {
      decisionRule = new DecisionRule(parent.getClassNode().getNode().getSerialNumber());
      decisionRule.setTextArea(parameterNames, classSplit.getSplit());
      decisionRule.setVisible(true);

      decisionRule.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          super.windowClosing(e);
          decisionRule.setVisible(false);
          decisionRule = null;
        }
      });
    }
  }

  private void updatePointsPlotCoords() {
    if (pointsPlotFrame != null) {
      pointsPlotFrame.getPointsPlot().setCoords(PrincipalPlaneFinder.projectOntoPrincipalPlane(parent.getClassNode(), classSplit.getSplit().getAxis()));
      pointsPlotFrame.getPointsPlot().setThreshold(classSplit.getSplit().getThreshold());
    }
  }

  public NodeView getLeftChild() {
    return leftChild;
  }

  public NodeView getRightChild() {
    return rightChild;
  }

  public NodeView getParentNodeView() {
    return parent;
  }

  public SplitView(NodeViewFactory nodeViewFactory,
                   NodeView parent,
                   ClassSplit classSplit,
                   String[] parameterNames) {
    this.parent = parent;
    this.classSplit = classSplit;
    this.parameterNames = parameterNames;
    leftChild = nodeViewFactory.createNodeView(this, classSplit.getLeftChild());
    rightChild = nodeViewFactory.createNodeView(this, classSplit.getRightChild());
    histogramView = new HistogramView();

    classSplit.addObserver((observable, object) -> {
      updateText();
      histogramView.repaint();
      if (pointsPlotFrame != null)
        pointsPlotFrame.getPointsPlot().setThreshold(SplitView.this.classSplit.getSplit().getThreshold());
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        Dimension size = getSize();
        int x = e.getX();
        int y = e.getY();

        boolean inComponent = 0 <= x &&
                x <= size.width &&
                0 <= y &&
                y <= size.height;

        if (inComponent && (e.getButton() == MouseEvent.BUTTON3)) {
          final PopupMenu popupMenu = new PopupMenu();
          popupMenu.setFont(DocumentFrame.menuFont);

          final MenuItem showPointsPlotMenuItem = new MenuItem();
          final MenuItem showDecisionRuleMenuItem = new MenuItem();

          {
            showPointsPlotMenuItem.setLabel("Show Points Plot");
            showPointsPlotMenuItem.addActionListener(event -> showPointsPlot());
          }

          {
            showDecisionRuleMenuItem.setLabel("Show Decision Rule");
            showDecisionRuleMenuItem.addActionListener(event -> showDecisionRule());
          }

          popupMenu.add(showPointsPlotMenuItem);
          popupMenu.add(showDecisionRuleMenuItem);

          getParent().add(popupMenu);
          popupMenu.show(SplitView.this, x, y);
          getParent().remove(popupMenu);
        } else if (inComponent && (e.getButton() == MouseEvent.BUTTON1)) showDecisionRule();
      }
    });
  }

  public void newPoints() {
    updatePointsPlotCoords();
    leftChild.newPoints();
    rightChild.newPoints();
  }

  public void updateText() {
    setText(getEquationString(classSplit.getSplit()));
  }

  private String getEquationString(Split split) {
    StringBuilder buffer = new StringBuilder();
    double[] axis = split.getAxis();

    int numberOfTerms = (limitNumberOfTerms == -1 || axis.length < limitNumberOfTerms)?
            axis.length : limitNumberOfTerms;

    for (int i = 0; i < numberOfTerms; i++) {
      if (i != 0) buffer.append(" + ");

      buffer.append(equationNumberFormat.format(axis[i]));
      buffer.append(" * ");
      buffer.append(parameterNames[i]);
    }

    if (numberOfTerms < axis.length) buffer.append(" + ...");

    buffer.append(" < ");
    buffer.append(equationNumberFormat.format(split.getThreshold()));

    return buffer.toString();
  }

  public void addSubtreeToContainer(Container container) {
    container.add(this);
    if (classSplit.getSplit().isLeafSplit())
      container.add(histogramView);
    addSubtreeToContainer(leftChild, container);
    addSubtreeToContainer(rightChild, container);
  }

  public void layoutSubtree(int left, int top, int right) {
    {
      updateText();
      Dimension size = this.getPreferredSize();
      setSize(size);
      setLocation((left + right - size.width) / 2, top);
      int center = (left + right) / 2;
      int newTop = top + size.height + 5;
      layoutSubtree(leftChild, left, newTop, center);
      layoutSubtree(rightChild, center, newTop, right);
    }

    if (classSplit.getSplit().isLeafSplit()) {
      Dimension size = histogramView.getSize();
      Rectangle leftBounds = leftChild.getComponent().getBounds();
      histogramView.setLocation((left + right - size.width) / 2, leftBounds.y + leftBounds.height + 5);
    }
  }

  public void paintSubtree(Graphics g, NodeView parent) {
    Rectangle bounds = parent.getComponent().getBounds();
    int x = bounds.x + bounds.width / 2;
    int y = bounds.y + bounds.height / 2;
    Rectangle leftBounds = leftChild.getComponent().getBounds();
    int leftX = leftBounds.x + leftBounds.width / 2;
    int leftY = leftBounds.y + leftBounds.height / 2;
    Rectangle rightBounds = rightChild.getComponent().getBounds();
    int rightX = rightBounds.x + rightBounds.width / 2;
    int rightY = rightBounds.y + rightBounds.height / 2;
    g.drawLine(x, y, leftX, leftY);
    g.drawLine(x, y, rightX, rightY);

    paintSubtree(leftChild, g);
    paintSubtree(rightChild, g);
  }

  private class HistogramView extends Canvas {
    public HistogramView() {
      setBackground(Color.white);
      setSize(histogramWidth, histogramHeight);
      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          updateValue(e.getX());
        }
      });
      addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseDragged(MouseEvent e) {
          updateValue(e.getX());
        }
      });
    }

    private void updateValue(int x) {
      if (classSplit.getParent().getPointCount() > 0) {
        Histogram histogram = classSplit.getHistogram();
        Dimension size = getSize();
        double fraction = (double) x / (double) size.width;
        double value = (1.0 - fraction) * histogram.getMin() + fraction * histogram.getMax();
        classSplit.getSplit().setThreshold(value);
      }
    }

    public void paint(Graphics g) {
      if (classSplit.getParent().getPointCount() > 0) {
        Histogram histogram = classSplit.getHistogram();
        Dimension size = getSize();
        g.setColor(Color.black);
        g.drawRect(0, 0, size.width - 1, size.height - 1);
        // g.clearRect(0, 0, size.width, size.height);
        /*
        g.setColor(getBackground());
        g.fillRect(0, 0, size.width, size.height);
        */
        int classCount = histogram.getClassCount();
        // int step = size.width / classCount;
        int max = histogram.getMaxFrequency();
        if (max > 0) {
          // int x = step / 2;
          g.setColor(Color.blue);
          int maxHeight = size.height - 4;
          int y2 = size.height - 3;
          for (int i = 0; i < classCount; i++) {
            int classSize = histogram.getClassFrequency(i);
            int lineHeight = maxHeight * classSize / max;
            int x = (int) (size.width * (i + .5) / classCount);
            if (lineHeight > 0)
              g.drawLine(x, y2, x, y2 + 1 - lineHeight);
            // x += step;
          }
        }
        g.setColor(Color.red);
        int valueX;
        double value = classSplit.getSplit().getThreshold();
        if (value > histogram.getMax())
          valueX = size.width;
        else
          valueX = (int) ((float) size.width * (value - histogram.getMin())
                  / (histogram.getMax() - histogram.getMin()));
        g.drawLine(valueX, 0, valueX, size.height);
      }
    }
  }

  static void addSubtreeToContainer(NodeView nodeView, Container container) {
    container.add(nodeView.getComponent());
    SplitView child = nodeView.getChild();
    if (child != null)
      child.addSubtreeToContainer(container);
  }

  static void layoutSubtree(NodeView nodeView, int left, int top, int right) {
    Component c = nodeView.getComponent();
    Dimension size = c.getPreferredSize();
    c.setSize(size);
    c.setLocation((left + right - size.width) / 2, top);
    SplitView child = nodeView.getChild();
    if (child != null)
      child.layoutSubtree(left, top + size.height + 5, right);
  }

  static void paintSubtree(NodeView nodeView, Graphics g) {
    SplitView child = nodeView.getChild();
    if (child != null)
      child.paintSubtree(g, nodeView);
  }

  public void setEquationNumberFormat(int numberOfDecimalPoints) {
    String zeroes = "0".repeat(numberOfDecimalPoints);
    updateText();
    this.equationNumberFormat = new DecimalFormat("##0." + zeroes);
    if (leftChild != null) {
      if (leftChild.getChild() != null)
        leftChild.getChild().setEquationNumberFormat(numberOfDecimalPoints);

      if (rightChild.getChild() != null)
        rightChild.getChild().setEquationNumberFormat(numberOfDecimalPoints);
    }
  }

  /**
   * Sets the number of terms shown in the decision rule, including its children
   * @param limitNumberOfTerms Limit, or -1 for no limit
   */
  public void setLimitNumberOfTerms(int limitNumberOfTerms) {
    this.limitNumberOfTerms = limitNumberOfTerms;
    updateText();
    if (leftChild != null) {
      if (leftChild.getChild() != null)
        leftChild.getChild().setLimitNumberOfTerms(limitNumberOfTerms);

      if (rightChild.getChild() != null)
        rightChild.getChild().setLimitNumberOfTerms(limitNumberOfTerms);
    }
  }

  public void setParameterNames(String[] parameterNames) {
    this.parameterNames = parameterNames;
    updateText();
    if (leftChild != null) {
      if (leftChild.getChild() != null)
        leftChild.getChild().setParameterNames(parameterNames);

      if (rightChild.getChild() != null)
        rightChild.getChild().setParameterNames(parameterNames);
    }
  }
}
