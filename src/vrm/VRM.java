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

  public VirtualMachine virtualMachine;

//  private void vmCreationExample() {
//    final RealMachine rm = new RealMachine(new Memory(RealMachine.MEMORY_SIZE));
//
//    // Create 3 VMs that use independent memory blocks of the RM
//    // Created VMs will have memories: [0..9], [10..19], [20..29]
//    for (int i = 0; i < 3; i++) {
//      // Allocate VM memory
//      final int vmMemoryOffset = i * RealMachine.VM_MEMORY_SIZE;
//      final Memory vmMemory = rm.memory.sublist(vmMemoryOffset, vmMemoryOffset + RealMachine.VM_MEMORY_SIZE);
//
//      // Create and save VM page table
//      final int vmPageTableOffset = RealMachine.INTERRUPT_TABLE_SIZE + i * RealMachine.VM_MEMORY_SIZE / 10;
//      final PageTable vmPageTable = new PageTable(Utils.generateRange(vmMemoryOffset, vmMemoryOffset + RealMachine.VM_MEMORY_SIZE));
//      rm.memory.replace(vmPageTableOffset, vmPageTable.table);
//
//      // Create VM
//      final VirtualMachine vm = new VirtualMachine(rm, vmMemory);
//
//      // Reference newly created VM as the current one
//      rm.virtualMachine = vm;
//      rm.PTR = vmPageTableOffset;
//    }
//  }
//
//  private void commandExample1() {
//    new Command(Command.Type.AD, 0, 1, 5); // AD015
//    new Command(Command.Type.STVM, 0); // STVM0
//    new Command(Command.Type.HALT); // HALT
//  }
//
//  private void commandExample2() {
//    // Prepare commands that we'll use
//    commands.clear();
//    commands.add("PD013");
//    commands.add("CR013");
//    commands.add("AD012");
//    commands.add("CP023");
//    commands.add("JM024");
//    commands.add("CM011");
//    commands.add("CR013");
//    commands.add("CR011");
//    commands.add("CM013");
//    commands.add("JP000");
//
//    // Parse commands
//    try {
//      for (String string : commands) {
//        Command.parse(string);
//        // Execute command
//      }
//    } catch (InvalidCommandException | InvalidArgumentsException e) {
//      // Interrupt
//      e.printStackTrace();
//    }
//  }

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
    // Imitate VM creation command
    realMachine.memory.replace(RealMachine.MEMORY_SIZE-1, "STVM0");
    realMachine.IC = RealMachine.MEMORY_SIZE-1;
    realMachine.step();
    virtualMachine = realMachine.virtualMachine;

    // Store a program (fibonacci less than 1000) in VM memory
    final Memory program = virtualMachine.memory;
    program.replace(0, "PD013");
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
    program.replace(14, "01000");
    program.replace(15, "HALT ");

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
      final Command command;
      try {
        command = Command.parse(virtualMachine.memory.get(savedIC));
      } catch (InvalidCommandException | InvalidArgumentsException e) {
        e.printStackTrace();

        // 5. Invalid instruction
        realMachine.PI = RealMachine.ProgramInterrupt.INV_OP;
        programInterrupt();
        continue;
      } catch (MemoryOutOfBoundsException e) {
        e.printStackTrace();

        // 6. Invalid address (pointed by IC)
        realMachine.PI = RealMachine.ProgramInterrupt.INV_ADDRESS;
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
        superInterrupt(savedIC);
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
    index++;
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

    // 5-10. Super interrupts. They don't have a custom handler.

    // 11. 1st channel work end - IOI
    address += words.length;
    index = 11;
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

    // Super mode
    realMachine.MODE = RealMachine.Mode.S;

    Command command = realMachine.stepQuietly();
    while (command.type != Command.Type.STVM && command.type != Command.Type.HALT) {
      // Move to the next instruction
      realMachine.IC++;
      command = realMachine.step();
    }

    // Reset mode
    realMachine.MODE = RealMachine.Mode.U;

    // Reset TI
    realMachine.TI = RealMachine.DEFAULT_TIMER;
  }

  private void programInterrupt() throws InterruptedException {
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

    Command command = realMachine.stepQuietly();
    while (command.type != Command.Type.STVM && command.type != Command.Type.HALT) {
      // Move to the next instruction
      realMachine.IC++;
      command = realMachine.step();
    }

    // Reset mode
    realMachine.MODE = RealMachine.Mode.U;

    // Clear PI
    realMachine.PI = RealMachine.ProgramInterrupt.NONE;
  }

  /**
   * When a VM can't execute a command, we duplicate saved IC value and execute the command in the RM.
   * When RM executes a command that's located in the context of a VM, the argument is converted to an absolute address.
   * @param failedIC IC value pointing to the failing command
   */
  private void superInterrupt(int failedIC) throws InterruptedException {

    // Convert the saved IC into an absolute address
    realMachine.IC = realMachine.getAbsoluteAddress(failedIC);

    // Execute the command, now in the RM
    realMachine.step();

    // Unprivileged command was executed in the RM.
    // We can now clear the SI register.
    realMachine.SI = RealMachine.SuperInterrupt.NONE;
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

    // Get handler index
    int address = 10 + channel;

    // Get handler address
    address = realMachine.memory.get(address).toNumber();

    // Set the handler's address as IC
    realMachine.IC = address;

    // Super mode
    realMachine.MODE = RealMachine.Mode.S;

    Command command = realMachine.stepQuietly();
    while (command.type != Command.Type.STVM && command.type != Command.Type.HALT) {
      // Move to the next instruction
      realMachine.IC++;
      command = realMachine.step();
    }

    // Reset mode
    realMachine.MODE = RealMachine.Mode.U;
    
    // Clear given channel from bitmask
    Utils.clearFlag(realMachine.IOI, channel);
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