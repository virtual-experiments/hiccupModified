package hicupp;

/**
 * Transforms a function into a function that is guaranteed not to
 * modify its arguments.
 */
public class CloningFunctionWrapper implements Function {
  private Function function;
  
  public CloningFunctionWrapper(Function function) {
    this.function = function;
  }
  
  public int getArgumentCount() {
    return function.getArgumentCount();
  }
  
  public double evaluate(double[] arguments) {
    return function.evaluate((double[]) arguments.clone());
  }
}
