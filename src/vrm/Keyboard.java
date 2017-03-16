package vrm;

/**
 * Keyboard model. Used to read input.
 */
public class Keyboard implements Channel {

  /**
   * Size in word count.
   */
  public static final int SIZE = 2;

  public final Word[] words = new Word[SIZE];

  @Override
  public Word[] read() {
    return words;
  }

  @Override
  public void write(Word[] words) {
    throw new IllegalStateException("Cannot write to keyboard!");
  }

  @Override
  public int getIndex() {
    return 1;
  }

}
