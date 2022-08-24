package hicupp;

import hicupp.algorithms.*;
import hicupp.algorithms.ga.*;
import hicupp.algorithms.gd.GradientDescent;
import hicupp.algorithms.sa.*;

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
  public static final int ANNEALING_ALGORITHM_INDEX = 1;
  public static final int GENETIC_ALGORITHM_INDEX = 2;
  public static final int GRADIENT_ALGORITHM_INDEX = 3;

  public static String[] getAlgorithmNames() {
    return algorithmIndices;
  }

  public static double[] maximize(Function function, int algorithmIndex, Monitor monitor, AlgorithmParameters parameters)
          throws NoConvergenceException, CancellationException {
    return switch (algorithmIndex) {
      case 1 -> SimulatedAnnealing.maximize(function, monitor, parameters);
      case 2 -> GeneticAlgorithm.maximize(function, monitor, parameters);
      case 3 -> GradientDescent.maximize(function, monitor);
      default -> Simplex.maximize(function, monitor);
    };
  }
}
