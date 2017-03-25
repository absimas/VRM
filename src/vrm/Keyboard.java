package vrm;

import com.sun.istack.internal.Nullable;

/**
 * Keyboard model. Used to read input.
 */
public class Keyboard implements Channel {

  @Nullable
  public Word word;

  public Word read() {
    final Word word = this.word;

    // Consume word
    this.word = null;

    return word;
  }

  @Override
  public int getIndex() {
    return 1;
  }

}
