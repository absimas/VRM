package vrm;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class RealMachine {

  public enum Mode {
    /**
     * Supervisor
     */
    S,
    /**
     * User
     */
    U
  }

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
   * Program Interruptions caused internal and source code problems.
   */
  public enum ProgramInterrupt {
    /**
     * Invalid memory address.
     */
    INV_ADDRESS,
    /**
     * Invalid operation code.
     */
    INV_OP,
    OVERFLOW,
    /**
     * Invalid/Incorrect assignment.
     */
    INV_ASSIGN
  }

  /**
   * Default timer value. [0..99].
   */
  public static final int DEFAULT_TIMER = 10;

  /**
   * Temporary. Size 5 bytes.
   */
  public int[] TMP = new int[] { 0, 0, 0, 0, 0 };
  /**
   * Page Table Register. Size 4 bytes.
   */
  public int[] PTR = new int[] { 0, 0, 0, 0 };
  /**
   * Instruction Counter. [0..999]. Size 3 bytes.
   */
  public int[] IC = new int[] { 0, 0, 0 };
  /**
   * Comparison result. Size 1 byte.
   */
  public Comparison C = Comparison.EQUAL;
  /**
   * Program Interrupt. Default value = null. Size 1 byte.
   */
  public ProgramInterrupt PI;
  /**
   * Timer Interrupt. [0.99]. Size 1 byte.
   */
  public int TI = DEFAULT_TIMER;
  /**
   * Super Interrupt. Default value = null. Size 1 byte.
   */
  public SuperInterrupt SI;
  /**
   * I/O Interrupt. Bitmask [1..3]. 1 byte.
   */
  public int IOI = 0;
  /**
   * Processor mode. 1 byte.
   */
  public Mode MODE = Mode.U;
  /**
   * Busy channels. Bitmask [1..3]. 1 Byte.
   */
  public int BUSY = 0;

  /**
   * Check whether a channel has been marked as busy.
   * @param i channel number [1..3]
   * @return true if channel is busy, false otherwise.
   */
  public boolean isChannelBusy(int i) {
    if (i < 1 || i > 3) {
      throw new IllegalArgumentException("Busy state for an invalid channel: " + i);
    }

    return Utils.checkFlag(BUSY, i);
  }

}
