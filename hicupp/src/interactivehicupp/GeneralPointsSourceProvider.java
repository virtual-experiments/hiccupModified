package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import hicupp.*;
import hicupp.classify.*;
import hicupp.trees.*;

public class GeneralPointsSourceProvider implements PointsSourceProvider {
  private final Menu pointsMenu = new Menu();
  private final MenuItem pointsLoadPointsMenuItem = new MenuItem();
  private LoadMatrixDialog loadMatrixDialog;
  
  private final PointsSourceClient client;
  private final ClassTree classTree;
  private final GeneralNodeView root;
  
  private double[] coords;
  private int ndims;
  private SetOfPoints points;
  private String[] parameterNames;
  
  private class GeneralNodeView extends AbstractNodeView {
    private final Label component = new Label();
    
    public GeneralNodeView(SplitView parent, ClassNode classNode) {
      super(GeneralPointsSourceProvider.this.client, parent, classNode);
      initChild();
      initComponent();
      newPoints();
    }
    
    SplitView createChild() {
      return new SplitView(new NodeViewFactory() {
        public NodeView createNodeView(SplitView parent, ClassNode classNode) {
          return new GeneralNodeView(parent, classNode);
        }
      }, this, getClassNode().getChild(), parameterNames);
    }
    
    public Component getComponent() {
      return component;
    }
    
    public void newPoints() {
      super.newPoints();
      component.setText(getClassNode().getPointCount() + " points");
    }
  }

  private void generateDefaultMatrix() {
    coords = new double[] {
      0, 0, 1, 0, 0, 1, 1, 1, 2, 2,
      8, 8, 9, 9, 9, 10, 10, 9, 10, 10
    };
    ndims = 2;
    points = new ArraySetOfPoints(ndims, coords);
    generateDefaultParameterNames();
  }
  
  private void generateDefaultParameterNames() {
    parameterNames = new String[ndims];
    for (int i = 0; i < ndims; i++)
      parameterNames[i] = "p" + i;
  }
  
  private void loadPoints() {
    if (loadMatrixDialog == null)
      loadMatrixDialog = new LoadMatrixDialog(client.getFrame(),
                                              "Load Points from ASCII File");
    loadMatrixDialog.show();
    double[] coords = loadMatrixDialog.getCoords();
    if (coords != null) {
      int ndims = loadMatrixDialog.getColumnsCount();
      if (classTree.getRoot().getNode().getChild() != null && ndims != this.ndims)
        MessageBox.showMessage(client.getFrame(), "Cannot load points: number of dimensions incompatible with split rules in tree.",
                               "Interactive Hicupp");
      else {
        this.ndims = ndims;
        points = new ArraySetOfPoints(ndims, coords);
        generateDefaultParameterNames();
        classTree.setPoints(points);
      }
    }
  }
  
  public GeneralPointsSourceProvider(PointsSourceClient client, Tree tree) {
    this.client = client;
    
    pointsMenu.setLabel("Points");
    pointsMenu.add(pointsLoadPointsMenuItem);
    
    pointsLoadPointsMenuItem.setLabel("Load Points From ASCII File...");
    pointsLoadPointsMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadPoints();
      }
    });

    generateDefaultMatrix();
    
    classTree = new ClassTree(tree, points);
    root = new GeneralNodeView(null, classTree.getRoot());
  }

  @Override
  public NodeView getRoot() {
    return root;
  }

  @Override
  public void addMenuBarItems(MenuBar menuBar) {
    menuBar.add(pointsMenu);
  }

  @Override
  public String getSourceFile() {
    return null;
  }

  @Override
  public String getMetadata() {
    return null;
  }

  public void addNodePopupMenuItems(Menu nodePopupMenu) {
  }
  
  public String[] getParameterNames() {
    return parameterNames;
  }
}
