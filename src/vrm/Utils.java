package vrm;

import java.util.stream.IntStream;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

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

  public static int setFlag(int value, int flag) {
    return value | flag;
  }

  public static int clearFlag(int value, int flag) {
    return value & (~flag);
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
    string = "00000000000000" + string;
    return string.substring(string.length()-size);
  }

  /**
   * @see #precedeZeroes(String, int)
   */
  public static String precedeZeroes(int integer, int size) {
    return precedeZeroes(String.valueOf(integer), size);
  }

  /**
   * Generate a range of ints.
   */
  public static int[] generateRange(int startInclusive, int endExclusive) {
    return IntStream.range(startInclusive, endExclusive).toArray();
  }

  public static void delay(Runnable runnable, int millis) throws RuntimeException {
    final Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        Thread.sleep(millis);
        return null;
      }
    };
    task.setOnSucceeded(event -> runnable.run());

    new Thread(task).start();
  }

}
