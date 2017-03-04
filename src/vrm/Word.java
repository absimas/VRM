package vrm;

/**
 * Word with a length of {@link #WORD_LENGTH}.
 */
public class Word {

  public static final int WORD_LENGTH = 5;

  public final char[] symbols;

  /**
   * Creates a word with 5 symbols.
   * @param word 5 symbol word
   */
  public Word(String word) {
    if (word == null || word.length() != WORD_LENGTH) {
      throw new IllegalArgumentException("Word must be of length 5. Got: " + word);
    }
    this.symbols = word.toCharArray();
  }

  @Override
  public String toString() {
    return new String(symbols);
  }

}
