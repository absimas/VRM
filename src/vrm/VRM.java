package vrm;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class VRM {

  public static void main() {
    new Command(Command.Type.AD, 0, 1, 5); // AD015
    new Command(Command.Type.STVM, 0); // STVM0
    new Command(Command.Type.HALT); // HALT
  }

}
