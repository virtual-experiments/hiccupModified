package hicupp.algorithms.sa;

import hicupp.algorithms.AlgorithmParameters;

public record SimulatedAnnealingParameters(int numberOfIterations, boolean convergeAtMaxEquals, int maxEquals) implements AlgorithmParameters {

    @Override
    public int numberOfIterations() {
        return numberOfIterations;
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
