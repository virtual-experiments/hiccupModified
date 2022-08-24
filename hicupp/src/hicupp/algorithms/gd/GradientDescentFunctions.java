package hicupp.algorithms.gd;

import hicupp.CancellationException;
import hicupp.MonitoringFunctionWrapper;
import hicupp.algorithms.AlgorithmUtilities;

import java.util.ArrayList;

final class GradientDescentFunctions {
    public static ArrayList<Solution> generateRandomSolutions(int numberOfSolutions, int n, MonitoringFunctionWrapper wrapper)
            throws CancellationException {
        ArrayList<Solution> solutions = new ArrayList<>(numberOfSolutions);

        for (int i = 0; i < numberOfSolutions; i++) {
            double[] x = AlgorithmUtilities.generateRandomArguments(n, 1);
            double fx = wrapper.evaluate(x);

            solutions.add(new Solution(x, fx));
        }

        return solutions;
    }
}
