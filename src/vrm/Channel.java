package vrm;

import java.io.IOException;

/**
 * Created by Simas on 2017 Mar 15.
 */
public interface Channel {

  Word[] read() throws IOException;
  void write(Word[] words) throws IOException;

}
