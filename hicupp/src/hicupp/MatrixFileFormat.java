package hicupp;

import java.io.*;
import java.util.*;

public class MatrixFileFormat {
  public static double[] readMatrix(String filename, boolean skipFirstLine, int[] columns) throws IOException {
    Vector lines = new Vector();
    
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    StreamTokenizer t = new StreamTokenizer(reader);
    t.eolIsSignificant(true);
    if (skipFirstLine) {
      do {
        t.nextToken();
        if (t.ttype == StreamTokenizer.TT_EOF)
          throw new IOException("Syntax error: end-of-file before first end-of-line");
      } while (t.ttype != StreamTokenizer.TT_EOL);
    }
    t.nextToken();
    
    while (true) {
      while (t.ttype == StreamTokenizer.TT_EOL)
        t.nextToken();
      if (t.ttype == StreamTokenizer.TT_EOF)
        break;
      double[] line = new double[columns.length];
      int column = 0;
      for (int i = 0; i < line.length; i++) {
        while (column < columns[i]) {
          t.nextToken();
          if (t.ttype == StreamTokenizer.TT_EOF || t.ttype == StreamTokenizer.TT_EOL)
            throw new IOException("Syntax error: line " + t.lineno() + ": could not read column " + (columns[i] + 1) + ": premature end of line");
          column++;
        }
        if (t.ttype != StreamTokenizer.TT_NUMBER)
          throw new IOException("Syntax error: line " + t.lineno() + ": column " + (column + 1) + ": end of line or not a number");
        line[i] = t.nval;
      }
      do
        t.nextToken();
      while (t.ttype != StreamTokenizer.TT_EOL && t.ttype != StreamTokenizer.TT_EOF);
      lines.addElement(line);
    }
    
    double[] matrix = new double[lines.size() * columns.length];
    int k = 0;
    for (int i = 0; i < lines.size(); i++) {
      double[] line = (double[]) lines.elementAt(i);
      for (int j = 0; j < columns.length; j++)
        matrix[k++] = line[j];
    }
    
    reader.close();
    
    return matrix;
  }
}
