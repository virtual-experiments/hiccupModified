package hicupp.algorithms.gd;

import hicupp.algorithms.AlgorithmUtilities;
import interactivehicupp.TextTools;

import java.util.Arrays;

class Solution implements Cloneable {
    private double[] x;
    private double fx;
    private double[] gradient;
    private boolean converged;
    private double learningRate;
    private double delta;

    public Solution (double[] x, double fx) {
        this.x = x;
        this.fx = fx;
        this.gradient = new double[x.length];
        this.converged = false;
        this.learningRate = 1e-6;
        this.delta = Double.MAX_VALUE;
    }

    public Solution(double[] x, double fx, double[] gradient, boolean converged, double learningRate, double delta) {
        this.x = x;
        this.fx = fx;
        this.gradient = gradient;
        this.converged = converged;
        this.learningRate = learningRate;
        this.delta = delta;
    }

    public double[] getX() {
        return x;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public double getFx() {
        return fx;
    }

    public void setFx(double fx) {
        this.fx = fx;
    }

    public double[] getGradient() {
        return gradient;
    }

    public void setGradient(double[] gradient) {
        this.gradient = gradient;
    }

    public boolean isConverged() {
        return converged;
    }

    public void setConverged(boolean converged) {
        this.converged = converged;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    @Override
    public Solution clone() {
        final double[] x = this.x.clone();
        final double fx = this.fx;
        final double[] gradient = this.gradient.clone();
        final boolean converged = this.converged;
        final double learningRate = this.learningRate;
        final double delta = this.delta;

        try {
            Solution clone = (Solution) super.clone();

            clone.setX(x);
            clone.setFx(fx);
            clone.setGradient(gradient);
            clone.setConverged(converged);
            clone.setLearningRate(learningRate);
            clone.setDelta(delta);

            return clone;
        } catch (CloneNotSupportedException e) {
            return new Solution(x, fx, gradient,  converged, learningRate, delta);
        }
    }

    @Override
    public String toString() {
        return "(fx = " + TextTools.formatScientific(fx) + ") " +
                "(gradient = " + AlgorithmUtilities.argumentArrayToString(gradient) + ") " +
                "(x = " + AlgorithmUtilities.argumentArrayToString(x) + ") " +
                "(delta = " + TextTools.formatScientific(delta) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Solution solution) {
            return Arrays.equals(solution.getX(), x) &&
                    (solution.getFx() == fx);
        } else return false;
    }
}
