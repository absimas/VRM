package vrm;

/**
 * Created when a word that's OOB in the given memory is referenced.
 */
public class MemoryOutOfBoundsException extends RuntimeException {

  public MemoryOutOfBoundsException() {
  }

  public MemoryOutOfBoundsException(String message) {
    super(message);
  }

  public MemoryOutOfBoundsException(String message, Throwable cause) {
    super(message, cause);
  }

  public MemoryOutOfBoundsException(Throwable cause) {
    super(cause);
  }

  public MemoryOutOfBoundsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}