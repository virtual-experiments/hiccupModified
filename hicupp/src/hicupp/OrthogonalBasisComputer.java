package hicupp;

import Jama.*;

public class OrthogonalBasisComputer {
  /**
   * Returns a matrix containing an orthogonal basis for the subspace in which the
   * given points are, with the constraint that the first axis is the given axis1.
   * Each row of <code>points</code> is a point. Each row of the result is an
   * axis of the orthogonal basis.
   */
  public static Matrix computeOrthogonalBasis(double[] axis1, Matrix points) {
    final int ndims = axis1.length;
    
    double[][] matrix = new double[ndims + 1][];
    
    EigenvalueDecomposition eig = points.eig();
    double[][] v = eig.getV().getArray();
    
    matrix[0] = axis1;
    System.arraycopy(v, 0, matrix, 1, ndims);
    
    // Now, orthogonalize this matrix.
    return new Matrix(orthogonalize(matrix));
  }
  
  private static double[][] orthogonalize(double[][] matrix) {
    int nvects = matrix.length;
    int ndims = matrix[0].length;
    double[] sizes = new double[ndims];
    double[][] result = new double[ndims][];
    
    int k = 0;
    for (int i = 0; i < nvects; i++) {
      double[] oldAxis = matrix[i];
      double[] newAxis = i == 0 ? oldAxis : (double[]) oldAxis.clone();
      for (int j = 0; j < k; j++) {
        double[] axis = result[j];
        double p = dotProduct(oldAxis, axis);
        double q = sizes[j];
        add(newAxis, - (p / q), axis);
      }
      double size = dotProduct(newAxis, newAxis);
      if (size >= 1e-5) {
        sizes[k] = size;
        result[k] = newAxis;
        k++;
      }
    }
    
    double[][] realResult = new double[k][];
    System.arraycopy(result, 0, realResult, 0, k);
    return realResult;
  }
  
  private static double dotProduct(double[] x, double[] y) {
    double sum = 0.0;
    int ndims = x.length;
    for (int i = 0; i < ndims; i++)
      sum += x[i] * y[i];
    return sum;
  }
  
  private static void add(double[] from, double coefficient, double[] amount) {
    int ndims = from.length;
    for (int i = 0; i < ndims; i++)
      from[i] += coefficient * amount[i];
  }
}
