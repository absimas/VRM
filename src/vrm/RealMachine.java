package vrm;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class RealMachine extends Machine {

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
   * Default external memory path.
   */
  private static final String EXTERNAL_MEMORY_PATH = "external_memory.txt";

  /**
   * Current VM memory Page Table Register. Size 4 bytes. ToDo change to a reference to VM or PTR class.
   */
  public int[] PTR = new int[] { 0, 0, 0, 0 };
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

  private final Keyboard keyboard = new Keyboard();
  private final Screen screen = new Screen();
  private final ExternalMemory externalMemory = new ExternalMemory(EXTERNAL_MEMORY_PATH);

  public RealMachine(Memory memory) {
    super(memory);
  }

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

  @Override
  public void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException {
    switch (command.type) {
      case GD:
        // Read symbols from keyboard
        final Word[] input = keyboard.read();

        // Save in memory
        for (int i = 0; i < input.length; i++) {
          memory.replace(command.getArgument() + i, input[i]);
        }
        break;
      case PD:
        // Get 10 symbols (2 words) from memory
        final Word[] output = { memory.get(command.getArgument()), memory.get(command.getArgument()+1) };

        // Output to screen
        screen.write(output);
        break;
      case RD: {
        // Read words
        final Word[] words = externalMemory.read();

        // Save in memory
        for (int i = 0; i < words.length; i++) {
          memory.replace(command.getArgument() + i, words[i]);
        }
        break;
      }
      case WD: {
        // Get 10 words from memory
        final Word[] words = new Word[10];
        for (int i = 0; i < 10; i++) {
          words[i] = memory.get(command.getArgument() + i);
        }

        // Write words to external memory
        externalMemory.write(words);
        break;
      }
      case SD:
        externalMemory.setPointer(command.getArgument());
        break;
      case HALT:
        // ToDo ?
        break;
      case GT:
        memory.replace(command.getArgument(), Utils.precedeZeroes(TI, Word.LENGTH));
        break;
      case PT:
        // Set TI to equal to the number at the specified address
        TI = memory.get(command.getArgument()).toNumber();
        break;
      case STVM:
        // ToDo indexed VM array
        break;
      case SVRG:
        // ToDo use a separate memory block when VM indexes are available
        break;
      case LDRG:
        // ToDo use a separate memory block when VM indexes are available
        break;
      default:
        super.execute(command);
    }
  }

}
