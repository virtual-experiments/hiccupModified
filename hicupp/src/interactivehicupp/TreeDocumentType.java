package interactivehicupp;

import java.io.IOException;

public class TreeDocumentType implements DocumentType {
  private final PointsSourceType pointsSourceType;
  
  public TreeDocumentType(PointsSourceType pointsSourceType) {
    this.pointsSourceType = pointsSourceType;
  }
  
  public String getCapitalizedName() {
    return "Tree";
  }
  
  public String getName() {
    return "tree";
  }
  
  public Document createNewDocument() {
    return new TreeDocument(pointsSourceType);
  }
  
  public Document loadDocument(String filename) throws IOException {
    return new TreeDocument(pointsSourceType, filename);
  }
}
