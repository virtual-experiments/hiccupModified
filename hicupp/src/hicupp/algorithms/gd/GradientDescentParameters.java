package hicupp.algorithms.gd;

import hicupp.algorithms.AlgorithmParameters;

public record GradientDescentParameters(int maxIterations, int numberOfSolutions, boolean convergeAtMaxEquals,
                                        int maxEquals) implements AlgorithmParameters {
    @Override
    public int maxIterations() {
        return maxIterations;
    }

    @Override
    public int numberOfSolutions() {
        return numberOfSolutions;
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
