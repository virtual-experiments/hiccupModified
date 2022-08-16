package hicupp;

import interactivehicupp.TextTools;

import java.util.Random;

/**
 * Holds a method for maximizing a function using the Simplex method of Nelder and Mead.
 */
public final class FunctionMaximizer {

  // simulated annealing,  genetic algorithms, gradient descent methods
  private static final String[] algorithmIndices = {
    "Simplex",
    "Simulated annealing",
    "Genetic algorithm",
    "Gradient descent"
  };

  public static final int SIMPLEX_ALGORITHM_INDEX = 0;

  public static String[] getAlgorithmNames() {
    return algorithmIndices;
  }

  public static double[] maximize(Function function, int algorithmIndex, Monitor monitor)
          throws NoConvergenceException, CancellationException {
    return switch (algorithmIndex) {
      case 1 -> annealing(function, monitor);
      case 2 -> genetic(function, monitor);
      case 3 -> gradient(function, monitor);
      default -> simplex(function, monitor);
    };
  }

  /**
   * Maximize a function using the Simplex method of Nelder and Mead.
   * <p>Reference:<br />
   * <i>A Simplex method of function minimization.<br />
   * The Computer Journal<br />
   * Vol. 7, p. 308, 1964.</i></p>
   * <p>Author:<br />
   * Art Smith<br />
   * IFAS Statistics<br />
   * 410 Rolfs Hall<br />
   * University of Florida<br />
   * Gainesville, FL 32611</p>
   * <p>Note:<br />
   * This routine is a symmetric reversal of Nelders published
   * algorithm which has been written for the projection pursuit problem.</p>
   * @param monitor If not <code>null</code>, this object will be notified
   *                of milestones within the computation. The object is also
   *                given a chance to cancel the computation.
   * @return An argument list for which the function is (sufficiently) maximal.
   * @exception NoConvergenceException If the algorithm fails to find a maximum.
   * @exception CancellationException Passed through from the <code>monitor</code>'s
   * {@link Monitor#continuing()} method.
   */
  private static double[] simplex(Function function, Monitor monitor)
      throws NoConvergenceException, CancellationException {
    final double RFACT = 1.0;
    final double CFACT  = 0.5;
    final double EFACT  = 2.0;
    final double EDGLEN = 5.0e-1;
    final double PFACT1 = 1.0e-2;
    
    final MonitoringFunctionWrapper wrapper =
      new MonitoringFunctionWrapper(new CloningFunctionWrapper(function), monitor);
    final int n = function.getArgumentCount();
    final int np1 = n + 1;
    
    double cscale = CFACT;
    double done = PFACT1;
    double escale = EFACT;
    double rscale = RFACT;
    double side = EDGLEN;
    
    // Force parameters to valid values if necessary.
    
    if (cscale <= 0.0 || cscale >= 1.0)
      cscale = 0.5;
    if (done <= 0.0)
      done = 1.0e-3;
    if (escale <= 1.0)
      escale = 2.0;
    if (rscale <= 0.0)
      rscale = 1.0;
    if (side <= 0.0)
      side = 0.1;

    // Compute initial simplex.
    final double[][] x = new double[np1][n];
    for (int i = 0; i < np1; i++) {
			/*
      double xlim = 1;
      for (int j = 0; j < n; j++) {
        x[i][j] = (2 * Math.random() - 1) * Math.sqrt(xlim);
        xlim -= x[i][j] * x[i][j];
      }
			*/
			double sumsq = 0;
			for (int j = 0; j < n; j++) {
				double v = 2 * Math.random() - 1;
				sumsq += v * v;
				x[i][j] = v;
			}
			double norm = Math.sqrt(sumsq);
			for (int j = 0; j < n; j++)
				x[i][j] /= norm;
    }
		
    
    // Compute function values.
    
    double[] fx = new double[np1];
    for (int i = 0; i < np1; i++)
      fx[i] = wrapper.evaluate(x[i]);
    
    int iter = 0;
    while (true) {
      iter++;

			for (int i = 0; i < x.length; i++) {
				double[] y = x[i];
				for (int j = 0; j < y.length; j++)
					System.out.print(y[j] + " ");
				System.out.println(" => " + fx[i]);
			}
			System.out.println();
			
      /*
      if (iter > 10000)
        throw new NoConvergenceException("Maximum iteration count exceeded.");
      */
    
      if (monitor != null) {
        monitor.continuing();
        monitor.iterationStarted(iter);
      }
      
      // Determine minimum and maximum values.
    
      double fxmax = fx[0];
      double fxmin = fx[0];
      int high = 0;
      int low = 0;
    
      for (int i = 0; i < np1; i++) {
        if (fx[i] < fxmin) {
          fxmin = fx[i];
          low = i;
        }
        if (fx[i] > fxmax) {
          fxmax = fx[i];
          high = i;
        }
      }
    
      // Compute the current centroid.
    
      double[] xcent = new double[n];
      for (int j = 0; j < n; j++) {
        double sum = 0.0;
        for (int i = 0; i < np1; i++)
          sum += x[i][j];
        sum -= x[low][j];
        xcent[j] = sum / n;
      }
      double fxcent = wrapper.evaluate(xcent);
    
      // Compute the reflected point.
    
      double[] xref = new double[n];
      for (int j = 0; j < n; j++)
        xref[j] = xcent[j] + rscale * (xcent[j] - x[low][j]);
      double fxref = wrapper.evaluate(xref);
    
      // Replace worst point x[low][j] with best new point.
    
      if (fxref > fxmax) {
        
        // Compute an expansion point.
        double[] xexp = new double[n];
        for (int j = 0; j < n; j++)
          xexp[j] = xcent[j] + escale * (xref[j] - xcent[j]);
        double fxexp = wrapper.evaluate(xexp);
        
        if (fxexp > fxref) {
          for (int j = 0; j < n; j++)
            x[low][j] = xexp[j];
          fx[low] = fxexp;
        } else {
          for (int j = 0; j < n; j++)
            x[low][j] = xref[j];
          fx[low] = fxref;
        }
      } else {
        boolean stop = false;
        for (int i = 0; i < np1; i++) {
          if (fxref > fx[i] && i != low) {
            stop = true;
            break;
          }
        }
        if (stop) {
          for (int j = 0; j < n; j++)
            x[low][j] = xref[j];
          fx[low] = fxref;
        } else {
          if (fxref > fxmin) {
            
            // We have a new worst point.

            for (int j = 0; j < n; j++)
              x[low][j] = xref[j];
            fx[low] = fxref;
            fxmin = fxref;
          }
          
          // Compute a contraction point.
          
          double[] xcon = new double[n];
          for (int j = 0; j < n; j++)
            xcon[j] = xcent[j] + cscale * (x[low][j] - xcent[j]);
          double fxcon = wrapper.evaluate(xcon);
          
          if (fxcon < fxmin) {
            for (int j = 0; j < n; j++) {
              for (int i = 0; i < np1; i++) {
                if (i != high)
                  x[i][j] = (x[i][j] + x[high][j]) * 0.5;
              }
            }
            for (int i = 0; i < np1; i++) {
              if (i != high)
                fx[i] = wrapper.evaluate(x[i]);
            }
          } else {
            for (int j = 0; j < n; j++)
              x[low][j] = xcon[j];
            fx[low] = fxcon;
          }
        }
      }
    
      // Have we reached an acceptable maximum?
    
      double convrg = 2.0 * Math.abs(fx[high] - fx[low]) /
                      (Math.abs(fx[high]) + Math.abs(fx[low]));
//      if (convrg <= done)
      
      if (monitor != null)
        monitor.writeLine("(iter = " + iter +
                          ") (fx[high] = " + TextTools.formatScientific(fx[high]) +
                          ") (convrg = " + TextTools.formatScientific(convrg) +
                          ") (x[high] = {" + argumentArrayToString(x[high]) + "})");
      
      if (convrg <= 1e-4)
        break;
    }
    
    int k = 1;
    double f = fx[1];
    for (int i = 0; i < np1; i++) {
      if (fx[i] > f) {
        f = fx[i];
        k = i;
      }
    }
    
		System.out.println("Optimal value: " + f);
		
    return x[k];
  }

