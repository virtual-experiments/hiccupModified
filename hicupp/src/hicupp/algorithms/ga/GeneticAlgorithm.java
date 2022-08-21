package hicupp.algorithms.ga;

import hicupp.*;
import hicupp.algorithms.AlgorithmParameters;
import hicupp.algorithms.AlgorithmUtilities;
import interactivehicupp.TextTools;

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
    public static double[] maximize(Function function, Monitor monitor, AlgorithmParameters parameters)
            throws NoConvergenceException, CancellationException {
        // genetic parameters
        if (!(parameters instanceof GeneticAlgorithmParameters algorithmParameters))
            throw new RuntimeException("Wrong parameters type.");

        final int populationSize = algorithmParameters.populationSize();
        final int maxGenerations = algorithmParameters.maxGenerations();
        final int mutationsPerGen = algorithmParameters.mutationsPerGen();
        final int spawnsPerGen = algorithmParameters.spawnsPerGen();
        final boolean convergeAtMaxEquals = algorithmParameters.convergeAtMaxEquals();
        final int maxEquals = algorithmParameters.maxEquals();

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
            System.out.println("\nGeneration " + generation);
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
            System.out.println("Crossover population.");
            for (int j = 0; j < populationSize; j++) {
                Chromosome father = population.get(random.nextInt(populationSize));
                Chromosome mother = population.get(random.nextInt(populationSize));

                int counter = 0;
                while (father.equals(mother) && counter < populationSize) {
                    mother = population.get(random.nextInt(populationSize));
                    counter++;
                }

                if (father.equals(mother)) {
                    System.out.println("Could not find another unique chromosome, generating a random child.");
                    population.addAll(GeneticAlgorithmFunctions.generateChromosomes(1, n, wrapper));
                } else {
                    Chromosome child = GeneticAlgorithmFunctions.crossover(father, mother, wrapper);
                    population.add(child);
                }
            }
            System.out.println("Population size " + population.size());

            // mutate
            for (int j = 0; j < mutationsPerGen; j++) {
                GeneticAlgorithmFunctions.mutate(wrapper, population.get(random.nextInt(populationSize)));
            }
            System.out.println("Mutated " + mutationsPerGen + " chromosomes.");

            // spawn
            population.addAll(GeneticAlgorithmFunctions.generateChromosomes(spawnsPerGen, n, wrapper));
            System.out.println("Spawned new chromosomes. Now population " + population.size());

            // selection
            population.sort(Comparator.comparingDouble(Chromosome::getFx).reversed());  // sort in descending fx
            population = population.stream()
                    .limit(populationSize)
                    .collect(Collectors.toCollection(ArrayList::new));
            System.out.println("Selected fittest population. Now size " + population.size());

            // find fittest
            Chromosome oldFittest = fittest.clone();
            fittest = population.get(0).clone();
            boolean equal = fittest.equals(oldFittest);
            double delta = fittest.getFx() - oldFittest.getFx();

            System.out.println("Old fittest " + oldFittest +
                    " New fittest: " + fittest +
                    " Equal? " + equal);

            if (monitor != null)
                monitor.writeLine("(gen = " + generation + ")"
                        + fittest +
                        "(delta = " + TextTools.formatScientific(delta) + ")");

            // converging
            if (equal) noOfEquals++;
            else noOfEquals = 0;

            if ((noOfEquals == maxEquals && convergeAtMaxEquals) || // fittest stayed the same after multiple generations
                    (!equal) && (delta <= 1e-4))    // converged
                break;
        }

        System.out.println("\nOptimal value: " + fittest.getFx());
        return fittest.getX();
    }
}
