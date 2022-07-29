package interactivehicupp;

import hicupp.trees.*;
import java.io.*;

public class TreeFileFormat {
  /* save file format
    Input file path
    Input file size
    Input file extension
    Dimensions
    Tree data ...
   */

  public static void saveTree(PointsSourceProvider pointsSourceProvider, String filename)
      throws IOException {
    Tree tree = pointsSourceProvider.getRoot().getClassNode().getNode().getTree();
    PrintWriter writer = new PrintWriter(new FileOutputStream(filename));

    // source file
    writer.println((pointsSourceProvider.getSourceFile() != null)?
            pointsSourceProvider.getSourceFile() : "N/A");

    // metadata
    writer.println(pointsSourceProvider.getMetadata());

    // number of dimensions
    int ndims = tree.getRoot().getChild() == null ? 0 : tree.getRoot().getChild().getAxis().length;
    writer.println(ndims);

    // write thresholds
    writeSplit(1, tree.getRoot().getChild(), writer);

    writer.close();
  }
  
  private static void writeSplit(int i, Split split, PrintWriter writer) {
    if (split != null) {
      writer.print(i);
      double[] axis = split.getAxis();
      for (double axi : axis) {
        writer.print(' ');
        writer.print(axi);
      }
      writer.print(' ');
      writer.print(split.getThreshold());
      writer.println();
      int iLeft = 2 * i;
      writeSplit(iLeft, split.getLeftChild().getChild(), writer);
      writeSplit(iLeft + 1, split.getRightChild().getChild(), writer);
    }
  }
  
  public static Tree loadTree(String filename) throws IOException {
    Reader reader = new BufferedReader(new FileReader(filename));
    StreamTokenizer t = new StreamTokenizer(reader);
    t.eolIsSignificant(true);

    readMetadata(t);

    // number of dimensions
    t.nextToken();
    if (t.ttype != StreamTokenizer.TT_NUMBER)
      syntaxError(t.lineno(), "The fourth line must state the number of dimensions.");
    int ndims = (int) t.nval;

    // EOL
    t.nextToken();
    if (t.ttype != StreamTokenizer.TT_EOL)
      syntaxError(t.lineno(), "End of line expected.");

    // data points
    t.nextToken();
    Tree tree = new Tree();
    readSubtree(tree.getRoot(), ndims, 1, t);
    reader.close();
    return tree;
  }
  
  private static void readSubtree(Node node, int ndims, int i, StreamTokenizer t)
      throws IOException {
    if (t.ttype == StreamTokenizer.TT_NUMBER && t.nval == i) {
      double[] axis = new double[ndims];
      for (int j = 0; j < ndims; j++)
        axis[j] = readNumber(t);
      double splitValue = readNumber(t);
      
      t.nextToken();
      if (t.ttype != StreamTokenizer.TT_EOL)
        syntaxError(t.lineno(), "End of line expected.");
      
      t.nextToken();
      int iLeft = 2 * i;
      
      node.split(axis, splitValue);
      Split child = node.getChild();
      readSubtree(child.getLeftChild(), ndims, iLeft, t);
      readSubtree(child.getRightChild(), ndims, iLeft + 1, t);
    }
  }

  public static boolean inputFileExists = false;
  public static StringBuilder filename = new StringBuilder();
  public static int fileSize = 0;
  public static StringBuilder fileExtension = new StringBuilder();

  private static void readMetadata( StreamTokenizer t) throws IOException {
    inputFileExists = false;

    // file path
    t.nextToken();
    filename = new StringBuilder();
    t.wordChars(':', ':');  // ensures all path tokens are taken into account
    t.wordChars('\\', '\\');

    while (t.ttype != StreamTokenizer.TT_EOL) {
      filename.append(t.sval);
      t.nextToken();
    }

    if (filename.toString().contains("\\")) { // check if a file path
      inputFileExists = true;
    }

    // file size (could be N/A)
    t.nextToken();
    if (t.ttype == StreamTokenizer.TT_NUMBER) fileSize = (int) t.nval;

    // find EOL
    while (t.ttype != StreamTokenizer.TT_EOL)
      t.nextToken();

    // file extension
    t.nextToken();
    fileExtension = new StringBuilder();
    while (t.ttype != StreamTokenizer.TT_EOL) {
      fileExtension.append(t.sval);
      t.nextToken();
    }
  }

  private static double readNumber(StreamTokenizer t) throws IOException {
    t.nextToken();
    if (t.ttype != StreamTokenizer.TT_NUMBER)
      syntaxError(t.lineno(), "Number expected.");
    return t.nval;
  } 
  
  private static void syntaxError(int lineNumber, String message) throws IOException {
    throw new IOException("Syntax error: line " + lineNumber + ": " + message);
  }
}
