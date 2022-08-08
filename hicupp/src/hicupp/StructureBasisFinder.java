package hicupp;

import Jama.*;

public class StructureBasisFinder {
  public static Matrix computeStructureBasis(int projectionIndex,
                                             SetOfPoints points,
                                             int maxDimensionCount,
                                             Monitor monitor)
      throws NoConvergenceException, CancellationException {
    Matrix pointsMatrix = MatrixTools.setOfPointsToMatrix(points);
    double[] center = MatrixTools.computeCenter(pointsMatrix);
    MatrixTools.subtractFromRows(pointsMatrix, center);
    return computeStructureBasisIter(projectionIndex, pointsMatrix,
                                     Math.min(maxDimensionCount,
                                              points.getDimensionCount()),
                                     monitor);
  }
  
  private static Matrix computeStructureBasisIter(int projectionIndex,
                                                  Matrix points,
                                                  int dimensionCount,
                                                  Monitor monitor)
      throws NoConvergenceException, CancellationException {
    if (points.getColumnDimension() == 1)
      return new Matrix(1, 1, 1);
    
    double[] axis = Clusterer.findAxis(projectionIndex,
                                       FunctionMaximizer.SIMPLEX_ALGORITHM_INDEX,
                                       new MatrixSetOfPoints(points),
                                       monitor);
    if (dimensionCount > 1) {
      Matrix basis = OrthogonalBasisComputer.computeOrthogonalBasis(axis, points);
    
      final int realndims = basis.getRowDimension();
      final int ndimsm1 = realndims - 1;
    
      // Leave out the first basis vector, which is just the axis itself.
      Matrix t = basis.getMatrix(1, basis.getRowDimension() - 1,
                                 0, basis.getColumnDimension() - 1);
    
      Matrix tt = t.transpose();
      Matrix pointsInSubspace = points.times(tt);
    
      Matrix subspaceBasis = computeStructureBasisIter(projectionIndex,
                                                       pointsInSubspace,
                                                       dimensionCount - 1,
                                                       monitor);
      Matrix subspaceBasis2 = subspaceBasis.times(t);
    
      double[][] result = new double[subspaceBasis2.getRowDimension() + 1][];
      result[0] = axis;
      for (int i = 1; i < result.length; i++)
        result[i] = subspaceBasis2.getArray()[i - 1];
      return new Matrix(result);
    } else
      return new Matrix(new double[][] {axis});
  }
}