  /**
   * Maximise a function using the Simulated Annealing method
   * @param monitor If not <code>null</code>, this object will be notified
   *                of milestones within the computation. The object is also
   *                given a chance to cancel the computation.
   * @return An argument list for which the function is (sufficiently) maximal.
   * @exception NoConvergenceException If the algorithm fails to find a maximum.
   * @exception CancellationException Passed through from the <code>monitor</code>'s
   * {@link Monitor#continuing()} method.
   */
  private static double[] annealing(Function function, Monitor monitor)
      throws NoConvergenceException, CancellationException {
    // Simulated annealing variables
    double temperature = 1;
    final double coolingRate = 0.001;

    final MonitoringFunctionWrapper wrapper =
            new MonitoringFunctionWrapper(new CloningFunctionWrapper(function), monitor);
    final int n = function.getArgumentCount();
    final int np1 = n + 1;

    // Initial random
    double[][] x = new double[np1][n];
    for (int i = 0; i < np1; i++) {
      double sum_square_root = 0;
      for (int j = 0; j < n; j++) {
        double v = 2 * Math.random() - 1;
        sum_square_root += v * v;
        x[i][j] = v;
      }

      double norm = Math.sqrt(sum_square_root);
      for (int j = 0; j < n; j++)
        x[i][j] /= norm;
    }

    // Compute function values
    double[] fx = new double[np1];
    for (int i = 0; i < np1; i++)
      fx[i] = wrapper.evaluate(x[i]);

    // Track best guess
    double[][] x_best = x.clone();
    double[] fx_best = fx.clone();
    double fx_best_max = fx[0];
    double convrg = 1;

    int iteration = 0;
    while (temperature > 0.1) {
      iteration++;

      for (int i = 0; i < x.length; i++) {
        double[] y = x[i];
        for (double v : y) System.out.print(v + " ");
        System.out.println(" => " + fx[i]);
      }
      System.out.println();

      if (monitor != null) {
        monitor.continuing();
        monitor.iterationStarted(iteration);
      }

      // Generate new vector from -1 to 1
      double[][] vector = new double[np1][n];
      for (int i = 0; i < np1; i++) {
        double sum_square_root = 0;
        for (int j = 0; j < n; j++) {
          double v = 2 * Math.random() - 1;
          sum_square_root += v * v;
          x[i][j] = v;
        }

        double norm = Math.sqrt(sum_square_root);
        for (int j = 0; j < n; j++)
          vector[i][j] /= norm;
      }

      // Create new candidate
      double[][] x_candidate = x.clone();
      for (int i = 0; i < np1; i++) {
        for (int j = 0; j < n; j ++) {
          x_candidate[i][j] += vector[i][j] * temperature;

          // Cap to abs 1
          if (x_candidate[i][j] > 1) x_candidate[i][j] = 1;
          if (x_candidate[i][j] < -1) x_candidate[i][j] = -1;
        }
      }

      // Compute new fx
      double[] fx_new = new double[np1];
      for (int i = 0; i < np1; i++)
        fx_new[i] = wrapper.evaluate(x_candidate[i]);

      // Check if accept
      double fx_max = fx[0];
      double fx_new_max = fx_new[0];
      double fx_new_min = fx_new[0];
      int high = 0;
      int low = 0;

      for (int i = 1; i < np1; i++) {
        if (fx[i] > fx_max) fx_max = fx[i];
        if (fx_new[i] > fx_new_max) {
          fx_new_max = fx_new[i];
          high = i;
        }
        if (fx_new[i] < fx_new_min) {
          fx_new_min = fx_new[i];
          low = i;
        }
      }

      if (fx_new_max > fx_max) { // accept new maximum
        fx = fx_new;
        x = x_candidate;

        // Track of best
        if (fx_best_max < fx_new_max) {
          fx_best_max = fx_new_max;
          fx_best = fx;
          x_best = x;
        }
      } else {  // check with temperature
        Random random = new Random();
        double r = random.nextInt(1000) / 1000.0; // random probability 0 <= r <= 1

        if (r < Math.exp(-(fx_new_max - fx_max) / temperature)) { // accept worse guess
          fx = fx_new;
          x = x_candidate;
        }
      }

      convrg = 2 * Math.abs(fx_best[high] - fx_best[low]) /
              (Math.abs(fx_best[high]) + Math.abs(fx_best[low]));

      if (monitor != null)
        monitor.writeLine("(iter = " + iteration +
                ") (fx[high] = " + TextTools.formatScientific(fx_best[high]) +
                ") (convrg = " + TextTools.formatScientific(convrg) +
                ") (x[high] = {" + argumentArrayToString(x_best[high]) + "})");

      if (convrg <= 1e-4)
        break;

      // decrease temperature
      temperature *= 1 - coolingRate;
      System.out.println("Temperature: " + temperature);
    }

    if (convrg > 1e-4)
      throw new NoConvergenceException("Solution did not converge");

    x = x_best;
    fx = fx_best;

    int k = 1;
    double f = fx[1];
    for (int i = 0; i < np1; i++) {
      if (fx[i] > f) {
        f = fx[i];
        k = i;
      }
    }

    System.out.println("Optimal value: " + f);
    return x[k];
  }

  private static double[] genetic(Function function, Monitor monitor)
          throws NoConvergenceException, CancellationException {

    throw new CancellationException("Genetic algorithm chosen.");
  }

  private static double[] gradient(Function function, Monitor monitor)
          throws NoConvergenceException, CancellationException {

    throw new CancellationException("Gradient descent chosen.");
  }

  private static String argumentArrayToString(double[] arguments) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < arguments.length; i++) {
      if (i > 0)
        buffer.append(", ");
      buffer.append(TextTools.formatScientific(arguments[i]));
    }
    
    return buffer.toString();
  }
}
