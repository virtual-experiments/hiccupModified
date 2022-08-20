package hicupp;

import hicupp.algorithms.AlgorithmParameters;

/**
 * Holds methods for detecting clusters in a set of points using
 * projection pursuit.
 */
public class Clusterer {
  /**
   * Returns the coordinates for the best projection axis for clustering
   * <code>points</code>.
   * @param projectionIndex See class {@link ProjectionIndexFunction}.
   * @param algorithmIndex See class {@link FunctionMaximizer}
   * @param monitor If not <code>null</code>,
   * this object is notified of milestones of the computation.
   * Also, this object can cancel the computation.
   * @exception CancellationException Thrown originally by the monitor object.
   */
  public static double[] findAxis(int projectionIndex,
                                  int algorithmIndex,
                                  SetOfPoints points,
                                  Monitor monitor,
                                  AlgorithmParameters parameters)
      throws NoConvergenceException, CancellationException {
    Function projectionIndexFunction = new ProjectionIndexFunction(projectionIndex,
                                                                   points);
    double[] arguments = FunctionMaximizer.maximize(projectionIndexFunction,
                                                    algorithmIndex,
                                                    monitor,
                                                    parameters);
    double[] axis = new double[points.getDimensionCount()];
    double sumOfSquares = 0.0;
    for (int j = 0; j < arguments.length; j++) {
      double argument = arguments[j];
      axis[j] = argument;
      sumOfSquares += argument * argument;
    }
    axis[arguments.length] = sumOfSquares < 1.0 ? Math.sqrt(1.0 - sumOfSquares) : 0.0;
    return axis;
  }

  public static double[] findAxis(int projectionIndex,
                                  SetOfPoints points,
                                  Monitor monitor)
          throws CancellationException, NoConvergenceException {
    return findAxis(projectionIndex,
            FunctionMaximizer.SIMPLEX_ALGORITHM_INDEX,
            points,
            monitor,
            null);
  }

  /**
   * Returns the value that separates the two clusters in the given ordered list of points on a line.
   */
  public static double split(double[] list) {
    
    final double PFACT2 = 1.0e-4;
    
    double sum = 0.0;
    for (int i = 0; i < list.length; i++)
      sum += list[i];
    
    double cutoff = 0.0;
    double max = -1.0;
    double lsum = 0.0;
    
    for (int l = 0; l < list.length - 1; l++) {
      lsum += list[l];
      double test = (list[l] - list[l + 1]) / list[l];
      if (Math.abs(test) >= PFACT2) {
        int r = list.length - l - 1;
        double rsum = sum - lsum;
        double m = (lsum * lsum / (l + 1)) + (rsum * rsum / r);
        if (m > max) {
          max = m;
          cutoff = list[l];
        }
      }
    }
    
    return cutoff;
  }
}
