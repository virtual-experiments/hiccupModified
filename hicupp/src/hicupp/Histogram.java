package hicupp;

public class Histogram {
  private double size;
  private double min, max;
  private int maxfreq;
  private int[] freqs;
  
  /**
   * Assumes that <code>list</code> is sorted.
   */
  public Histogram(double[] list) {
    int nslots;
    if (list.length < 500)
      nslots = list.length < 10 ? 1 : list.length / 10;
    else
      nslots = 50;
    min = list[0];
    max = list[list.length - 1];
    size = (max - min) / nslots;

    freqs = new int[nslots];

    double bottom = list[0];
    double top = bottom + size;
    for (int i = 0; i < nslots; i++) {
      int freq = 0;
      for (int j = 0; j < list.length; j++) {
        double value = list[j];
        if (value >= bottom && value < top)
          freq++;
      }
      if (freq > maxfreq)
        maxfreq = freq;
      freqs[i] = freq;
      bottom = top;
      top += size;
    }
  }
  
  public double getMin() {
    return min;
  }
  
  public double getMax() {
    return max;
  }
  
  public int getClassCount() {
    return freqs.length;
  }
  
  /**
   * The index of the first class is 0.
   */
  public int getClassFrequency(int classIndex) {
    return freqs[classIndex];
  }
  
  public double getClassSize() {
    return size;
  }
  
  public int getMaxFrequency() {
    return maxfreq;
  }
}
