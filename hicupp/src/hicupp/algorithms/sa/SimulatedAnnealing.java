package hicupp.algorithms.sa;

import hicupp.*;
import hicupp.algorithms.AlgorithmParameters;
import hicupp.algorithms.AlgorithmUtilities;
import interactivehicupp.TextTools;

import java.util.Random;

public class SimulatedAnnealing {

    /**
     * Maximize a function using the Simulated Annealing method
     * @param monitor If not <code>null</code>, this object will be notified
     *                of milestones within the computation. The object is also
     *                given a chance to cancel the computation.
     * @param parameters must be an instance of {@link SimulatedAnnealingParameters}
     * @return An argument list for which the function is (sufficiently) maximal.
     * @exception NoConvergenceException If the algorithm fails to find a maximum.
     * @exception CancellationException Passed through from the <code>monitor</code>'s
     * {@link Monitor#continuing()} method.
     */
    public static double[] maximize(Function function, Monitor monitor, AlgorithmParameters parameters)
            throws NoConvergenceException, CancellationException {
        if (!(parameters instanceof SimulatedAnnealingParameters simulatedAnnealingParameters))
            throw new RuntimeException("Wrong parameters");

        // Simulated annealing variables
        double temperature = 1;
        final int numberOfIterations = simulatedAnnealingParameters.numberOfIterations();
        final boolean convergeAtMaxEquals = simulatedAnnealingParameters.convergeAtMaxEquals();
        final int maxEquals = simulatedAnnealingParameters.maxEquals();

        final MonitoringFunctionWrapper wrapper =
                new MonitoringFunctionWrapper(new CloningFunctionWrapper(function), monitor);
        final int n = function.getArgumentCount();

        // Initial random
        double[] x = AlgorithmUtilities.generateRandomArguments(n, 1);

        // Compute function values
        double fx = wrapper.evaluate(x);

        // Track best guess
        double[] x_best = x.clone();
        double fx_best = fx;
        double delta = Double.MAX_VALUE;
        int numberOfEquals = 0;

        for (int iteration = 1; iteration <= numberOfIterations; iteration++) {
            System.out.println();
            AlgorithmUtilities.printAxis(x_best, fx_best);

            if (monitor != null) {
                monitor.continuing();
                monitor.iterationStarted(iteration);
            }

            // Generate new vector from -temperature to temperature
            double[] vector = AlgorithmUtilities.generateRandomArguments(n, temperature);

            // Create new candidate
            double[] x_candidate = x.clone();
            for (int i = 0; i < n; i ++) {
                x_candidate[i] += vector[i];

                // Cap to abs 1
                if (x_candidate[i] > 1) x_candidate[i] = 1;
                if (x_candidate[i] < -1) x_candidate[i] = -1;
            }

            // Compute new fx
            double fx_candidate = wrapper.evaluate(x_candidate);

            if (fx_candidate > fx) { // accept new maximum
                fx = fx_candidate;
                x = x_candidate;
            } else {  // check with temperature
                Random random = new Random();
                double r = random.nextInt(1000) / 1000.0; // random probability 0 <= r <= 1

                if (r < Math.exp(-(fx_candidate - fx) / temperature)) { // accept worse guess
                    fx = fx_candidate;
                    x = x_candidate;
                }
            }

            // check converged
            double fx_best_old = fx_best;
            if (fx_candidate > fx_best) {
                fx_best = fx_candidate;
                x_best = x_candidate;
            }
            delta = fx_best - fx_best_old;
            System.out.println("Delta: " + delta);

            if (convergeAtMaxEquals) {
                if (delta <= 1e-4) numberOfEquals++;
                else numberOfEquals = 0;

                if (numberOfEquals >= maxEquals) break;
            }

            // log
            if (monitor != null)
                monitor.writeLine("(iter = " + iteration +
                        ") (fx = " + TextTools.formatScientific(fx) +
                        ") (fx_best = " + TextTools.formatScientific(fx_best) +
                        ") (x_best = {" + AlgorithmUtilities.argumentArrayToString(x_best) + "})");

            // decrease temperature
            temperature *= (1.0f - ((double) (iteration + 1)) / (double) numberOfIterations);
            System.out.println("Temperature: " + temperature);
        }

        if (delta > 1e-4)
            throw new NoConvergenceException("Did not converge.");

        x = x_best;
        fx = fx_best;

        System.out.println("\nOptimal value: " + fx);
        return x;
    }
}
