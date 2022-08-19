package hicupp.algorithms.ga;

import hicupp.*;
import hicupp.algorithms.AlgorithmUtilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public final class GeneticAlgorithm {

    /**
     * Maximize a function using the Genetic Algorithm method. A random population will
     * cross over, mutate, spawn, and selected.
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
        // genetic parameters
        final int populationSize = 10;
        final int maxGenerations = 200;
        final int mutationsPerGen = 2;
        final int spawnsPerGen = 10;
        final int maxEquals = 3;

        Random random = new Random();
        int noOfEquals = 0;

        final MonitoringFunctionWrapper wrapper =
                new MonitoringFunctionWrapper(new CloningFunctionWrapper(function), monitor);
        final int n = function.getArgumentCount();

        // Initial random population
        ArrayList<Chromosome> population = new ArrayList<>
                (GeneticAlgorithmFunctions.generateChromosomes(populationSize, n, wrapper));

        // keep track of fittest
        population.sort(Comparator.comparingDouble(Chromosome::getFx).reversed());
        Chromosome fittest = population.get(0);


        for (int generation = 1; generation <= maxGenerations; generation++) {
            System.out.println();
            {
                final double[] x = fittest.getX();
                double fx = fittest.getFx();
                AlgorithmUtilities.printAxis(x, fx);
            }

            if (monitor != null) {
                monitor.continuing();
                monitor.iterationStarted(generation);
            }

            // crossover population
            for (int j = 0; j < populationSize; j++) {
                Chromosome father = population.get(random.nextInt(populationSize));
                Chromosome mother = population.get(random.nextInt(populationSize));

                while (father.equals(mother)) mother = population.get(random.nextInt(populationSize));

                Chromosome child = GeneticAlgorithmFunctions.crossover(father, mother, wrapper);
                population.add(child);
            }

            // mutate
            for (int j = 0; j < mutationsPerGen; j++) {
                GeneticAlgorithmFunctions.mutate(wrapper, population.get(random.nextInt(populationSize)));
            }

            // spawn
            population.addAll(GeneticAlgorithmFunctions.generateChromosomes(spawnsPerGen, n, wrapper));

            // selection
            population.sort(Comparator.comparingDouble(Chromosome::getFx).reversed());
            population = population.stream()
                    .limit(populationSize)
                    .collect(Collectors.toCollection(ArrayList::new));

            // find fittest
            Chromosome oldFittest = fittest.clone();
            fittest = population.get(0);
            boolean equal = fittest.equals(oldFittest);
            double delta =  fittest.getFx() - oldFittest.getFx();

            System.out.println("Old fittest " + oldFittest +
                    " New fittest: " + fittest +
                    " Equal? " + equal);

            if (monitor != null)
                monitor.writeLine("(gen = " + generation + ")"
                        + fittest +
                        "(delta = " + delta + ")");

            // converging
            if (equal) noOfEquals++;
            else noOfEquals = 0;

            if ((noOfEquals == maxEquals) ||        // fittest stayed the same after multiple generations
                    (!equal) && (delta <= 1e-4))    // converged
                break;
            else if (generation == maxGenerations)
                throw new NoConvergenceException("Does not converge after " + maxGenerations + " generations.");
        }

        System.out.println("\nOptimal value: " + fittest.getFx());
        return fittest.getX();
    }
    
}
