package vrm;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class Utils {

  /**
   * Private c-tor.
   */
  private Utils() {}

  public static boolean checkFlag(int value, int flag) {
    return (value & flag) == flag;
  }

  /**
   * Precedes the given string with zeroes until the wanted size is met.
   * Note that if the given string is longer, it will be trimmed to meet the size and no zeroes will be added.
   * E.g.
   * <code>
   *   precedeZeroes("abc", 5);
   * </code>
   * will return 00abc.
   * @param string  string that will be preceded by zeroes
   * @param size    total size of the resulting string
   * @return string preceded with zeroes to meet the wanted size.
   */
  public static String precedeZeroes(String string, int size) {
    return ("0000000000" + string).substring(size);
  }

  /**
   * @see #precedeZeroes(String, int)
   */
  public static String precedeZeroes(int integer, int size) {
    return precedeZeroes(String.valueOf(integer), size);
  }

}
