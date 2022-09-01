package hicupp;

public class ArraySetOfPoints implements SetOfPoints {
  private final int dimensionCount;
  private final int pointCount;
  private final double[] matrix;
  
  public ArraySetOfPoints(int dimensionCount, double[] matrix) {
    this.dimensionCount = dimensionCount;
    pointCount = matrix.length / dimensionCount;
    this.matrix = matrix;
  }
  
  public int getDimensionCount() {
    return dimensionCount;
  }
  
  public int getPointCount() {
    return pointCount;
  }
  
  public PointIterator createIterator() {
    return new MatrixPointIterator();
  }
  
  private class MatrixPointIterator implements PointIterator {
    private int i = -dimensionCount;
    
    public boolean hasNext() {
      return (i + dimensionCount) < matrix.length;
    }
    
    public void next() {
      i += dimensionCount;
    }
    
    public double getCoordinate(int index) {
      return matrix[i + index];
    }
  }
}
