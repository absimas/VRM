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
    Arrays.fill(symbols, '0');
  }

  /**
   * Creates a word with at most {@link #LENGTH} symbols.
   * @param string string with {@link #LENGTH} symbols
   */
  public Word(String string) {
    if (string == null || string.length() != LENGTH) {
      throw new IllegalArgumentException(String.format("Word length must equal to %d. Got: %s.", LENGTH, string));
    }
    this.symbols = string.toCharArray();
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
   * @param string string with {@link #LENGTH} symbols
   */
  public void replace(String string) {
    if (string == null || string.length() != LENGTH) {
      throw new IllegalArgumentException(String.format("Word length must equal to %d. Got: %s.", LENGTH, string));
    }

    this.symbols = string.toCharArray();
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
