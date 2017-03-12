package vrm;

import java.util.Arrays;

/**
 * Word with a length of {@link #WORD_LENGTH}.
 */
public class Word {

  private static final int WORD_LENGTH = 5;

  private char[] symbols;

  /**
   * Creates an empty word.
   */
  public Word() {
    symbols = new char[WORD_LENGTH];
  }

  /**
   * Creates a word with {@link #WORD_LENGTH} symbols.
   * @param word {@link #WORD_LENGTH} symbol word
   */
  public Word(String word) {
    if (word == null || word.length() != WORD_LENGTH) {
      throw new IllegalArgumentException("Word must be of length 5. Got: " + word);
    }
    this.symbols = word.toCharArray();
  }

  /**
   * Get underlying characters.
   * Returns a copy so modification is impossible.
   */
  public char[] getSymbols() {
    return Arrays.copyOf(symbols, symbols.length);
  }

  /**
   * Replaces underlying characters.
   */
  public void replace(String word) {
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
