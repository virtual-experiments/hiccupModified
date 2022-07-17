package hicupp;

import Jama.*;

public class MatrixSetOfPoints implements SetOfPoints {
  private Matrix matrix;
  
  public MatrixSetOfPoints(Matrix matrix) {
    this.matrix = matrix;
  }
  
  public int getDimensionCount() {
    return matrix.getColumnDimension();
  }
  
  public int getPointCount() {
    return matrix.getRowDimension();
  }
  
  public PointIterator createIterator() {
    return new MatrixPointIterator();
  }
  
  private class MatrixPointIterator implements PointIterator {
    private int row = -1;
    
    public boolean hasNext() {
      return row < matrix.getRowDimension();
    }
    
    public void next() {
      row++;
    }
    
    public double getCoordinate(int index) {
      return matrix.get(row, index);
    }
  }
}
