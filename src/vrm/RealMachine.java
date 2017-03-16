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
  private static final int MAX_VM_COUNT = 5;
  /**
   * Size in words.
   */
  private static final int VM_MEMORY_SIZE = 100;

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
  private final VirtualMachine[] virtualMachines = new VirtualMachine[MAX_VM_COUNT];

  /**
   * {@link VirtualMachine} that's currently executing.
   */
  public VirtualMachine virtualMachine;

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

  private void setChannelBusy(int i, boolean busy) {
    if (busy) {
      BUSY = Utils.setFlag(BUSY, i);
    } else {
      BUSY = Utils.clearFlag(BUSY, i);
    }
  }

  @Override
  public void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException {
    switch (command.type) {
      case GD:
        // Block keyboard channel
        setChannelBusy(keyboard.getIndex(), true);

        // Read symbols from keyboard
        final Word[] input = keyboard.read();

        // Save in memory
        for (int i = 0; i < input.length; i++) {
          memory.replace(command.getArgument() + i, input[i]);
        }

        // Unblock keyboard channel
        setChannelBusy(keyboard.getIndex(), false);
        break;
      case PD:
        // Block screen channel
        setChannelBusy(screen.getIndex(), true);

        // Get 10 symbols (2 words) from memory
        final Word[] output = {memory.get(command.getArgument()), memory.get(command.getArgument() + 1)};

        // Output to screen
        screen.write(output);

        // Unblock keyboard channel
        setChannelBusy(screen.getIndex(), false);
        break;
      case RD: {
        // Block external memory channel
        setChannelBusy(externalMemory.getIndex(), true);

        // Read words
        final Word[] words = externalMemory.read();

        // Save in memory
        for (int i = 0; i < words.length; i++) {
          memory.replace(command.getArgument() + i, words[i]);
        }

        // Unblock external memory channel
        setChannelBusy(externalMemory.getIndex(), false);
        break;
      }
      case WD: {
        // Block external memory channel
        setChannelBusy(externalMemory.getIndex(), true);

        // Get 10 words from memory
        final Word[] words = new Word[10];
        for (int i = 0; i < 10; i++) {
          words[i] = memory.get(command.getArgument() + i);
        }

        // Write words to external memory
        externalMemory.write(words);

        // Unblock external memory channel
        setChannelBusy(externalMemory.getIndex(), true);
        break;
      }
      case SD:
        // Block external memory channel
        setChannelBusy(externalMemory.getIndex(), true);

        externalMemory.setPointer(command.getArgument());

        // Unblock external memory channel
        setChannelBusy(externalMemory.getIndex(), false);
        break;
      case HALT:
        System.exit(1); // ToDo does this work with GUI apps?
        break;
      case GT:
        memory.replace(command.getArgument(), Utils.precedeZeroes(TI, Word.LENGTH));
        break;
      case PT:
        // Set TI to equal to the number at the specified address
        TI = memory.get(command.getArgument()).toNumber();
        break;
      case STVM: {
        final int index = command.getArgument();
        if (index >= MAX_VM_COUNT) {
          throw new IllegalArgumentException(String.format("Index %d exceeds the maximum VM count!", index));
        }

        VirtualMachine vm = virtualMachines[index];

        // 1. Restore VM registers
        if (vm != null) {
          vm.restoreRegisters();
        }

        // 2. Change MODE
        MODE = Mode.U;

        // 3. Reset timer
        TI = DEFAULT_TIMER;

        // 4. Start/Resume VM
        if (vm == null) {
          // 20 - interrupt table, 50 - page tables, VM_MEMORY_SIZE - memory for each VM
          final int offset = 20 + 50 + index * VM_MEMORY_SIZE;
          virtualMachines[index] = new VirtualMachine(this, memory.sublist(offset, offset + VM_MEMORY_SIZE));
        }
        virtualMachine = virtualMachines[index];
        break;
      }
      case SVRG: {
        final int index = command.getArgument();
        if (index >= MAX_VM_COUNT) {
          throw new IllegalArgumentException(String.format("Index %d exceeds the maximum VM count!", index));
        }

        final VirtualMachine vm = virtualMachines[index];
        if (vm == null) {
          throw new IllegalArgumentException(String.format("VM with index %d was not yet created!", index));
        }

        vm.saveRegisters();
        break;
      }
      case LDRG: {
        final int index = command.getArgument();
        if (index >= MAX_VM_COUNT) {
          throw new IllegalArgumentException(String.format("Index %d exceeds the maximum VM count!", index));
        }

        final VirtualMachine vm = virtualMachines[index];
        if (vm == null) {
          throw new IllegalArgumentException(String.format("VM with index %d was not yet created!", index));
        }

        vm.restoreRegisters();
        break;
      }
      default:
        super.execute(command);
    }
  }

  /**
   * Used to halt a VM from being tracked by this RM.
   */
  public void haltVM(VirtualMachine vm) {
    // Remove from VM list
    for (int i = 0; i < virtualMachines.length; i++) {
      if (virtualMachines[i] == vm) {
        virtualMachines[i] = null;
      }
    }
    // Remove if it's the current VM
    if (virtualMachine == vm) {
      virtualMachine = null;
    }
  }

}
