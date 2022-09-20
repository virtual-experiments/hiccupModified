package hicupp.algorithms.gd;

import hicupp.CancellationException;
import hicupp.MonitoringFunctionWrapper;
import hicupp.algorithms.AlgorithmUtilities;

import java.util.ArrayList;

final class GradientDescentFunctions {
    public static ArrayList<Solution> generateRandomSolutions(int numberOfSolutions, int n, MonitoringFunctionWrapper wrapper)
            throws CancellationException {
        ArrayList<Solution> solutions = new ArrayList<>(numberOfSolutions);

        for (int i = 0; i < numberOfSolutions; i++)
            solutions.add(generateRandomSolution(n, wrapper));

        return solutions;
    }

    public static Solution generateRandomSolution(int n, MonitoringFunctionWrapper wrapper)
            throws CancellationException {
        double[] x = AlgorithmUtilities.generateRandomArguments(n, 1);
        double fx = wrapper.evaluate(x);

        return new Solution(x, fx);
    }

    public static void resetSolution(Solution solution, int n, MonitoringFunctionWrapper wrapper)
            throws CancellationException {
        System.out.println("Resetting solution " + solution);
        Solution newSolution = generateRandomSolution(n, wrapper);
        solution.setX(newSolution.getX());
        solution.setFx(newSolution.getFx());
        solution.setGradient(new double[n]);
        solution.setDelta(Double.MAX_VALUE);
    }

    public static void findGradient(Solution solution, MonitoringFunctionWrapper wrapper, int n, double h)
            throws CancellationException {
        if (!solution.isConverged()) {
            final double[] x_current = solution.getX().clone();
            final double fx_current = solution.getFx();
            double[] gradient_current = new double[n];

            for (int j = 0; j < n; j++) { // each axis
                double[] x_new = x_current.clone();

                x_new[j] += h;
                double fx_new = wrapper.evaluate(x_new);
                if (fx_new < fx_current || Math.abs(x_new[j]) > 1) {  // go opposite
                    x_new[j] -= h * 2;
                    fx_new = wrapper.evaluate(x_new);
                }

                double gradient_axis = (fx_new - fx_current) / h;
                gradient_current[j] = gradient_axis;
            }

            solution.setGradient(gradient_current);
        }
    }

    public static void findNewSolution(Solution solution, MonitoringFunctionWrapper wrapper,
                                       int n, double precision)
            throws  CancellationException {
        if (!solution.isConverged()) {
            final double[] gradient = solution.getGradient().clone();
            double[] x = solution.getX().clone();

            // add gradients
            for (int i = 0; i < n; i++) {
                double newAxis = x[i] + gradient[i] * solution.getLearningRate();
                int counter = 0;

                while (Math.abs(newAxis) > 1 && counter < 1000) {     // keep trying until in bounds
                    solution.setLearningRate(solution.getLearningRate() / 2);
                    newAxis = x[i] + gradient[i] * solution.getLearningRate();
                    counter++;
                }

                if (Math.abs(newAxis) > 1) {        // still out of bounds
                    System.out.println("Out of bounds");
                    resetSolution(solution, n, wrapper);
                    return;
                } else
                    x[i] = newAxis;
            }

            double newFx = wrapper.evaluate(x);
            solution.setDelta(Math.abs(newFx - solution.getFx()));

            if (solution.getDelta() < precision) {   // converged
                System.out.println("Solution converged");
                solution.setConverged(true);
            }

            if (newFx < 0) resetSolution(solution, n, wrapper);    // error
            else {
                if (newFx < solution.getFx())   // overstepped
                    solution.setLearningRate(solution.getLearningRate() / 2);

                solution.setX(x);
                solution.setFx(newFx);
            }
        }
    }
}
