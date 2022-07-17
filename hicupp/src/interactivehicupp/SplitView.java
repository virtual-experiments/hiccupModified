package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.Observable;
import java.util.Observer;

import hicupp.*;
import hicupp.classify.*;
import hicupp.trees.*;

public class SplitView extends Label {
  private static final int histogramWidth = 100;
  private static final int histogramHeight = 50;
  
  private final ClassSplit classSplit;
  private final NodeView parent;
  private final NodeView leftChild, rightChild;
  private final HistogramView histogramView;
  private final String[] parameterNames;
  private PointsPlotFrame pointsPlotFrame;

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
    pointsPlotFrame.show();
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

    classSplit.addObserver(new Observer() {
      public void update(Observable observable, Object object) {
        updateText();
        histogramView.repaint();
        if (pointsPlotFrame != null)
          pointsPlotFrame.getPointsPlot().setThreshold(SplitView.this.classSplit.getSplit().getThreshold());
      }
    });

    addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        Dimension size = getSize();
        int x = e.getX();
        int y = e.getY();
        boolean inComponent = 0 <= x &&
                              x <= size.width &&
                              0 <= y &&
                              y <= size.height;
        if (inComponent && (e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
          PopupMenu popupMenu = new PopupMenu();
          MenuItem showPointsPlotMenuItem = new MenuItem("Show Points Plot");
          showPointsPlotMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
              showPointsPlot();
            }
          });
          popupMenu.add(showPointsPlotMenuItem);
          getParent().add(popupMenu);
          popupMenu.show(SplitView.this, e.getX(), e.getY());
          getParent().remove(popupMenu);
        }
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

  private static final NumberFormat equationNumberFormat = new DecimalFormat("##0.00");
  
  private String getEquationString(Split split) {
    StringBuffer buffer = new StringBuffer();
    double[] axis = split.getAxis();
    for (int i = 0; i < axis.length; i++) {
      if (i != 0)
        buffer.append(" + ");
      buffer.append(equationNumberFormat.format(axis[i]));
      buffer.append(" * ");
      buffer.append(parameterNames[i]);
    }
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
}
