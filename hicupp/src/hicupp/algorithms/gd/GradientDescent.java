package hicupp.algorithms.gd;

import hicupp.*;
import hicupp.algorithms.AlgorithmParameters;

import java.util.ArrayList;
import java.util.Comparator;

public final class GradientDescent {

    /**
     * Maximize a function using the Gradient Descent method.
     * @param monitor If not <code>null</code>, this object will be notified
     *                of milestones within the computation. The object is also
     *                given a chance to cancel the computation.
     * @return An argument list for which the function is (sufficiently) maximal.
     * @exception NoConvergenceException If the algorithm fails to find a maximum.
     * @exception CancellationException Passed through from the <code>monitor</code>'s
     * {@link Monitor#continuing()} method.
     */
    public static double[] maximize(Function function, Monitor monitor, AlgorithmParameters parameters)
            throws NoConvergenceException, CancellationException {
        if (!(parameters instanceof GradientDescentParameters gradientDescentParameters))
            throw new RuntimeException("Wrong parameters");

        // parameters
        final int maxIterations = gradientDescentParameters.maxIterations();
        final int numberOfSolutions = gradientDescentParameters.numberOfSolutions();
        final boolean convergeAtMaxEquals = gradientDescentParameters.convergeAtMaxEquals();
        final int maxEquals = gradientDescentParameters.maxEquals();

        final double precision = 1e-4;
        final double h = 1e-4;

        // function variables
        final MonitoringFunctionWrapper wrapper =
                new MonitoringFunctionWrapper(new CloningFunctionWrapper(function), monitor);
        final int n = function.getArgumentCount();

        // random numberOfSolutions solutions
        ArrayList<Solution> solutions =
                GradientDescentFunctions.generateRandomSolutions(numberOfSolutions, n, wrapper);
        boolean allConverged = false;

        // keep track of best
        Solution bestSolution = solutions.get(0);

        int iteration = 1;
        int numberOfEquals = 0;
        while (iteration <= maxIterations && !allConverged) {

            if (monitor != null) {
                monitor.continuing();
                monitor.iterationStarted(iteration);
            }

            solutions.stream()
                     .filter(solution -> !solution.isConverged())
                     .forEach(System.out::println);
            System.out.println("Converges: " + solutions.stream().filter(Solution::isConverged).count() + "\n");

            for (Solution solution : solutions)
                GradientDescentFunctions.findGradient(solution, wrapper, n, h);

            for (Solution solution : solutions)
                GradientDescentFunctions.findNewSolution(solution, wrapper, n, precision);

            // find best solution
            Solution newBest = solutions.stream()
                    .max(Comparator.comparingDouble(Solution::getFx))
                    .orElseThrow()
                    .clone();

            if (bestSolution.getFx() >= newBest.getFx()) { // not improved, increase counter
                numberOfEquals++;
                System.out.println("Number of equals: " + numberOfEquals);
            } else {
                numberOfEquals = 0;
                System.out.println("Improved");
                bestSolution = newBest.clone(); // improvement
            }

            if (monitor != null)
                monitor.writeLine("(iter = " + iteration + ") " + bestSolution);

            // check stop condition
            if (solutions.stream().allMatch(Solution::isConverged))         // all solutions converged
                allConverged = true;
            else if (convergeAtMaxEquals && numberOfEquals >= maxEquals)    // has not improved in a while
                break;

            iteration++;
        }

        // check if any solution converged
        if (solutions.stream().noneMatch(Solution::isConverged))
            throw new NoConvergenceException("No solutions converged.");


        System.out.println("Optimal solution: " + bestSolution.getFx());
        return bestSolution.getX();
    }
}