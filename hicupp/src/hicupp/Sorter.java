package hicupp;

public class Sorter {
  public static void quickSort(double[] values) {
    final int stackCapacity = 1024;
    final int bubbleSortThreshold = 10;
  
    final double fa = 2.11e2;
    final double fc = 1.663e3;
    final double fm = 7.875e3;
    final double fmi = 1.0 / fm;
    
    int stackPointer = 0;
    final int[] stack = new int[stackCapacity];
    double fxx = 0.0;
    int l = 0;
    int r = values.length - 1;
    
    while (true) {
      if (r - l < bubbleSortThreshold) {
        
        // Partition is small enough to sort by straight insertion.
        
        for (int j = l + 1; j <= r; j++) {
          double item = values[j];
          int i;
          for (i = j - 1; i >= l && values[i] > item; i--)
            values[i + 1] = values[i];
          values[i + 1] = item;
        }
    
        // An empty stack means we are finished.
        
        if (stackPointer == 0) {
          if (!sorted(values))
            throw new RuntimeException("Postcondition check failed.");
          return;
        }
        
        // Pop the next partition off the stack.
        
        r = stack[--stackPointer];
        l = stack[--stackPointer];
      } else {
        
        // Partition the next sublist.
        
        int i = l;
        int j = r;
        fxx = Math.IEEEremainder(fxx * fa + fc, fm);
        if (fxx < 0.0)
          fxx += fm;
        int pivot = l + (int) ((r - l + 1) * (fxx * fmi));
        double item = values[pivot];
        values[pivot] = values[l];
        
        while (true) {
          
          // Scan the list from the right.
          
          while (j >= 0 && item < values[j])
            j--;
          
          if (j <= i) {
            
            // Our pointers have crossed.
            // Place the element into its final (sorted) position.
            
            values[i] = item;
            break;
          }
          
          // Exchange list elements.
          
          values[i] = values[j];
          i++;
          
          // Scan the list from the left.
          
          while (i < values.length && item > values[i])
            i++;
          
          if (j <= i) {
            // Our pointers have crossed.
            // Place the element into its final (sorted) position.
          
            values[j] = item;
            i = j;
            break;
          }
          
          // Exchange list elements.
          
          values[j] = values[i];
          j--;
        }
        
        // Increment the stack pointer and stack the largest
        // partition for future processing.
        
        if (r - i >= i - l) {
          stack[stackPointer++] = i + 1;
          stack[stackPointer++] = r;
          r = i - 1;
        } else {
          stack[stackPointer++] = l;
          stack[stackPointer++] = i - 1;
          l = i + 1;
        }
      }
    }
  }
  
  public static boolean sorted(double[] values) {
    for (int i = 1; i < values.length; i++)
      if (values[i - 1] > values[i])
        return false;
    return true;
  }
}
