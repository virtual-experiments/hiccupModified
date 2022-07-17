package hicupp;

public class Projector {
  public static double[] project(SetOfPoints points, double[] axis) {
    double[] values = new double[points.getPointCount()];
    PointIterator iterator = points.createIterator();
    for (int k = 0; k < points.getPointCount(); k++) {
      iterator.next();
      double value = 0.0;
      for (int i = 0; i < points.getDimensionCount(); i++)
        value += iterator.getCoordinate(i) * axis[i];
      values[k] = value;
    }
    return values;
  }
}
