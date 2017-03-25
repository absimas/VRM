package vrm;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

/**
 * Screen model. Used to display output.
 */
public class Screen implements Channel {

  @Nullable
  public Word word;

  public void write(@NotNull Word word) {
    System.out.println("Write to screen: " + word.toString());
    this.word = word;
  }

  @Override
  public int getIndex() {
    return 2;
  }

}
