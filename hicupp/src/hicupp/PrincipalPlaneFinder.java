package hicupp;

import Jama.*;

public class PrincipalPlaneFinder {
  /**
   * The principal plane is the plane defined by <code>axis</code> and
   * a vector in the hyperplane that is perpendicular to <code>axis</code>,
   * along which the <code>points</code> have greatest variance.
   * This method returns the projection of <code>points</code> onto the
   * principal plane.
   */
  public static double[][] projectOntoPrincipalPlane(SetOfPoints points, double[] axis) {
    final int npoints = points.getPointCount();
    final int ndims = axis.length;
    Matrix setOfPoints = MatrixTools.setOfPointsToMatrix(points);
    double[] center = MatrixTools.computeCenter(setOfPoints);
    MatrixTools.subtractFromRows(setOfPoints, center);
    Matrix basis = OrthogonalBasisComputer.computeOrthogonalBasis(axis, setOfPoints);
    
    final int realndims = basis.getRowDimension();
    final int ndimsm1 = realndims - 1;
    
    Matrix t = basis.getMatrix(1, realndims - 1, 0, ndims - 1);
    
    Matrix tt = t.transpose();
    Matrix pointsInSubspace = setOfPoints.times(tt);
    Matrix varianceMatrix = MatrixTools.computeVariance(pointsInSubspace);
    EigenvalueDecomposition eig = varianceMatrix.eig();
    
    double[] eigenvalues = eig.getRealEigenvalues();
    int max = 0;
    for (int i = 1; i < ndimsm1; i++)
      if (eigenvalues[i] > eigenvalues[max])
        max = i;
    Matrix v = eig.getV().transpose();
    Matrix principalAxisInSubspace = new Matrix(v.getArray()[max], 1);
    double[] principalAxis = principalAxisInSubspace.times(t).getArray()[0];
    double[][] projector = new double[][] {axis, principalAxis};
    Matrix projectionMatrix = new Matrix(projector, 2, ndims);
    Matrix projectionMatrixTranspose = projectionMatrix.transpose();
    Matrix centerMatrix = new Matrix(center, 1);
    double[][] result = setOfPoints.times(projectionMatrixTranspose).getArray();
    double[] resultCenter = centerMatrix.times(projectionMatrixTranspose).getRowPackedCopy();
    MatrixTools.addToRows(new Matrix(result), resultCenter);
    return result;
  }
}
