package hicupp.algorithms;

import hicupp.Function;
import hicupp.ProjectionIndexFunction;
import hicupp.SetOfPoints;
import interactivehicupp.TextTools;

public final class AlgorithmUtilities {

    /**
     * Generate a random number between -scalar and scalar to an array sized n
     * @param n number of arguments
     * @param scalar scalar of argument
     * @return 1D array with length n containing numbers between -scalar and scalar
     */
    public static double[] generateRandomArguments(int n, double scalar) {
        double sumsq = 0;
        double[] x = new double[n];

        for (int j = 0; j < n; j++) {
            double v = 2 * Math.random() - 1;
            sumsq += v * v;
            x[j] = v;
        }

        double norm = Math.sqrt(sumsq);
        for (int j = 0; j < n; j++) {
            x[j] /= norm;
            x[j] *= scalar;
        }

        return x;
    }

    public static String argumentArrayToString(double[] arguments) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0)
                buffer.append(", ");
            buffer.append(TextTools.formatScientific(arguments[i]));
        }

        return buffer.toString();
    }

    public static void printAxis(double[] x, double fx) {
        for (double v : x) System.out.print(v + " ");
        System.out.println(" => " + fx);
    }

    /**
     * Find the evaluation time of given set of points with chosen projection index
     * @param projectionIndex chosen projection index
     * @param points set of points of the node
     * @return time it takes to evaluate a single random input in milliseconds
     */
    public static long evaluationTime(int projectionIndex, SetOfPoints points) {
        Function projectionIndexFunction = new ProjectionIndexFunction(projectionIndex, points);
        double[] x = generateRandomArguments(projectionIndexFunction.getArgumentCount(), 1);

        long start = System.currentTimeMillis();
        projectionIndexFunction.evaluate(x);
        long end = System.currentTimeMillis();

        long duration = end - start;

        System.out.println("Current picture single evaluation time (ms): " + duration);

        return duration;
    }
}
