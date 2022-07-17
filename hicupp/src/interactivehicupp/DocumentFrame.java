package interactivehicupp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class DocumentFrame extends Frame {
  private String title;
  private DocumentType documentType;
  private Document document;
  private Component documentComponent;
  private int untitledCounter = 1;
  private File file;
  private boolean dirty;
  
  private final Menu fileMenu = new Menu();
  private final MenuItem fileNewMenuItem = new MenuItem();
  private final MenuItem fileOpenMenuItem = new MenuItem();
  private final MenuItem fileSaveMenuItem = new MenuItem();
  private final MenuItem fileSaveAsMenuItem = new MenuItem();

  public DocumentFrame(DocumentType documentType, String title) {
    this.documentType = documentType;
    this.title = title;
    
    String documentTypeName = documentType.getCapitalizedName();

    Font menuFont = new Font("Default", Font.PLAIN, 14);

    fileMenu.setLabel("File");
    fileMenu.setFont(menuFont);
    fileNewMenuItem.setLabel("New " + documentTypeName);
    fileNewMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newDocument();
      }
    });
    fileOpenMenuItem.setLabel("Open " + documentTypeName + "...");
    fileOpenMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openDocument();
      }
    });
    fileSaveMenuItem.setLabel("Save " + documentTypeName);
    fileSaveMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveDocument();
      }
    });
    fileSaveAsMenuItem.setLabel("Save " + documentTypeName + " As...");
    fileSaveAsMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveDocumentAs();
      }
    });
    
    fileMenu.add(fileNewMenuItem);
    fileMenu.add(fileOpenMenuItem);
    fileMenu.add(fileSaveMenuItem);
    fileMenu.add(fileSaveAsMenuItem);
    
    setDocument(documentType.createNewDocument());
    pack();
  }
  
  private void setDocument(Document document) {
    this.document = document;
    MenuBar menuBar = new MenuBar();
    menuBar.add(fileMenu);
    document.addMenuBarItems(menuBar);
    setMenuBar(menuBar);
    document.addChangeListener(new DocumentChangeListener() {
      public void documentChanged() {
        if (!dirty) {
          dirty = true;
          updateTitle();
        }
      }
    });
    documentComponent = document.getComponent();
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
      MessageBox.showMessage(this, "Could not save " + name + ": " + e.toString(), title);
      return false;
    }
  }
  
  private boolean saveDocumentAs() {
    String name = documentType.getCapitalizedName();
    FileDialog fileDialog = new FileDialog(this, "Save " + name + " As", FileDialog.SAVE);
    if (file != null && file.getParent() != null)
      fileDialog.setDirectory(file.getParent().toString());
    fileDialog.setFile(getFileName());
    fileDialog.show();
    if (fileDialog.getFile() == null)
      return false;
    else
      return saveDocument(new File(fileDialog.getDirectory(), fileDialog.getFile()).toString());
  }
  
  private void newDocument() {
    if (askSaveIfDirty()) {
      file = null;
      untitledCounter++;
      setDocument(documentType.createNewDocument());
    }
  }
  
  private void openDocument() {
    if (askSaveIfDirty()) {
      String capdName = documentType.getCapitalizedName();
      FileDialog fileDialog = new FileDialog(this, "Open " + capdName, FileDialog.LOAD);
      fileDialog.show();
      if (fileDialog.getFile() != null) {
        File filename = new File(fileDialog.getDirectory(), fileDialog.getFile());
        try {
          Document document = documentType.loadDocument(filename.toString());
          dirty = false;
          file = filename;
          setDocument(document);
        } catch (IOException e) {
          String name = documentType.getName();
          MessageBox.showMessage(this, "Could not open " + name + " file: " + e.toString(), title);
        }
      }
    }
  }
}