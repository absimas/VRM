package vrm;

import com.sun.istack.internal.NotNull;

import java.io.IOException;

/**
 * Created by Simas on 2017 Mar 15.
 */
public interface Channel {

  Word[] read() throws IOException;
  void write(@NotNull Word... words) throws IOException;

  /**
   * Get channel index.
   */
  int getIndex();

}
