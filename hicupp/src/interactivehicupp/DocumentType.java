package interactivehicupp;

import java.io.IOException;

public interface DocumentType {
  String getCapitalizedName();
  String getName();
  Document createNewDocument();
  Document loadDocument(String filename) throws IOException;
}
