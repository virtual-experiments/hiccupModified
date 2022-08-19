package hicupp.algorithms;

import interactivehicupp.TextTools;

public final class AlgorithmUtilities {

    public static double[] generateRandomAxis(int n) {
        double sumsq = 0;
        double[] x = new double[n];

        for (int j = 0; j < n; j++) {
            double v = 2 * Math.random() - 1;
            sumsq += v * v;
            x[j] = v;
        }

        double norm = Math.sqrt(sumsq);
        for (int j = 0; j < n; j++)
            x[j] /= norm;

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

}
