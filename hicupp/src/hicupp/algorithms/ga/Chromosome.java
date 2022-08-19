package hicupp.algorithms.ga;

import hicupp.algorithms.AlgorithmUtilities;
import interactivehicupp.TextTools;

import java.util.Arrays;

class Chromosome implements Cloneable{
    private double[] x;
    private double fx;

    public Chromosome(double[] x ) {
        this.x = x;
    }

    public Chromosome(double[] x, double fx) {
        this.x = x;
        this.fx = fx;
    }

    public double[] getX() {
        return x;
    }

    public double getFx() {
        return fx;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public void setFx(double fx) {
        this.fx = fx;
    }

    @Override
    public Chromosome clone() {
        double[] x = this.x.clone();

        try {
            Chromosome clone = (Chromosome) super.clone();
            clone.setX(x);
            return clone;
        } catch (CloneNotSupportedException e) {
            return new Chromosome(x, fx);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Chromosome compare) {
            double[] x1 = compare.getX();
            double fx1 = compare.getFx();
            return Arrays.equals(x, x1) && fx == fx1;
        } return false;
    }

    @Override
    public String toString() {
        return "(fx[high] = " + TextTools.formatScientific(fx) +
                ") (x[high] = {" + AlgorithmUtilities.argumentArrayToString(x) + "})";
    }
}
