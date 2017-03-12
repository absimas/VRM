package vrm;

import java.util.Arrays;

/**
 * Word with a length of {@link #LENGTH}.
 */
public class Word {

  public static final int LENGTH = 5;

  private char[] symbols;

  /**
   * Creates an empty word.
   */
  public Word() {
    symbols = new char[LENGTH];
  }

  /**
   * Creates a word with {@link #LENGTH} symbols.
   * @param word {@link #LENGTH} symbol word
   */
  public Word(String word) {
    if (word == null || word.length() != LENGTH) {
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
    if (word == null || word.length() != LENGTH) {
      throw new IllegalArgumentException("Word must be of length 5. Got: " + word);
    }

    this.symbols = word.toCharArray();
  }

  @Override
  public String toString() {
    return new String(symbols);
  }

  /**
   * Converts this word into a number.
   * @return number representation of this word.
   * @throws NumberFormatException if the word cannot be converted into a number
   */
  public int toNumber() throws NumberFormatException {
    return Integer.valueOf(toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Word word = (Word) o;

    return Arrays.equals(symbols, word.symbols);

  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(symbols);
  }

}
