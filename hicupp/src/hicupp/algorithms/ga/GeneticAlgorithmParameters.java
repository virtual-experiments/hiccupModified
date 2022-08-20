package hicupp.algorithms.ga;

import hicupp.algorithms.AlgorithmParameters;

public record GeneticAlgorithmParameters(int populationSize, int maxGenerations, int mutationsPerGen, int spawnsPerGen,
                                         boolean convergeAtMaxEquals, int maxEquals) implements AlgorithmParameters {

    @Override
    public int populationSize() {
        return populationSize;
    }

    @Override
    public int maxGenerations() {
        return maxGenerations;
    }

    @Override
    public int mutationsPerGen() {
        return mutationsPerGen;
    }

    @Override
    public int spawnsPerGen() {
        return spawnsPerGen;
    }

    @Override
    public boolean convergeAtMaxEquals() {
        return convergeAtMaxEquals;
    }

    @Override
    public int maxEquals() {
        return maxEquals;
    }
}
