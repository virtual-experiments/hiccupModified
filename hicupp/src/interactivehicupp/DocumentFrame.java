package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class DocumentFrame extends Frame {
  public static final Font menuFont = new Font("MenuFont", Font.PLAIN, 14);

  private final String title;
  private final DocumentType documentType;
  private Document document;
  private int untitledCounter = 1;
  private File file;
  private boolean dirty;

  private final Menu fileMenu = new Menu();

  public DocumentFrame(DocumentType documentType, String title) {
    this.documentType = documentType;
    this.title = title;

    String documentTypeName = documentType.getCapitalizedName();

    fileMenu.setLabel("File");

    MenuItem fileNewMenuItem = new MenuItem();
    fileNewMenuItem.setLabel("New " + documentTypeName);
    fileNewMenuItem.addActionListener(e -> newDocument());

    MenuItem fileOpenMenuItem = new MenuItem();
    fileOpenMenuItem.setLabel("Open " + documentTypeName + "...");
    fileOpenMenuItem.addActionListener(e -> openDocument());

    MenuItem fileSaveMenuItem = new MenuItem();
    fileSaveMenuItem.setLabel("Save " + documentTypeName);
    fileSaveMenuItem.addActionListener(e -> saveDocument());

    MenuItem fileSaveAsMenuItem = new MenuItem();
    fileSaveAsMenuItem.setLabel("Save " + documentTypeName + " As...");
    fileSaveAsMenuItem.addActionListener(e -> saveDocumentAs());

    MenuItem fileRedrawItem = new MenuItem();
    fileRedrawItem.setLabel("Redraw " + documentTypeName);
    fileRedrawItem.addActionListener(e -> redraw());

    fileMenu.add(fileNewMenuItem);
    fileMenu.add(fileOpenMenuItem);
    fileMenu.add(fileSaveMenuItem);
    fileMenu.add(fileSaveAsMenuItem);
    fileMenu.add(fileRedrawItem);

    setDocument(documentType.createNewDocument());
    pack();

    addComponentListener(new FrameComponentAdapter());
  }

  private void setDocument(Document document) {
    this.document = document;
    MenuBar menuBar = new MenuBar();
    menuBar.setFont(menuFont);
    menuBar.add(fileMenu);
    document.addMenuBarItems(menuBar);
    setMenuBar(menuBar);
    document.addChangeListener(() -> {
      if (!dirty) {
        dirty = true;
        updateTitle();
      }
    });
    Component documentComponent = document.getComponent();
    removeAll();
    add(documentComponent, BorderLayout.CENTER);
    updateTitle();
  }

  private String getFileName() {
    String filename;
    if (file == null)
      if (untitledCounter == 1)
        filename = "(Untitled)";
      else
        filename = "(Untitled " + untitledCounter + ")";
    else
      filename = file.getName();
    return filename;
  }

  private void updateTitle() {
    setTitle(getFileName() + (dirty ? "*" : "") + " - " + title);
  }

  /**
   * Returns <code>false</code> if the user cancelled the operation,
   * and <code>true</code> otherwise.
   */
  public boolean askSaveIfDirty() {
    boolean continueOperation;
    String name = documentType.getName();

    if (dirty) {
      int result = MessageBox.showMessage(this,
              "The " + name + " has been modified. Save?",
              title,
              new String[] {"Yes", "No", "Cancel"});
      if (result == 0)
        continueOperation = saveDocument();
      else
        continueOperation = result == 1;
    } else
      continueOperation = true;
    return continueOperation;
  }

  private boolean saveDocument() {
    if (file == null)
      return saveDocumentAs();
    return saveDocument(file.toString());
  }

  private boolean saveDocument(String filename) {
    try {
      document.save(filename);
      file = new File(filename);
      dirty = false;
      updateTitle();
      return true;
    } catch (IOException e) {
      String name = documentType.getName();
      MessageBox.showMessage(this, "Could not save " + name + ": " + e, title);
      return false;
    }
  }

  private boolean saveDocumentAs() {
    String name = documentType.getCapitalizedName();
    FileDialog fileDialog = new FileDialog(this, "Save " + name + " As", FileDialog.SAVE);
    if (file != null && file.getParent() != null)
      fileDialog.setDirectory(file.getParent());
    fileDialog.setFile(getFileName());
    fileDialog.setVisible(true);
    if (fileDialog.getFile() == null)
      return false;
    else
      return saveDocument(new File(fileDialog.getDirectory(), fileDialog.getFile()).toString());
  }

  private void newDocument() {
    if (askSaveIfDirty()) {
      hideAllInfo(document.getRoot());
      file = null;
      untitledCounter++;
      TreeFileFormat.inputFileExists = false;
      setDocument(documentType.createNewDocument());
    }
  }

  private void openDocument() {
    if (askSaveIfDirty()) {
      hideAllInfo(document.getRoot());
      String capdName = documentType.getCapitalizedName();
      FileDialog fileDialog = new FileDialog(this, "Open " + capdName, FileDialog.LOAD);
      fileDialog.setVisible(true);
      if (fileDialog.getFile() != null) {
        File filename = new File(fileDialog.getDirectory(), fileDialog.getFile());
        try {
          Document document = documentType.loadDocument(filename.toString());
          dirty = false;
          file = filename;
          setDocument(document);
        } catch (IOException e) {
          String name = documentType.getName();
          MessageBox.showMessage(this, "Could not open " + name + " file: " + e, title);
        }
      }
    }
  }

  private void redraw() {
    document.redraw();
  }

  public static void hideAllInfo(NodeView nodeView) {
    if (nodeView != null) {
      nodeView.hideInfo();
      if (nodeView.getChild() != null) {
        hideAllInfo(nodeView.getChild().getLeftChild());
        hideAllInfo(nodeView.getChild().getRightChild());
      }
    }
  }

  private class FrameComponentAdapter extends ComponentAdapter {
    @Override
    public void componentResized(ComponentEvent e) {
      super.componentResized(e);
      hideAllInfo(document.getRoot());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      super.componentMoved(e);
      hideAllInfo(document.getRoot());
    }
  }
}