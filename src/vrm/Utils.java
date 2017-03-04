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

}
