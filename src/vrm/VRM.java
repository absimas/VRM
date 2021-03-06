package vrm;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ui.MainController;
import vrm.exceptions.InvalidArgumentsException;
import vrm.exceptions.InvalidCommandException;
import vrm.exceptions.MemoryOutOfBoundsException;

/**
 * Created by Simas on 2017 Mar 04.
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public class VRM {

  private static final int INTERRUPT_HANDLER_START_ADDRESS = 600;

  /**
   * List containing all executed commands. This is managed for UI purposes only.
   */
  public final ObservableList<String> commandLog = FXCollections.observableArrayList();
  public final RealMachine realMachine;
  private final MainController ui;

  /**
   * Reference {@link VirtualMachine} that was last executing.
   * This is different from {@link RealMachine#virtualMachine}, because the latter is cleared as soon as the VM is suspended, while this isn't.
   */
  public VirtualMachine virtualMachine;

  /**
   * Create a VRM object that performs the CPU algorithm and contains all the registers.
   * {@link #realMachine} and {@link #virtualMachine} values are public for viewing but should not be modified from outside.
   * @param mainController UI controller that's called when redrawing is necessary.
   */
  public VRM(MainController mainController) throws InterruptedException {
    ui = mainController;

    // Create a RM with a 1000 word memory
    realMachine = new RealMachine(commandLog, new Memory(RealMachine.MEMORY_SIZE));

    // Init interruption handlers
    interruptionHandlers();
  }

  public void begin() throws InterruptedException {
    // Imitate VM creation command to get back to VM execution
    realMachine.executeQuietly(new Command(Command.Type.STVM, 0));
    virtualMachine = realMachine.virtualMachine;

    // Store a program (fibonacci less than 1000) in VM memory
    final Memory program = virtualMachine.memory;
    program.replace(0, "HALT ");
    program.replace(1, "CR013");
    program.replace(2, "AD012");
    program.replace(3, "CP014");
    program.replace(4, "JM015");
    program.replace(5, "CM011");
    program.replace(6, "CR013");
    program.replace(7, "CM012");
    program.replace(8, "CR011");
    program.replace(9, "CM013");
    program.replace(10, "JP000");
    program.replace(12, "00000");
    program.replace(13, "00001");
    program.replace(14, "00005");
    program.replace(15, "HALT ");

    // VM is now started and its program loaded into memory. Wait for the caller to continue.
    synchronized (realMachine) {
      realMachine.wait();
    }

    // Point IC to the start of the program
    virtualMachine.IC = 0;

    // CPU ALGORITHM
    while (true) {
      // 1. Check TI
      if (realMachine.TI <= 0) {
        timerInterrupt();
        continue;
      }

      // 3. Check IC
      if (virtualMachine.IC >= 100) {
        realMachine.PI = RealMachine.ProgramInterrupt.INV_ADDRESS;
        programInterrupt();
        continue;
      }

      // Save IC
      final int savedIC = virtualMachine.IC;

      // 4. Increment IC
      virtualMachine.IC++;

      // 2. Read instruction pointed by the saved IC
      // Get word
      final Word word;
      try {
        word = virtualMachine.memory.get(savedIC);
      } catch (MemoryOutOfBoundsException e) {
        e.printStackTrace();

        // 6. Invalid address (pointed by IC)
        realMachine.PI = RealMachine.ProgramInterrupt.INV_ADDRESS;
        programInterrupt();
        continue;
      }
      // Parse command
      final Command command;
      try {
        command = Command.parse(word);
      } catch (InvalidCommandException | InvalidArgumentsException e) {
        e.printStackTrace();

        // 5. Invalid instruction
        realMachine.PI = RealMachine.ProgramInterrupt.INV_OP;
        commandLog.add(String.format("%s in %s", word.toString(), virtualMachine));
        virtualMachine.doWait();
        programInterrupt();
        continue;
      }

      // 7. Decrement TI
      realMachine.TI--;

      // 8. Execute instruction
      try {
        virtualMachine.execute(command);
      } catch (MemoryOutOfBoundsException e) {
        e.printStackTrace();
        // 9. Invalid address during command execution
        realMachine.PI = RealMachine.ProgramInterrupt.INV_ADDRESS;
        programInterrupt();
        continue;
      }

      if (!realMachine.isInterrupted()) {
        // 10. Go to start
        continue;
      }

      // 11. Handle interruptions
      if (realMachine.SI.ordinal() > 0) {
        superInterrupt(command, savedIC);
      } else if (realMachine.PI.ordinal() > 0) {
        programInterrupt();
      } else if (realMachine.TI <= 0) {
        timerInterrupt();
      } else if (realMachine.IOI > 0) {
        ioiInterrupt();
      }

      ui.draw();
    }
  }

  /**
   * Fills {@link RealMachine} memory with interruption handlers as well as stores addresses to these handlers in the first 2 blocks based on interruption indexes:
   * <pre>
   * 0.  TI
   * 1.  Incorrect address - PI
   * 2.  Incorrect operation - PI
   * 3.  Overflow - PI
   * 4.  Incorrect assignment - PI
   * 5.  GD - SI
   * 6.  PD - SI
   * 7.  RD - SI
   * 8.  WD - SI
   * 9.  SD - SI
   * 10. HALT - SI
   * 11. 1st channel work end - IOI
   * 12. 2nd channel work end - IOI
   * 13. 3rd channel work end - IOI
   * </pre>
   */
  @SuppressWarnings("UnusedAssignment")
  private void interruptionHandlers() {
    // 0. TI
    int index = 0;
    int address = INTERRUPT_HANDLER_START_ADDRESS;
    Word[] words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("TI   ") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 1. Incorrect address - PI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("PI_AD") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 2. Incorrect operation - PI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("PI_OP") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 3. Overflow - PI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("PI_OF") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 4. Incorrect assignment - PI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("PI_AS") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 5. GD - PI
    address += words.length;
    words = new Word[] { new Word(), new Word("STVM0") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 6. PD - PI
    address += words.length;
    words = new Word[] { new Word(), new Word("STVM0") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 7. RD - PI
    address += words.length;
    words = new Word[] { new Word(), new Word("STVM0") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 8. WD - PI
    address += words.length;
    words = new Word[] { new Word(), new Word("STVM0") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 9. SD - PI
    address += words.length;
    words = new Word[] { new Word(), new Word("STVM0") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 10. HALT - PI
    address += words.length;
    words = new Word[] { new Word("JP" + Utils.precedeZeroes(address, 3)), new Word("STVM0") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 11. 1st channel work end - IOI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("IOI_1") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 12. 2nd channel work end - IOI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("IOI_2") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 13. 2nd channel work end - IOI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("IOI_3") };
    realMachine.memory.replace(address, words);
    realMachine.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));
  }

  private void timerInterrupt() throws InterruptedException {
    // Get handler address
    int address = realMachine.memory.get(0).toNumber();

    // Set the handler's address as IC
    realMachine.IC = address;

    // Suspend current VM (will also change MODE)
    realMachine.suspendVM(realMachine.virtualMachine);

    // Loop execution until STVM is reached
    while (true) {
      final Command command = realMachine.step();
      if (command.type == Command.Type.STVM) break;
      // If a non-final (STVM) command was executed, wait and then increment IC for the next iteration
      realMachine.doWait();
      realMachine.IC++;
    }
    // Now, since STVM was executed, update VM reference
    virtualMachine = realMachine.virtualMachine;

    // Reset mode
    realMachine.MODE = RealMachine.Mode.U;

    // Reset TI
    realMachine.TI = RealMachine.DEFAULT_TIMER;

    // Now that registers have been modified, wait for the next command
    realMachine.doWait();
  }

  private void programInterrupt() throws InterruptedException {
    // Suspend current VM
    if (virtualMachine != null) {
      synchronized (virtualMachine) {
        realMachine.suspendVM(virtualMachine);
      }
    }

    // Get handler index
    int address;
    switch (realMachine.PI) {
      case INV_ADDRESS:
        address = 1;
        break;
      case INV_OP:
        address = 2;
        break;
      case OVERFLOW:
        address = 3;
        break;
      case INV_ASSIGN:
        address = 4;
        break;
      default:
        throw new IllegalStateException("PI handler called without a proper PI value set!");
    }

    // Get handler address
    address = realMachine.memory.get(address).toNumber();

    // Set the handler's address as IC
    realMachine.IC = address;

    // Super mode
    realMachine.MODE = RealMachine.Mode.S;

    // Loop execution until STVM is reached
    while (true) {
      final Command command = realMachine.step();
      if (command.type == Command.Type.STVM) break;
      // If a non-final (STVM) command was executed, wait and then increment IC for the next iteration
      realMachine.doWait();
      realMachine.IC++;
    }
    // Now, since STVM was executed, update VM reference
    virtualMachine = realMachine.virtualMachine;

    // Reset mode
    realMachine.MODE = RealMachine.Mode.U;

    // Clear PI
    realMachine.PI = RealMachine.ProgramInterrupt.NONE;

    // Now that registers have been modified, wait for the next command
    realMachine.doWait();
  }

  /**
   * When a VM can't execute a command, we duplicate saved IC value and execute the command in the RM.
   * When RM executes a command that's located in the context of a VM, the argument is converted to an absolute address.
   * @param command  command whose execution failed within a VM
   * @param ic       relative instruction address pointing to the command that failed
   */
  private void superInterrupt(Command command, int ic) throws InterruptedException {
    // Super
    realMachine.MODE = RealMachine.Mode.S;

    int address;
    switch (realMachine.SI) {
      case GD:
        address = 5;
        break;
      case PD:
        address = 6;
        break;
      case RD:
        address = 7;
        break;
      case WD:
        address = 8;
        break;
      case SD:
        address = 9;
        break;
      case HALT:
        address = 10;
        break;
      default:
        return;
    }
    address = realMachine.memory.get(address).toNumber();
    realMachine.IC = address;

    // HALT interruption does not modify the command or the handler's program
    if (realMachine.SI != RealMachine.SuperInterrupt.HALT) {
      // Modify command arguments and save it at this interruption handler's beginning
      command = realMachine.getAbsoluteCommand(command);
      realMachine.memory.replace(address, String.format("%-5s", command.toString()));
    }

    while (true) {
      // Save IC
      final int savedIC = realMachine.IC;

      // Increment IC
      realMachine.IC++;

      // Read instruction pointed by the saved IC
      // Get word
      final Word word = realMachine.memory.get(savedIC);
      // Parse command
      try {
        command = Command.parse(word);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      // Execute instruction
      realMachine.execute(command);

      if (command.type == Command.Type.STVM) break;

      realMachine.doWait();
    }
    // Now, since STVM was executed, update VM reference
    virtualMachine = realMachine.virtualMachine;

    // Clear SI
    realMachine.SI = RealMachine.SuperInterrupt.NONE;

    // Wait for STVM
    realMachine.doWait();
  }

  private void ioiInterrupt() throws InterruptedException {
    // Handle all channels
    ioiInterrupt(1);
    ioiInterrupt(2);
    ioiInterrupt(3);
  }

  /**
   * @param channel channel that finished its work ([1..3])
   */
  private void ioiInterrupt(int channel) throws InterruptedException {
    if (!Utils.checkFlag(realMachine.IOI, channel)) return;

    // Suspend current VM
    if (virtualMachine != null) {
      synchronized (virtualMachine) {
        realMachine.suspendVM(virtualMachine);
      }
    }

    // Get handler index
    int address = 10 + channel;

    // Get handler address
    address = realMachine.memory.get(address).toNumber();

    // Set the handler's address as IC
    realMachine.IC = address;

    // Super mode
    realMachine.MODE = RealMachine.Mode.S;

    // Loop execution until STVM is reached
    while (true) {
      final Command command = realMachine.step();
      if (command.type == Command.Type.STVM) break;
      // If a non-final (STVM) command was executed, wait and then increment IC for the next iteration
      realMachine.doWait();
      realMachine.IC++;
    }
    // Now, since STVM was executed, update VM reference
    virtualMachine = realMachine.virtualMachine;

    // Reset mode
    realMachine.MODE = RealMachine.Mode.U;
    
    // Clear given channel from bitmask
    Utils.clearFlag(realMachine.IOI, channel);

    // Now that registers have been modified, wait for the next command
    realMachine.doWait();
  }

  /**
   * Interrupts a wait command and continues VRM execution.
   */
  public synchronized void forward() {
    notify();

    synchronized (realMachine) {
      realMachine.notify();
    }

    if (virtualMachine == null) return;
    synchronized (virtualMachine) {
      virtualMachine.notify();
    }
  }

}