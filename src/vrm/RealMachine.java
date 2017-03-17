package vrm;

import com.sun.istack.internal.NotNull;

import vrm.exceptions.InvalidArgumentsException;
import vrm.exceptions.InvalidCommandException;
import vrm.exceptions.MemoryOutOfBoundsException;
import vrm.exceptions.UnhandledCommandException;

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
    NONE(null),
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
    NONE,
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
  public static final int VM_MEMORY_SIZE = 100;
  /**
   * Max VM page table size in words.
   */
  public static final int VM_PAGE_TABLES_SIZE = MAX_VM_COUNT * VM_MEMORY_SIZE / 10;
  /**
   * Size in words.
   */
  public static final int INTERRUPT_TABLE_SIZE = 20;
  /**
   * RM memory size in words.
   */
  public static final int MEMORY_SIZE = 1000;
  /**
   * Counter for instantiated RMs.
   */
  private static int RM_IDS = 0;

  /**
   * Current VM memory Page Table Register's address. Size 3 bytes.
   */
  public int PTR;
  /**
   * Program Interrupt. Default value = {@link ProgramInterrupt#NONE}. Size 1 byte.
   */
  @NotNull
  public ProgramInterrupt PI = ProgramInterrupt.NONE;
  /**
   * Timer Interrupt. [0.99]. Size 1 byte.
   */
  public int TI = DEFAULT_TIMER;
  /**
   * Super Interrupt. Default value = {@link SuperInterrupt#NONE}. Size 1 byte.
   */
  @NotNull
  public SuperInterrupt SI = SuperInterrupt.NONE;
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
   * Unique id for this RM instance.
   */
  private final int id = RM_IDS++;

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
  protected void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException, InterruptedException {
    System.out.println("Execute " + command + " in " + this);
    switch (command.type) {
      case GD:
        // Block until keyboard is free
        if (isChannelBusy(keyboard.getIndex())) {
          wait();
        }

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
        // Block until screen is free
        if (isChannelBusy(screen.getIndex())) {
          wait();
        }

        // Block screen channel
        setChannelBusy(screen.getIndex(), true);

        // Output to screen
        screen.write(memory.get(command.getArgument()));

        // Unblock keyboard channel
        setChannelBusy(screen.getIndex(), false);
        break;
      case RD: {
        // Block until external memory is free
        if (isChannelBusy(externalMemory.getIndex())) {
          wait();
        }

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
        // Block until external memory is free
        if (isChannelBusy(externalMemory.getIndex())) {
          wait();
        }

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
        // Block until external memory is free
        if (isChannelBusy(externalMemory.getIndex())) {
          wait();
        }

        // Block external memory channel
        setChannelBusy(externalMemory.getIndex(), true);

        externalMemory.setPointer(command.getArgument());

        // Unblock external memory channel
        setChannelBusy(externalMemory.getIndex(), false);
        break;
      case HALT:
        System.exit(3); // ToDo does this work with GUI apps?
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

        // Calculate offset for VM page table
        final int vmPageTableOffset = INTERRUPT_TABLE_SIZE + index * VM_MEMORY_SIZE / 10;

        // 4. Start/Resume VM
        if (vm == null) {
          // Calculate offset for VM memory
          final int vmMemoryOffset = INTERRUPT_TABLE_SIZE + VM_PAGE_TABLES_SIZE + index * VM_MEMORY_SIZE;

          // Allocate VM memory
          final Memory vmMemory = memory.sublist(vmMemoryOffset, vmMemoryOffset + VM_MEMORY_SIZE);

          // Generate a page table for this memory
          final PageTable vmPageTable = new PageTable(Utils.generateRange(vmMemoryOffset, vmMemoryOffset + VM_MEMORY_SIZE / 10));
          // Save page table
          memory.replace(vmPageTableOffset, vmPageTable.table);

          // Create VM
          virtualMachines[index] = new VirtualMachine(this, vmMemory);
        }
        // Reference newly created/resumed VM as the current one
        virtualMachine = virtualMachines[index];
        PTR = vmPageTableOffset;
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
   * Check interruption registers whether an interruption has occurred.
   */
  public boolean isInterrupted() {
    return PI.ordinal() + SI.ordinal() + IOI > 0 || TI == 0;
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

  /**
   * Convert the given relative address to an absolute one. Current VMs page table will be used for conversion.
   */
  public int getAbsoluteAddress(int relativeAddress) {
    return getAbsoluteAddress(getPageTable(PTR), relativeAddress);
  }

  /**
   * Convert a relative address to an absolute one using a specific page table.
   * @param pageTable       page table
   * @param relativeAddress address relative to the given VM
   * @return absolute (RM) address
   */
  private int getAbsoluteAddress(PageTable pageTable, int relativeAddress) {
    // Determine what VM block the address is pointing to
    final int vmMemoryBlock = relativeAddress / 10;

    // Calculate address offset from the beginning of that block
    final int addressOffset = relativeAddress - vmMemoryBlock;

    // Determine absolute address for the given relative address
    return pageTable.table[vmMemoryBlock].toNumber() + addressOffset;
  }

  /**
   * Gets VM page table at the specified address.
   * @param address page table address
   */
  private PageTable getPageTable(int address) {
    return new PageTable(memory.get(address, RealMachine.VM_MEMORY_SIZE / 10));
  }

  @Override
  public String toString() {
    return String.format("RM%d", id);
  }

  /**
   * Notes:
   * - When RM executes a command that's located in the context of a VM, the argument is converted to an absolute address.
   * @see Machine#step()
   */

  /**
   * Executes instruction pointed by {@code #IC}.
   * This method returns false if the executing the current instruction caused an interruption.
   * @return true if the instruction was executed successfully, false otherwise.
   * @throws UnhandledCommandException when a command cannot be handled in this machine. E.g. STVMx in a VM.
   * @throws InterruptedException when a machine block (e.g. when waiting for a channel) is interrupted
   */
  public boolean step() throws UnhandledCommandException, InterruptedException {
    // Switch to super mode
    MODE = RealMachine.Mode.S;

    // Fetch Word at IC
    final Word word;
    try {
      word = memory.get(IC);
    } catch (MemoryOutOfBoundsException e) {
      e.printStackTrace();
      throw new RuntimeException(String.format("RM referenced an invalid address when looking for a command at %d!", IC));
    }

    // Convert Word to Command
    Command command;
    try {
      command = Command.parse(word);
    } catch (InvalidArgumentsException | InvalidCommandException e) {
      // IC is pointing to an invalid command
      e.printStackTrace();
      throw new RuntimeException(String.format("RM encountered an invalid command: %s!", word));
    }

    // If this command is located in the VM memory block, create an identical command with an absolute address
    final int startInclusive = INTERRUPT_TABLE_SIZE + VM_PAGE_TABLES_SIZE;
    final int endExclusive = startInclusive + MAX_VM_COUNT * VM_MEMORY_SIZE;
    if (IC >= startInclusive && IC < endExclusive) {
      command = getAbsoluteCommand(command);
      // ToDo include the new command into command list so it's displayed and we know what's going on
    }

    // Execute the command
    try {
      execute(command);
    } catch (MemoryOutOfBoundsException e) {
      e.printStackTrace();
      throw new RuntimeException(String.format("RM referenced an invalid address when executing %s!", command));
    }

    // Switch back to user mode
    MODE = RealMachine.Mode.U;

    return true;
  }

  /**
   * Converts relative command arguments to absolute and returns it all in a newly created {@link Command}.
   * @param command original command with relative arguments
   * @return command with absolute arguments
   */
  private Command getAbsoluteCommand(Command command) {
    final String absolute = Utils.precedeZeroes(getAbsoluteAddress(command.getArgument()), 3);

    final int count = command.type.argCount;
    if (count == 0) return new Command(command.type);

    final int[] args = new int[count];
    for (int i = 0; i < count; i++) {
      args[i] = Character.getNumericValue(absolute.charAt(i));
    }

    return new Command(command.type, args);
  }

}
