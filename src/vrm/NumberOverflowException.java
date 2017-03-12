package vrm;

/**
 * Thrown when number arithmetic result does not fit in a {@link Word}.
 */
public class NumberOverflowException extends RuntimeException {

  public NumberOverflowException() {
    super();
  }

  public NumberOverflowException(String message) {
    super(message);
  }

}
