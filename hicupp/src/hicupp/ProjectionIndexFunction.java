package hicupp;

import java.util.Stack;

/**
 * A function that computes a projection index for projection pursuit
 * using the method described in Friedman and Tukey.
 * <p>Reference:<br />
 * <i>A Projection Pursuit Algorithm for Exploratory Data Analysis.<br />
 * J. H. Friedman and J. W. Tukey<br />
 * IEEE Transactions on Computers, Vol C-23 No. 9<br />
 * September 1974, p. 881-890</i></p>
 * <p>Author:<br />
 * Doug Reese<br />
 * IFAS Statistics<br />
 * 410 Rolfs Hall<br />
 * University of Florida<br />
 * Gainesville, FL 32611</p>
 */
public class ProjectionIndexFunction implements Function {
  private static final Distance entropyDistance = new Distance() {
    public double evaluate(double fhat, double phi) {
      return (fhat < 1e-10 ? 0 : fhat * Math.log(fhat)) -
             phi * Math.log(phi);
    }
  };
  
  private static final Distance l1Distance = new Distance() {
    public double evaluate(double fhat, double phi) {
      return Math.abs(fhat - phi);
    }
  };
  
  private static final Distance hellingersDistance = new Distance() {
    public double evaluate(double fhat, double phi) {
      double d = Math.sqrt(fhat) - Math.sqrt(phi);
      return d * d;
    }
  };
  
  private static final Distance friedmansDistance = new Distance() {
    public double evaluate(double fhat, double phi) {
      double d = fhat - phi;
      return d * d / 2 / phi;
    }
  };
  
  private static final Distance hallsDistance = new Distance() {
    public double evaluate(double fhat, double phi) {
      double d = fhat - phi;
      return d * d;
    }
  };
  
  private static final Distance cooksDistance = new Distance() {
    public double evaluate(double fhat, double phi) {
      double d = fhat - phi;
      return d * d * phi;
    }
  };
  
  private static final String[] projectionIndices = {
    "Shape",
		"Mixed",
    "Entropy",
    "L1",
    "Hellinger's",
    "Friedman's",
    "Hall's",
    "Cook's"
  };
  
	public static final int MIXED_PROJECTION_INDEX = 1;
  public static final int FRIEDMANS_PROJECTION_INDEX = 5;

  public static String[] getProjectionIndexNames() {
    return projectionIndices;
  }
  
  private static final Distance[] distances = {
    entropyDistance,
    l1Distance,
    hellingersDistance,
    friedmansDistance,
    hallsDistance,
    cooksDistance
  };
  
  private int projectionIndex;
  private SetOfPoints points;
  
  public ProjectionIndexFunction(int projectionIndex, SetOfPoints points) {
    this.projectionIndex = projectionIndex;
    this.points = points;
  }
  
  public int getArgumentCount() {
    return points.getDimensionCount() - 1;
  }
  
