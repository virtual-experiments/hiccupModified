package hicupp;

import Jama.*;

public class MatrixTools {
  public static Matrix computeVariance(Matrix matrix) {
    int npoints = matrix.getRowDimension();
    int ndims = matrix.getColumnDimension();
    double[][] array = matrix.getArray();
    Matrix varianceMatrix = new Matrix(ndims, ndims);
    final double[][] variance = varianceMatrix.getArray();
    for (int i = 0; i < ndims; i++)
      for (int j = 0; j < ndims; j++) {
        double sum = 0.0;
        for (int k = 0; k < npoints; k++)
          sum += array[k][i] * array[k][j];
        variance[i][j] = sum;
      }
    return varianceMatrix;
  }
  
  public static double[] computeCenter(Matrix matrix) {
    double[] center = sumOfRows(matrix);
    int npoints = matrix.getRowDimension();
    for (int i = 0; i < center.length; i++)
      center[i] /= npoints;
    return center;
  }
  
  public static void multiply(double[] vector, double factor) {
    for (int i = 0; i < vector.length; i++)
      vector[i] *= factor;
  }
  
  public static void addToRows(Matrix matrix, double[] vector) {
    double[][] rows = matrix.getArray();
    int npoints = rows.length;
    int ndims = vector.length;
    for (int i = 0; i < npoints; i++) {
      double[] row = rows[i];
      for (int j = 0; j < ndims; j++)
        row[j] += vector[j];
    }
  }
  
  public static void subtractFromRows(Matrix matrix, double[] vector) {
    double[][] rows = matrix.getArray();
    int npoints = rows.length;
    int ndims = vector.length;
    for (int i = 0; i < npoints; i++) {
      double[] row = rows[i];
      for (int j = 0; j < ndims; j++)
        row[j] -= vector[j];
    }
  }
  
  public static Matrix setOfPointsToMatrix(SetOfPoints points) {
    int npoints = points.getPointCount();
    int ndims = points.getDimensionCount();
    
    double[][] array = new double[npoints][ndims];
    PointIterator iter = points.createIterator();
    for (int i = 0; i < npoints; i++) {
      iter.next();
      double[] row = array[i];
      for (int j = 0; j < ndims; j++)
        row[j] = iter.getCoordinate(j);
    }
    
    return new Matrix(array, npoints, ndims);
  }

  public static double[] sumOfRows(Matrix matrix) {
    double[][] array = matrix.getArray();
    int nrows = array.length;
    int ncols = array[0].length;
    double[] sum = new double[ncols];
    for (int i = 0; i < nrows; i++) {
      double[] row = array[i];
      for (int j = 0; j < ncols; j++)
        sum[j] += row[j];
    }
    return sum;
  }
}
