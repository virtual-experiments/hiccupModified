package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import hicupp.*;
import hicupp.classify.*;
import hicupp.trees.*;
import imageformats.RGBAImage;

import javax.swing.*;

public class ImagePointsSourceProvider implements PointsSourceProvider {
  private static final float[] zoomFactors = {0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f};
  private static final String[] parameterNames = {"R", "G", "B"};

  public static final Color[] colors = {
          Color.black, Color.red, Color.green, Color.blue, Color.yellow, Color.cyan, Color.magenta, Color.white
  };

  public static final String[] colorNames = {
          "Black", "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta", "White"
  };

  private static final int initialOldMaskColorIndex = 4;
  private static final int initialNewMaskColorIndex = 7;

  private final JMenu viewMenu = new JMenu();
  private final JCheckBoxMenuItem zoomAutomaticMenuItem = new JCheckBoxMenuItem();
  private final JCheckBoxMenuItem[] zoomMenuItems = new JCheckBoxMenuItem[zoomFactors.length];
  private final JCheckBoxMenuItem zoomCustomMenuItem = new JCheckBoxMenuItem();
  private final JCheckBoxMenuItem viewAutomaticMaskColor = new JCheckBoxMenuItem();
  private final JMenuItem viewOldMaskColorMenuItem;
  private final JMenuItem viewNewMaskColorMenuItem;

  private final PointsSourceClient client;
  private final ClassTree classTree;
  private final ImageNodeView root;

  private int[] imagePixels;
  private int imageWidth;
  private int imageHeight;
  private float zoomFactor;
  private int displayImageWidth;
  private int displayImageHeight;
  private MemoryImageSource unscaledImageSource;
  private MemoryImageSource imageSource;
  private int oldMaskColor = colors[initialOldMaskColorIndex].getRGB();
  private int newMaskColor = colors[initialNewMaskColorIndex].getRGB();
  private int oldMaskIndex = initialOldMaskColorIndex;
  private int newMaskIndex = initialNewMaskColorIndex;

  private String chosenImageFile = null;
  private String metadata = "N/A\nN/A\nN/A";
  private boolean automaticColor;

  private final SetOfPoints points = new SetOfPoints() {
    public int getDimensionCount() {
      return 3;
    }

    public int getPointCount() {
      return imagePixels.length;
    }

    public PointIterator createIterator() {
      return new PixelIterator();
    }

    class PixelIterator implements PointIterator {
      private int index = -1;

      public boolean hasNext() {
        return index < imagePixels.length;
      }

      public void next() {
        index++;
      }

      public double getCoordinate(int index) {
        return (imagePixels[this.index] >> ((2 - index) << 3)) & 0xff;
      }
    }
  };

  private static void pixelToRgb(int pixel, double[] rgb) {
    rgb[0] = (pixel >> 16) & 0xff;
    rgb[1] = (pixel >> 8) & 0xff;
    rgb[2] = pixel & 0xff;
  }

  private class ImageNodeView extends AbstractNodeView {
    private class NodeViewComponent extends JPanel {
      @Override
      public void update(Graphics g) {
        paintComponent(g);
      }

      @Override
      public Dimension getPreferredSize() {
        return new Dimension(displayImageWidth + 4, displayImageHeight + 4);
      }

      @Override
      public void paintComponent(Graphics g) {
        if (image == null)
          updateImage(this);
        Dimension size = getSize();
        g.setColor(SystemColor.control);
        g.drawRect(0, 0, size.width - 1, size.height - 1);
        g.setColor(Color.black);
        g.drawRect(1, 1, size.width - 3, size.height - 3);
        g.drawImage(image, 2, 2, size.width - 4, size.height - 4, null);
      }
    }

    private final NodeViewComponent component = new NodeViewComponent();
    private Image image;
    private InspectorFrame inspectorFrame;
    private Inspector inspector;

    private void updateImage(Component c) {
      ClassNode classNode = getClassNode();
      ClassNode parentClassNode;
      setEvaluationTime();

      int oldMaskColor;
      if (classNode.getParent() == null) {
        parentClassNode = classNode;
        oldMaskColor = newMaskColor;
        ImagePointsSourceProvider.this.setAutomaticMaskColor();
      } else {
        parentClassNode = classNode.getParent().getParent();
        oldMaskColor = ImagePointsSourceProvider.this.oldMaskColor;
      }
      int[] pixels = new int[imageWidth * imageHeight];
      for (int i = 0; i < pixels.length; i++) {
        int color = parentClassNode.containsPointAtIndex(i) ?
                (classNode.containsPointAtIndex(i) ?
                        imagePixels[i] :
                        newMaskColor) :
                oldMaskColor;
        pixels[i] = color;
      }

      MemoryImageSource producer = new MemoryImageSource(imageWidth,
              imageHeight,
              pixels,
              0,
              imageWidth);
      image = c.createImage(producer);
    }

