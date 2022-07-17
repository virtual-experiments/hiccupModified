package hicupp.classify;

import hicupp.*;

public class ProjectionStatistics {
  private double[] axis;
  private Histogram histogram;
  private double bestThreshold;
  
  public ProjectionStatistics(SetOfPoints points, double[] axis) {
    this.axis = (double[]) axis.clone();
    double[] values = Projector.project(points, this.axis);
    Sorter.quickSort(values);
    histogram = new Histogram(values);
    bestThreshold = Clusterer.split(values);
  }
  
  public double getAxisElement(int index) {
    return axis[index];
  }
  
  public Histogram getHistogram() {
    return histogram;
  }
  
  public double getBestThreshold() {
    return bestThreshold;
  }
}
