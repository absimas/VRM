package vrm.exceptions;

/**
 * Exception thrown when an unrecognized command is encountered.
 */
public class InvalidCommandException extends Exception {

  public InvalidCommandException() {
  }

  public InvalidCommandException(String message) {
    super(message);
  }

  public InvalidCommandException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidCommandException(Throwable cause) {
    super(cause);
  }

  public InvalidCommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}