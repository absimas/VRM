package vrm;

/**
 * Exception thrown when command argument count does not match the count specified by {@link Command.Type#argCount}
 * or when the arguments cannot be parsed into valid integers.
 */
public class InvalidArgumentsException extends Exception {

  public InvalidArgumentsException() {
  }

  public InvalidArgumentsException(String message) {
    super(message);
  }

  public InvalidArgumentsException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidArgumentsException(Throwable cause) {
    super(cause);
  }

  public InvalidArgumentsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}