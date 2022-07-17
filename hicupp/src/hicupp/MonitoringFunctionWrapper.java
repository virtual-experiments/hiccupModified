package hicupp;

public class MonitoringFunctionWrapper {
  private Function function;
  private Monitor monitor;
  
  public MonitoringFunctionWrapper(Function function, Monitor monitor) {
    this.function = function;
    this.monitor = monitor;
  }
  
  public int getArgumentCount() {
    return function.getArgumentCount();
  }
  
  public double evaluate(double[] arguments) throws CancellationException {
    if (monitor != null) {
      monitor.continuing();
      monitor.evaluationStarted();
    }
    return function.evaluate(arguments);
  }
}
