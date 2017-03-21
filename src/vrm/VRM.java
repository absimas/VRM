package vrm;

import java.util.ArrayList;
import java.util.List;

import vrm.exceptions.InvalidArgumentsException;
import vrm.exceptions.InvalidCommandException;
import vrm.exceptions.MemoryOutOfBoundsException;

/**
 * Created by Simas on 2017 Mar 04.
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public class VRM {

  public static void main(String[] args) throws InterruptedException {
    new VRM().vmProgramExample();
  }

  private static final int INTERRUPT_HANDLER_START_ADDRESS = 600;

  private final List<String> commands = new ArrayList<>();

  private void vmCreationExample() {
    final RealMachine rm = new RealMachine(new Memory(RealMachine.MEMORY_SIZE));

    // Create 3 VMs that use independent memory blocks of the RM
    // Created VMs will have memories: [0..9], [10..19], [20..29]
    for (int i = 0; i < 3; i++) {
      // Allocate VM memory
      final int vmMemoryOffset = i * RealMachine.VM_MEMORY_SIZE;
      final Memory vmMemory = rm.memory.sublist(vmMemoryOffset, vmMemoryOffset + RealMachine.VM_MEMORY_SIZE);

      // Create and save VM page table
      final int vmPageTableOffset = RealMachine.INTERRUPT_TABLE_SIZE + i * RealMachine.VM_MEMORY_SIZE / 10;
      final PageTable vmPageTable = new PageTable(Utils.generateRange(vmMemoryOffset, vmMemoryOffset + RealMachine.VM_MEMORY_SIZE));
      rm.memory.replace(vmPageTableOffset, vmPageTable.table);

      // Create VM
      final VirtualMachine vm = new VirtualMachine(rm, vmMemory);

      // Reference newly created VM as the current one
      rm.virtualMachine = vm;
      rm.PTR = vmPageTableOffset;
    }
  }

  private void commandExample1() {
    new Command(Command.Type.AD, 0, 1, 5); // AD015
    new Command(Command.Type.STVM, 0); // STVM0
    new Command(Command.Type.HALT); // HALT
  }

  private void commandExample2() {
    // Prepare commands that we'll use
    commands.clear();
    commands.add("PD013");
    commands.add("CR013");
    commands.add("AD012");
    commands.add("CP023");
    commands.add("JM024");
    commands.add("CM011");
    commands.add("CR013");
    commands.add("CR011");
    commands.add("CM013");
    commands.add("JP000");

    // Parse commands
    try {
      for (String string : commands) {
        Command.parse(string);
        // Execute command
      }
    } catch (InvalidCommandException | InvalidArgumentsException e) {
      // Interrupt
      e.printStackTrace();
    }
  }

  private void vmProgramExample() throws InterruptedException {
    // Create a RM with a 1000 word memory
    final RealMachine rm = new RealMachine(new Memory(RealMachine.MEMORY_SIZE));

    // Init interruption handlers
    interruptionHandlers(rm);

    // Imitate VM creation command
    rm.memory.replace(RealMachine.MEMORY_SIZE-1, "STVM0");
    rm.IC = RealMachine.MEMORY_SIZE-1;
    rm.step();
    final VirtualMachine vm = rm.virtualMachine;

    // Store a program (fibonacci less than 1000) in VM memory
    final Memory program = vm.memory;
    program.replace(0, "PD013");
    program.replace(1, "CR013");
    program.replace(2, "AD012");
    program.replace(3, "CP023");
    program.replace(4, "JM024");
    program.replace(5, "CM011");
    program.replace(6, "CR013");
    program.replace(7, "CM012");
    program.replace(8, "CR011");
    program.replace(9, "CM013");
    program.replace(10, "JP000");
    program.replace(12, "00000");
    program.replace(13, "00001");
    program.replace(23, "01000");
    program.replace(24, "HALT ");

    // Point IC to the start of the program
    vm.IC = 0;

    // CPU ALGORITHM
    while (true) {
      // 1. Check TI
      if (rm.TI <= 0) {
        timerInterrupt(rm);
        continue;
      }

      // 3. Check IC
      if (vm.IC >= 100) {
        rm.PI = RealMachine.ProgramInterrupt.INV_ADDRESS;
        programInterrupt(rm);
        continue;
      }

      // Save IC
      final int savedIC = vm.IC;

      // 4. Increment IC
      vm.IC++;

      // 2. Read instruction pointed by the saved IC
      final Command command;
      try {
        command = Command.parse(vm.memory.get(savedIC));
      } catch (InvalidCommandException | InvalidArgumentsException e) {
        e.printStackTrace();

        // 5. Invalid instruction
        rm.PI = RealMachine.ProgramInterrupt.INV_OP;
        programInterrupt(rm);
        continue;
      } catch (MemoryOutOfBoundsException e) {
        e.printStackTrace();

        // 6. Invalid address (pointed by IC)
        rm.PI = RealMachine.ProgramInterrupt.INV_ADDRESS;
        programInterrupt(rm);
        continue;
      }

      // 7. Decrement TI
      rm.TI--;

      // 8. Execute instruction
      try {
        vm.execute(command);
      } catch (MemoryOutOfBoundsException e) {
        e.printStackTrace();
        // 9. Invalid address during command execution
        rm.PI = RealMachine.ProgramInterrupt.INV_ADDRESS;
        programInterrupt(rm);
        continue;
      }

      if (!rm.isInterrupted()) {
        // 10. Go to start
        continue;
      }

      // 11. Handle interruptions
      if (rm.SI.ordinal() > 0) {
        superInterrupt(rm, savedIC);
      } else if (rm.PI.ordinal() > 0) {
        programInterrupt(rm);
      } else if (rm.TI <= 0) {
        timerInterrupt(rm);
      } else if (rm.IOI > 0) {
        ioiInterrupt(rm);
      }
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
  private void interruptionHandlers(RealMachine rm) {
    // 0. TI
    int index = 0;
    int address = INTERRUPT_HANDLER_START_ADDRESS;
    Word[] words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("TI   ") };
    rm.memory.replace(address, words);
    rm.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 1. Incorrect address - PI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("AdrPI") };
    rm.memory.replace(address, words);
    rm.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 2. Incorrect operation - PI
    index++;
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("OprPI") };
    rm.memory.replace(address, words);
    rm.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 3. Overflow - PI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("OFLOW") };
    rm.memory.replace(address, words);
    rm.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 4. Incorrect assignment - PI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("AsiPI") };
    rm.memory.replace(address, words);
    rm.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 5-10. Super interrupts. They don't have a custom handler.

    // 11. 1st channel work end - IOI
    address += words.length;
    index = 11;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("1cIOI") };
    rm.memory.replace(address, words);
    rm.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 12. 2nd channel work end - IOI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("2cIOI") };
    rm.memory.replace(address, words);
    rm.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));

    // 13. 2nd channel work end - IOI
    address += words.length;
    words = new Word[] { new Word("PD" + Utils.precedeZeroes(address+2, Word.LENGTH-2)), new Word("STVM0"), new Word("3cIOI") };
    rm.memory.replace(address, words);
    rm.memory.replace(index++, new Word(Utils.precedeZeroes(address, Word.LENGTH)));
  }

  private void timerInterrupt(RealMachine rm) throws InterruptedException {
    // Get handler address
    int address = rm.memory.get(0).toNumber();

    // Set the handler's address as IC
    rm.IC = address;

    // ToDo include the new commands into command list so they're displayed and we know what's going on

    // Super mode
    rm.MODE = RealMachine.Mode.S;

    Command command = rm.stepQuietly();
    while (command.type != Command.Type.STVM && command.type != Command.Type.HALT) {
      // ToDo wait here until > is clicked
      // Move to the next instruction
      rm.IC++;
      command = rm.step();
    }

    // Reset mode
    rm.MODE = RealMachine.Mode.U;

    // Reset TI
    rm.TI = RealMachine.DEFAULT_TIMER;
  }

  private void programInterrupt(RealMachine rm) throws InterruptedException {
    // Get handler index
    int address;
    switch (rm.PI) {
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
    address = rm.memory.get(address).toNumber();

    // Set the handler's address as IC
    rm.IC = address;

    // ToDo include the new commands into command list so they're displayed and we know what's going on

    // Super mode
    rm.MODE = RealMachine.Mode.S;

    Command command = rm.stepQuietly();
    while (command.type != Command.Type.STVM && command.type != Command.Type.HALT) {
      // ToDo wait here until > is clicked
      // Move to the next instruction
      rm.IC++;
      command = rm.step();
    }

    // Reset mode
    rm.MODE = RealMachine.Mode.U;

    // Clear PI
    rm.PI = RealMachine.ProgramInterrupt.NONE;
  }

  /**
   * When a VM can't execute a command, we duplicate saved IC value and execute the command in the RM.
   * When RM executes a command that's located in the context of a VM, the argument is converted to an absolute address.
   * @param rm       machine that will take over the execution
   * @param failedIC IC value pointing to the failing command
   */
  private void superInterrupt(RealMachine rm, int failedIC) throws InterruptedException {

    // Convert the saved IC into an absolute address
    rm.IC = rm.getAbsoluteAddress(failedIC);

    // ToDo include the new commands into command list so they're displayed and we know what's going on

    // Execute the command, now in the RM
    rm.step();

    // Unprivileged command was executed in the RM.
    // We can now clear the SI register.
    rm.SI = RealMachine.SuperInterrupt.NONE;
  }

  private void ioiInterrupt(RealMachine rm) throws InterruptedException {
    // Handle all channels
    ioiInterrupt(rm, 1);
    ioiInterrupt(rm, 2);
    ioiInterrupt(rm, 3);
  }

  /**
   * @param channel channel that finished its work ([1..3])
   */
  private void ioiInterrupt(RealMachine rm, int channel) throws InterruptedException {
    if (!Utils.checkFlag(rm.IOI, channel)) return;

    // Get handler index
    int address = 10 + channel;

    // Get handler address
    address = rm.memory.get(address).toNumber();

    // Set the handler's address as IC
    rm.IC = address;

    // ToDo include the new commands into command list so they're displayed and we know what's going on

    // Super mode
    rm.MODE = RealMachine.Mode.S;

    Command command = rm.stepQuietly();
    while (command.type != Command.Type.STVM && command.type != Command.Type.HALT) {
      // ToDo wait here until > is clicked
      // Move to the next instruction
      rm.IC++;
      command = rm.step();
    }

    // Reset mode
    rm.MODE = RealMachine.Mode.U;
    
    // Clear given channel from bitmask
    Utils.clearFlag(rm.IOI, channel);
  }

}
