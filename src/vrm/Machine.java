package vrm;

/**
 * Main class that allows:
 * <ul>
 *   <li>{@link Command} execution</li>
 * </ul>
 */
public abstract class Machine {

  /**
   * Temporary. Size 5 bytes.
   */
  public int[] TMP = new int[] { 0, 0, 0, 0, 0 };
  /**
   * Comparison result. Size 1 byte.
   */
  public Comparison C = Comparison.EQUAL;
  /**
   * Instruction Counter. Length specifies the maximum instruction size.
   * E.g. length == 2 means [0..99], while length == 3 means [0..999].
   */
  public final int[] IC;

  /**
   * Super Interruptions caused by a specific type Command ({@link Command.Type}).
   */
  public enum SuperInterrupt {
    GD(Command.Type.GD),
    PD(Command.Type.PD),
    RD(Command.Type.RD),
    WD(Command.Type.WD),
    SD(Command.Type.SD),
    HALT(Command.Type.HALT);

    private final Command.Type cause;

    SuperInterrupt(Command.Type cause) {
      this.cause = cause;
    }
  }

  /**
   * Memory
   */
  public final Memory memory;

  protected Machine(Memory memory) {
    this.memory = memory;

    // Create an IC big enough to cover all the whole memory
    IC = new int[String.valueOf(this.memory.size()).length()];
  }

}