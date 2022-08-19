package hicupp.algorithms.ga;

import hicupp.CancellationException;
import hicupp.MonitoringFunctionWrapper;
import hicupp.algorithms.AlgorithmUtilities;

import java.util.ArrayList;

final class GeneticAlgorithmFunctions {

    /**
     * Generate random chromosomes
     * @param amount number of chromosomes generated
     * @param n argument
     * @param wrapper wrapper of function
     * @return array list of random chromosomes
     * @throws CancellationException throws when wrapper cancel called
     */
    public static ArrayList<Chromosome> generateChromosomes(int amount, int n, MonitoringFunctionWrapper wrapper)
            throws CancellationException {
        ArrayList<Chromosome> population = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            double[] x_new = AlgorithmUtilities.generateRandomAxis(n);

            Chromosome chromosome = new Chromosome(x_new);
            evaluate(wrapper, chromosome);

            population.add(chromosome);
        }

        return population;
    }

    /**
     * Crossover 2 chromosomes at random point
     * @param father chromosome 1
     * @param mother chromosome 2
     * @return child chromosome
     * @throws CancellationException thrown when monitor is cancelled / cannot create child
     */
    public static Chromosome crossover(Chromosome father, Chromosome mother, MonitoringFunctionWrapper wrapper)
            throws CancellationException {
        int n = father.getX().length;
        if (n != mother.getX().length) throw new RuntimeException("Mismatch gene length.");

        Chromosome child = father.clone();

        System.arraycopy(mother.getX(), 0, child.getX(), 0, n / 2);
        evaluate(wrapper, child);

        return child;
    }

    /**
     * Evaluates arguments and set fx via {@link Chromosome#setFx(double)}
     * @param wrapper Function wrapper to evaluate
     * @param chromosome Chromosome in question
     * @throws CancellationException thrown when monitor is cancelled
     */
    public static void evaluate(MonitoringFunctionWrapper wrapper, Chromosome chromosome) throws CancellationException {
        chromosome.setFx(wrapper.evaluate(chromosome.getX()));
    }

    /**
     * Create new random axis for chromosome
     * @param wrapper Function wrapper to evaluate after mutation
     * @throws CancellationException thrown when monitor is cancelled
     */
    public static void mutate(MonitoringFunctionWrapper wrapper, Chromosome chromosome) throws CancellationException {
        chromosome.setX(AlgorithmUtilities.generateRandomAxis(chromosome.getX().length));
        evaluate(wrapper, chromosome);
    }

}