    public Component getComponent() {
      return component;
    }

    private class Inspector extends Canvas {
      public Inspector() {
        addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ScrollPane scrollPane = inspectorFrame.scrollPane;
            Dimension size = getSize();
            Dimension viewportSize = scrollPane.getViewportSize();
            Point scrollPosition = scrollPane.getScrollPosition();
            boolean zoomIn = e.getButton() == MouseEvent.BUTTON1;
            float zoomFactor = zoomIn ? 2.0f : 0.5f;
            int newWidth = (int) ((float) size.width * zoomFactor);
            if (newWidth < viewportSize.width) {
              newWidth = viewportSize.width;
              zoomFactor = (float) newWidth / size.width;
            }
            int newHeight = (int) ((float) size.height * zoomFactor);
            if (newHeight < viewportSize.height) {
              newHeight = viewportSize.height;
              zoomFactor = (float) newHeight / size.height;
              newWidth = (int) ((float) size.width * zoomFactor);
            }

            int newX = (int) ((float) e.getX() * zoomFactor);
            int newY = (int) ((float) e.getY() * zoomFactor);
            int deltaX = e.getX() - scrollPosition.x;
            int deltaY = e.getY() - scrollPosition.y;
            setSize(newWidth, newHeight);
            scrollPane.doLayout();
            scrollPane.setScrollPosition(newX - deltaX,
                    newY - deltaY);
          }
        });
      }

      @Override
      public void update(Graphics g) {
        paint(g);
      }

      @Override
      public void paint(Graphics g) {
        Dimension size = getSize();
        if (image == null)
          updateImage(this);
        g.drawImage(image, 0, 0, size.width, size.height, null);
      }
    }

    private class InspectorFrame extends Frame {
      public ScrollPane scrollPane = new ScrollPane();

      public InspectorFrame() {
        super("Node Inspector - Left Click Zoom In / Right Click Zoom Out");

        inspector = new Inspector();
        inspector.setSize(imageWidth, imageHeight);
        scrollPane.add(inspector);
        scrollPane.setSize(600, 600);
        add(scrollPane, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            dispose();
            inspectorFrame = null;
            inspector = null;
          }
        });

        addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            inspector.setSize(imageWidth, imageHeight);
          }
        });

        pack();
      }
    }

    public void inspect() {
      if (inspectorFrame == null)
        inspectorFrame = new InspectorFrame();
      inspectorFrame.setVisible(true);
    }

    public ImageNodeView(SplitView parent, ClassNode classNode) {
      super(ImagePointsSourceProvider.this.client, parent, classNode);
      initChild();
      initComponent();
      newImageSource();
      newPoints();
    }

    @Override
    SplitView createChild() {
      return new SplitView(ImageNodeView::new, this, getClassNode().getChild(), parameterNames);
    }

    @Override
    void addNodePopupMenuItems(JPopupMenu popupMenu) {
      final JMenuItem inspectMenuItem = new JMenuItem("Inspect");
      inspectMenuItem.addActionListener(e -> inspect());
      popupMenu.add(inspectMenuItem);
    }

    @Override
    public void newPoints() {
      super.newPoints();
      image = null;
      component.repaint();
      if (inspector != null)
        inspector.repaint();
    }

    public void newImageSource() {
      component.repaint();
      component.setSize(displayImageWidth + 4, displayImageHeight + 4);
      if (getChild() != null) {
        ((ImageNodeView) getChild().getLeftChild()).newImageSource();
        ((ImageNodeView) getChild().getRightChild()).newImageSource();
      }
    }

    @Override
    public void split() throws NoConvergenceException, CancellationException {
      super.split();
    }

    public void newMaskColors() {
      image = null;
      component.repaint();
      if (getChild() != null) {
        ((ImageNodeView) getChild().getLeftChild()).newMaskColors();
        ((ImageNodeView) getChild().getRightChild()).newMaskColors();
      }
    }
  }

  public ImagePointsSourceProvider(PointsSourceClient client,
                                   Tree tree) {
    this.client = client;
    generateDefaultImage();
    classTree = new ClassTree(tree, points);
    this.root = new ImageNodeView(null, classTree.getRoot());
    zoomFactor = 1.0f;
    displayImageWidth = imageWidth;
    displayImageHeight = imageHeight;
    updateImageSource();
    ColorUtils.initColorList();

    // main
    viewMenu.setText("View");
    JMenuItem viewChooseImageMenuItem = new JMenuItem();
    viewChooseImageMenuItem.setText("Choose Image...");
    viewChooseImageMenuItem.addActionListener(e -> chooseImage(ImagePointsSourceProvider.this.client.getFrame()));

    // mask color
    {
      RadioMenuTools.RadioMenuEventListener listener = index -> {
        oldMaskIndex = index;
        oldMaskColor = colors[index].getRGB();
        root.newMaskColors();
      };

      viewOldMaskColorMenuItem = RadioMenuTools.createRadioMenu(colorNames,
              initialOldMaskColorIndex,
              listener);
      viewOldMaskColorMenuItem.setText("Old Mask Color");
    }

    {
      RadioMenuTools.RadioMenuEventListener listener = index -> {
        newMaskIndex = index;
        newMaskColor = colors[index].getRGB();
        root.newMaskColors();
      };

      viewNewMaskColorMenuItem = RadioMenuTools.createRadioMenu(colorNames,
              initialNewMaskColorIndex,
              listener);
      viewNewMaskColorMenuItem.setText("New Mask Color");
    }

    {
      viewAutomaticMaskColor.addActionListener(e -> {
        automaticColor = !automaticColor;
        setAutomaticMaskColor();
      });
      viewAutomaticMaskColor.setState(false);
      automaticColor = false;
      viewAutomaticMaskColor.setText("Automatic Mask Color");
    }

    viewMenu.add(viewChooseImageMenuItem);
    JMenu viewZoomMenuItem = new JMenu();
    viewMenu.add(viewZoomMenuItem);
    viewMenu.add(viewAutomaticMaskColor);
    viewMenu.add(viewOldMaskColorMenuItem);
    viewMenu.add(viewNewMaskColorMenuItem);

    // zoom
    viewZoomMenuItem.add(zoomAutomaticMenuItem);
    zoomAutomaticMenuItem.setText("Automatic");
    zoomAutomaticMenuItem.setState(false);
    zoomAutomaticMenuItem.addActionListener(e -> setAutomaticZoom());
    viewZoomMenuItem.addSeparator();

    for (int i = 0; i < zoomMenuItems.length; i++) {
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(Float.toString(zoomFactors[i]));
      viewZoomMenuItem.add(item);
      zoomMenuItems[i] = item;
      final int index = i;

      if (zoomFactors[i] == 1.0f) item.setState(true);  // initial zoom

      item.addActionListener(e -> {
        for (int j = 0; j < zoomMenuItems.length; j++) {
          JCheckBoxMenuItem currentItem = zoomMenuItems[j];
          currentItem.setState(index == j);
        }

        resetCustomZoomLabel();
        setZoomFactor(zoomFactors[index]);
      });
    }

    viewZoomMenuItem.setText("Zoom");
    viewZoomMenuItem.addSeparator();
    viewZoomMenuItem.add(zoomCustomMenuItem);

    zoomCustomMenuItem.setText("Custom factor...");
    zoomCustomMenuItem.setState(false);
    zoomCustomMenuItem.addActionListener(e -> chooseCustomZoomFactor());
  }

  @Override
  public void addMenuBarItems(JMenuBar menuBar) {
    menuBar.add(viewMenu);
  }

  @Override
  public NodeView getRoot() {
    return root;
  }

  @Override
  public String getSourceFile() {
    return chosenImageFile;
  }

  @Override
  public String getMetadata() {
    return metadata;
  }

  @Override
  public String[] getParameterNames() {
    return parameterNames;
  }

  private static Frame getFrameAncestor(Component c) {
    while (!(c instanceof Frame))
      c = c.getParent();
    return (Frame) c;
  }

  @Override
  public void loadFile(String filename) {
    loadBMPFile(filename);
    chosenImageFile = filename;
    setMetadata();
  }

  private void chooseImage(Component c) {
    FileDialog dialog = new FileDialog(client.getFrame(), "Choose an Image", FileDialog.LOAD);
    dialog.setVisible(true);
    String file = dialog.getFile();
    if (file != null) {
      chosenImageFile = new File(dialog.getDirectory(), file).toString();
      setMetadata();
      loadBMPFile(chosenImageFile);
    }
  }

  private void loadBMPFile(String file) {
    try {
      imageformats.RGBAImage image = imageformats.BMPFileFormat.readImage(file);
      imagePixels = image.getPixels();
      imageWidth = image.getWidth();
      imageHeight = image.getHeight();
      classTree.setPoints(points);
      setAutomaticZoom();

      if (chosenImageFile != null) {
        JTextArea logTextArea = client.getLogTextArea();
        if (!logTextArea.getText().equals("")) logTextArea.append("\n");
        logTextArea.append("Loaded image " + chosenImageFile + "\n");
      }

    } catch (IOException e) {
      MessageBox.showMessage(client.getFrame(), "Could not load bitmap: " + e, "Interactive Hicupp");
    }
  }

  public void loadDefaultImage() {
    try {
      imageformats.RGBAImage image = imageformats.BMPFileFormat.loadDefaultImage();
      imagePixels = image.getPixels();
      imageWidth = image.getWidth();
      imageHeight = image.getHeight();
      classTree.setPoints(points);
      setAutomaticZoom();
    } catch (IOException e) {
      MessageBox.showMessage(client.getFrame(), "Could not load default image: " + e, "Interactive Hicupp");
    }
  }

  private void setZoomFactor(float value) {
    zoomAutomaticMenuItem.setState(false);

    zoomFactor = value;
    displayImageWidth = (int) (zoomFactor * (float) imageWidth);
    displayImageHeight = (int) (zoomFactor * (float) imageHeight);
    updateImageSource();
    root.newImageSource();
    client.layoutTree();
    hideAllInfo();
  }

  private void setAutomaticZoom() {
    zoomAutomaticMenuItem.setState(true);
    resetCustomZoomLabel();
    uncheckZoomItems();

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    displayImageHeight = Math.round(screenSize.height / 4.0f);
    double factor = (double) imageWidth / imageHeight;
    displayImageWidth = (int) Math.round(factor * displayImageHeight);
    updateImageSource();
    root.newImageSource();
    client.layoutTree();
    hideAllInfo();
  }

  private void chooseCustomZoomFactor() {
    final JDialog dialog = new JDialog(client.getFrame(), "Custom Zoom Factor", true);
    final JLabel label = new JLabel("Zoom factor: ");
    final JTextField textField = new JTextField(Float.toString(zoomFactor));
    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    final JButton okButton = new JButton("OK");
    final JButton cancelButton = new JButton("Cancel");

    dialog.setLayout(new BorderLayout());
    dialog.add(label, BorderLayout.WEST);
    dialog.add(textField, BorderLayout.CENTER);
    dialog.add(buttonsPanel, BorderLayout.SOUTH);
    buttonsPanel.add(okButton);
    buttonsPanel.add(cancelButton);

    cancelButton.addActionListener(e -> dialog.dispose());
    okButton.addActionListener(e -> {
      try {
        float newZoomFactor = Float.parseFloat(textField.getText());
        if (zoomFactor <= 0.0f)
          MessageBox.showMessage(client.getFrame(), "The zoom factor must be greater than zero.", "Interactive Hicupp");
        else {
          uncheckZoomItems();
          zoomCustomMenuItem.setState(true);
          zoomCustomMenuItem.setText("Custom factor: " + newZoomFactor);
          setZoomFactor(newZoomFactor);
          dialog.dispose();
        }
      } catch (NumberFormatException ex) {
        MessageBox.showMessage(client.getFrame(), "What you entered is not a number.", "Interactive Hicupp");
      }
    });

    dialog.pack();
    Dimension dialogSize = dialog.getSize();
    Dimension screenSize = client.getFrame().getToolkit().getScreenSize();
    dialog.setLocation((screenSize.width - dialogSize.width) / 2,
            (screenSize.height - dialogSize.height) / 2);
    dialog.setVisible(true);
  }

  private void generateDefaultImage() {
    final int width = 100;
    final int height = 100;

    imagePixels = new int[width * height];
    for (int i = 0; i < width; i++)
      for (int j = 0; j < height; j++)
        imagePixels[i + j * width] = 0xff000000 + 2 * i + 512 * j;
    imageWidth = width;
    imageHeight = height;
  }

  private void updateImageSource() {
    unscaledImageSource = new MemoryImageSource(imageWidth, imageHeight, imagePixels, 0, imageWidth);
    if (imageWidth == displayImageWidth && imageHeight == displayImageHeight)
      imageSource = unscaledImageSource;
    else {
      // Replace the ReplicateScaleFilter by an AveragingAreaScaleFilter for better quality.
      ImageFilter filter = new ReplicateScaleFilter(displayImageWidth, displayImageHeight);
      ImageProducer producer = new FilteredImageSource(unscaledImageSource, filter);
      int[] scaledImagePixels = new int[displayImageWidth * displayImageHeight];
      PixelGrabber pixelGrabber = new PixelGrabber(producer,
              0,
              0,
              displayImageWidth,
              displayImageHeight,
              scaledImagePixels,
              0,
              displayImageWidth);
      try {
        pixelGrabber.grabPixels();
      } catch (InterruptedException ignored) { }
      imageSource = new MemoryImageSource(displayImageWidth, displayImageHeight, scaledImagePixels, 0, displayImageWidth);
    }
  }

  private void setMetadata() {
    try {
      Path path = Paths.get(chosenImageFile);

      long kilobytes = Files.size(path) / 1024;
      String type = Optional.of(chosenImageFile)
              .filter(f -> f.contains("."))
              .map(f -> f.substring(chosenImageFile.lastIndexOf(".") + 1))
              .orElse("N/A");

      metadata = kilobytes + "\n" +
              type + "\n" +
              "N/A"; // 3rd line only for general points
    } catch (IOException | NullPointerException e) {
      metadata = "N/A\nN/A\nN/A";
      e.printStackTrace();
    }
  }

  private void uncheckZoomItems() {
    for (JCheckBoxMenuItem zoomMenuItem : zoomMenuItems) zoomMenuItem.setState(false);
  }

  private void resetCustomZoomLabel() {
    zoomCustomMenuItem.setState(false);
    zoomCustomMenuItem.setText("Custom factor...");
  }

  private void hideAllInfo() {
    DocumentFrame.hideAllInfo(root);
  }

  private void setAutomaticMaskColor() {
    viewAutomaticMaskColor.setState(automaticColor);
    viewOldMaskColorMenuItem.setEnabled(!automaticColor);
    viewNewMaskColorMenuItem.setEnabled(!automaticColor);

    if (automaticColor) {
      ClassNode classNode = root.getClassNode();

      // old mask
      {
        int red = (int) (0xff - Math.round(classNode.getMean(0)));
        int green = (int) (0xff - Math.round(classNode.getMean(1)));
        int blue = (int) (0xff - Math.round(classNode.getMean(2)));

        Color color = new Color(red, green, blue);
        String colorName = ColorUtils.getColorNameFromColor(color);
        if (colorName == null) {
          automaticMaskFail();
          return;
        } else {
          viewOldMaskColorMenuItem.setText("Old Mask Color: " + colorName);
          color = ColorUtils.getColorFromColorName(colorName);
          oldMaskColor = color.getRGB();
        }
      }

      // new mask
      {
        int red = (int) (0xff - (Math.round(classNode.getMean(0)) + RGBAImage.getRed(oldMaskColor)) / 2);
        int green = (int) (0xff - (Math.round(classNode.getMean(1)) + RGBAImage.getRed(oldMaskColor)) / 2);
        int blue = (int) (0xff - (Math.round(classNode.getMean(2)) + RGBAImage.getRed(oldMaskColor)) / 2);

        Color color = new Color(red, green, blue);
        String colorName = ColorUtils.getColorNameFromColor(color);
        if (colorName == null) {
          automaticMaskFail();
          return;
        } else {
          viewNewMaskColorMenuItem.setText("New Mask Color: " + colorName);
          color = ColorUtils.getColorFromColorName(colorName);
          newMaskColor = color.getRGB();
        }
      }

      if (newMaskColor == oldMaskColor) {
        automaticMaskFail();
        return;
      }

    } else {
      oldMaskColor = colors[oldMaskIndex].getRGB();
      newMaskColor = colors[newMaskIndex].getRGB();
      viewOldMaskColorMenuItem.setText("Old Mask Color");
      viewNewMaskColorMenuItem.setText("New Mask Color");
    }

    root.newMaskColors();
  }

  private void automaticMaskFail() {
    automaticColor = false;
    MessageBox.showMessage(client.getFrame(), "Unable to determine mask colors.", "Interactive Hicupp");
    setAutomaticMaskColor();
  }
}
