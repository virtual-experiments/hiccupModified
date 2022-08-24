package hicupp.algorithms.gd;

import hicupp.algorithms.AlgorithmUtilities;
import interactivehicupp.TextTools;

import java.util.Arrays;

class Solution implements Cloneable {
    private double[] x;
    private double fx;
    private double[] gradient;
    private boolean converged;

    public Solution (double[] x, double fx) {
        this.x = x;
        this.fx = fx;
        this.gradient = new double[x.length];
        this.converged = false;
    }

    public Solution(double[] x, double fx, double[] gradient, boolean converged) {
        this.x = x;
        this.fx = fx;
        this.gradient = gradient;
        this.converged = converged;
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

    @Override
    public Solution clone() {
        final double[] x = this.x.clone();
        final double fx = this.fx;
        final double[] gradient = this.gradient.clone();
        final boolean converged = this.converged;

        try {
            Solution clone = (Solution) super.clone();

            clone.setX(x);
            clone.setFx(fx);
            clone.setGradient(gradient);
            clone.setConverged(converged);

            return clone;
        } catch (CloneNotSupportedException e) {
            return new Solution(x, fx, gradient,  converged);
        }
    }

    @Override
    public String toString() {
        return "(fx = " + TextTools.formatScientific(fx) + ") " +
                "(gradient = " + AlgorithmUtilities.argumentArrayToString(gradient) + ") " +
                "(x = " + AlgorithmUtilities.argumentArrayToString(x) + ") " +
                "(converged = " + isConverged() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Solution solution) {
            return Arrays.equals(solution.getX(), x) &&
                    (solution.getFx() == fx);
        } else return false;
    }
}
