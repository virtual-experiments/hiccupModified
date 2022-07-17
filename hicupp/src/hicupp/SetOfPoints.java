package hicupp;

public interface SetOfPoints {
  int getDimensionCount();
  int getPointCount();
  PointIterator createIterator();
}
