package hicupp;

/**
 * Implemented by the user of the {@link FunctionMaximizer} class and passed to
 * that class's {@link FunctionMaximizer#maximize(Function, int, Monitor)} method.
 */
public interface Monitor {
  /**
   * Invoked to give the user a chance to throw a CancellationException, which
   * will cause the {@link FunctionMaximizer#maximize(Function, int, Monitor)} method to
   * terminate, passing the exception on to its caller.
   */
  void continuing() throws CancellationException;
  /**
   * Invoked whenever a new iteration is started.
   * <p>The first iteration number is 1.</p>
   */
  void iterationStarted(int iterationNumber);
  /**
   * Invoked whenever the function is evaluated.
   */
  void evaluationStarted();
  void writeLine(String text);
}
