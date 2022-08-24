package hicupp.algorithms.gd;

import hicupp.*;

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
    public static double[] maximize(Function function, Monitor monitor)
            throws NoConvergenceException, CancellationException {
        // parameters
        final int maxIterations = 100;
        final int numberOfSolutions = 15;
        final int maxEquals = 10;
        final boolean convergeAtMaxEquals = true;

        final double precision = 1e-4;
        final double h = 1e-4;
        final double learningRate = 0.01;

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

            // find gradient unit vector
            for (Solution solution : solutions) {
                if (!solution.isConverged()) {
                    final double[] x_current = solution.getX().clone();
                    final double fx_current = solution.getFx();
                    double[] gradient_current = new double[n];
                    double sum_of_squares = 0.0f;

                    for (int j = 0; j < n; j++) { // each axis
                        double[] x_new = x_current.clone();

                        x_new[j] += h;
                        double fx_new = wrapper.evaluate(x_new);
                        if (fx_new < fx_current) {  // go opposite
                            x_new[j] -= h * 2;
                            fx_new = wrapper.evaluate(x_new);
                        }

                        double gradient_axis = (fx_new - fx_current) / h;
                        gradient_current[j] = gradient_axis;
                        sum_of_squares += gradient_axis * gradient_axis;
                    }

                    for (int j = 0; j < n; j++) {   // normalise
                        gradient_current[j] = gradient_current[j] / Math.sqrt(sum_of_squares);
                    }
                    solution.setGradient(gradient_current);
                }
            }

            // find new solution
            for (Solution solution : solutions) {
                if (!solution.isConverged()) {
                    final double[] gradient = solution.getGradient().clone();
                    double[] x = solution.getX().clone();

                    for (int i = 0; i < n; i++) {
                        x[i] += gradient[i] * learningRate;

                        if (Math.abs(x[i]) > 1) solution.setConverged(true);  // out of bounds
                    }

                    solution.setX(x);
                    double fx = wrapper.evaluate(x);

                    if (Math.abs(fx - solution.getFx()) < precision)    // converged
                        solution.setConverged(true);

                    if (fx < 0) solution.setConverged(true);    // error
                    else solution.setFx(fx);
                }
            }

            // find best solution
            Solution newBest = solutions.stream()
                    .max(Comparator.comparingDouble(Solution::getFx))
                    .orElseThrow()
                    .clone();

            if (bestSolution.equals(newBest)) numberOfEquals++;
            else numberOfEquals = 0;

            if (monitor != null)
                monitor.writeLine("(iter = " + iteration + ") " + bestSolution);

            // check stop condition
            if (solutions.stream().allMatch(Solution::isConverged))
                allConverged = true;
            else if (convergeAtMaxEquals && numberOfEquals >= maxEquals)
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