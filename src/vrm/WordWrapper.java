package vrm;

/**
 * {@link String} wrapper that forces a length of {@link #WORD_LENGTH}.
 */
public class WordWrapper {

  private static final int WORD_LENGTH = 5;

  public final String word;

  /**
   * Creates a word with 5 words.
   * @param word 5 symbol word
   */
  public WordWrapper(String word) {
    if (word == null || word.length() != WORD_LENGTH) {
      throw new IllegalArgumentException("Word must be of length 5. Got: " + word);
    }
    this.word = word;
  }

  @Override
  public String toString() {
    return word;
  }
}