  public double evaluate(double[] arguments) {
    
    final double HFACT  = 0.1;
    
    // Constrain the axis to be a unit vector.
    
    double sumOfSquares = 0.0;
    for (int i = 0; i < arguments.length; i++) {
      double argument = arguments[i];
      sumOfSquares += argument * argument;
    }
    if (sumOfSquares <= 1.0) {
      
      final double[] axis = new double[points.getDimensionCount()];
      for (int i = 0; i < arguments.length; i++)
        axis[i] = arguments[i];
      axis[arguments.length] = Math.sqrt(1.0 - sumOfSquares);
      
      // Project each data point onto the axis, giving a list of values.
      
      final double[] values = Projector.project(points, axis);
      
      final double mean;
      final double variance;
      final double standardDeviation;
      {
        double sum = 0.0;
        double sumsq = 0.0;
        for (int i = 0; i < values.length; i++) {
          sum += values[i];
          sumsq += values[i] * values[i];
        }
        mean = sum / values.length;
        variance = sumsq / values.length - mean * mean;
        standardDeviation = Math.sqrt(variance);
      }
      
			// if (projectionIndex != MIXED_PROJECTION_INDEX)
			for (int i = 0; i < values.length; i++)
			  values[i] = (values[i] - mean) / standardDeviation;
      
      if (projectionIndex == 0) {    // "Shape"
        final int n = values.length;
        
        double sum3 = 0.0;
        double sum4 = 0.0;
        
        for (int i = 0; i < n; i++) {
          double z = values[i];
          double z3 = z * z * z;
          sum3 += z3;
          sum4 += z3 * z;
        }
        
        final double skewness = sum3 * n / (n - 1) / (n - 2);
        final double kurtosis = sum4 * n * (n + 1) / (n - 1) / (n - 2) / (n - 3) -
                                3 * (n - 1) * (n - 1) / (n - 2) / (n - 3);
      
        final double k2 = variance;
        final double k3 = skewness;
        final double k4 = kurtosis;
        
        final double k2_3 = k2 * k2 * k2;
        
        return (k3 * k3 / k2_3 + k4 * k4 / k2_3 / k2 / 4) / 12;
			} else if (projectionIndex == 1) { // Mixed
				final int mixcomp = 2;
				final int iterationCount = 4;
				MixedModel mixedModel = MixedModel.iterate(mixcomp, values, iterationCount);
				mixedModel.dump();
				return -mixedModel.aic;
      } else {
          
        // Sort the projected points in ascending order. (Using QuickSort.)
      
        Sorter.quickSort(values);

        final double lowerQuantile = values[values.length / 4];
        final double upperQuantile = values[values.length * 3 / 4];
        final double interQuantileRange = upperQuantile - lowerQuantile;
      
        final double bandwidth = 1.06 *
                                 Math.min(1.0, interQuantileRange / 1.34) *
                                 Math.pow(values.length, -1d/5);
      
        final double xl = Math.max(Math.abs(values[0]),
                                   Math.abs(values[values.length - 1]));
      
        final int m = 100;
        final int M = 2 * m + 1;
        final double[] fhat = new double[M];
        final double[] Phi = new double[M];
        final double step = xl / m;
        final double x0 = -xl - step / 2;
      
        {
          double x = x0;
          int i0 = 0;
          
          for (int j = 0; j < M; j++) {
            
            while (i0 < values.length && values[i0] < x - 4 * bandwidth)
              i0++;
            
            double sum = 0.0;
            for (int i = i0; i < values.length && values[i] <= x + 4 * bandwidth; i++)
              sum += kernel((x - values[i]) / bandwidth);
            
            fhat[j] = sum / bandwidth;
            Phi[j] = kernel(x);
            
            x += step;
            
          }
        }
      
        final Distance distance = distances[projectionIndex - 2];
        final double[] integrand = new double[M];
        for (int j = 0; j < M; j++)
          integrand[j] = distance.evaluate(fhat[j], Phi[j]);
      
        return computeIntegral(M, step, integrand);
      }
    } else
      return -3.4e38;
  }
  
  private double computeIntegral(int n, double step, double[] f) {
    double sum = 0.0;
    for (int i = 1; i < n - 1; i++)
      sum += f[i];
    return (sum + (f[0] + f[n - 1]) / 2) * step;
  }
  
  private static double kernel(double z) {
    return 1 / Math.sqrt(2 * Math.PI) * Math.exp(z * z / -2);
  }
  
  private static double calculateTrimmedStandardDeviation(double[] values) {
    
    /* Compute the standard deviation of the (sorted) list "values"
     * ignoring the low and high entries. The number of entries to
     * trim for each end is determined by the proportion "TFACT".
     */
    
    final double TFACT  = 0.1;
    
    int ntrim = (int) (TFACT * values.length);
    double nleft = values.length - 2.0 * ntrim;
    double sum = 0.0;
    double sumsq = 0.0;
    for (int i = ntrim; i < values.length - ntrim; i++) {
      double value = values[i];
      sum += value;
      sumsq += value * value;
    }
    double q = sum / nleft;
    return Math.sqrt(sumsq / nleft - q * q);
  }
  
  private static double calculateDensity(double[] list, double horzn) {
    
    /* Compute the weighted nearness of list items within the horizon
     * "horzn" (equal points receive a weight equal to the horizon
     * value, unequal points receive a weight which decreases linearly
     * with increasing distance to zero at the horizon and beyond),
     * for all possible pairings of points.
     */
    
    // First, set up the vector of discrete "values" of "list".
    
    double[] value = new double[list.length];
    int[] noccur = new int[list.length];
    
    int count = 0;
    double curval = list[0];
    value[0] = list[0];
    noccur[0] = 1;
    
    for (int i = 1; i < list.length; i++) {
      if (list[i] != curval) {
        count++;
        value[count] = list[i];
        curval = list[i];
        noccur[count] = 1;
      } else {
        noccur[count]++;
      }
    }
    
    double dense = 0.0;
    for (int i = 0; i < count; i++) {
      for (int j = i + 1; j <= count; j++) {
        double dist = value[j] - value[i];
        if (dist > horzn)
          break;
        dense += noccur[i] * noccur[j] * (horzn - dist);
      }
    }
    for (int i = 0; i <= count; i++)
      dense += 0.5 * (noccur[i] * (noccur[i] - 1)) * horzn;
    
    return 2.0 * dense + list.length * horzn;
  }
}

interface Distance {
  double evaluate(double fhat, double phi);
}

interface TabularFunction {
  double evaluate(int index);
}