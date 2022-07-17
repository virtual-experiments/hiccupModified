package hicupp;

/**
 * Represents a sequence of points.
 * <p>{@link next()} must be called at least once before calling
 * {@link getCoordinate(int)}. In other words, the "current element pointer" is
 * initially before the first element of the sequence.</p>
 */
public interface PointIterator {
  boolean hasNext();
  void next();
  double getCoordinate(int index);
}
