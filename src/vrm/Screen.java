package vrm;

/**
 * Screen model. Used to display output.
 */
public class Screen implements Channel {

  /**
   * Size in word count.
   */
  public static final int SIZE = 2;

  public final Word[] words = new Word[SIZE];

  @Override
  public Word[] read() {
    throw new IllegalStateException("Cannot read from screen!");
  }

  @Override
  public void write(Word[] words) {
    if (words.length != SIZE) {
      throw new IllegalArgumentException(String.format("Must write %d words!", SIZE));
    }

    System.arraycopy(words, 0, this.words, 0, words.length);
  }

  @Override
  public int getIndex() {
    return 2;
  }

}
