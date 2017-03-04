package vrm;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class VRM {

  public static void main() {
    new Command(Command.Type.AD, 0, 1, 5); // AD015
    new Command(Command.Type.STVM, 0); // STVM0
    new Command(Command.Type.HALT); // HALT

    // -------------------------------------------------------------

    // 1 block = 10 words
    final Word[] block = new Word[10];

    // Fill words
    block[0] = new Word("PD013");
    block[1] = new Word("CR013");
    block[2] = new Word("AD012");
    block[3] = new Word("CP023");
    block[4] = new Word("JM024");
    block[5] = new Word("CM011");
    block[6] = new Word("CR013");
    block[7] = new Word("CR011");
    block[8] = new Word("CM013");
    block[9] = new Word("JP000");

    // Parse words into commands
    try {
      for (Word word : block) {
        Command.parse(word);
        // Execute command
      }
    } catch (InvalidCommandException | InvalidArgumentsException e) {
      // Interrupt
      e.printStackTrace();
    }
  }

}
