package vrm;

import com.sun.istack.internal.Nullable;

/**
 * Keyboard model. Used to read input.
 */
public class Keyboard {

  public Word word;

  @Nullable
  public Word read() {
    return word;
  }

}
