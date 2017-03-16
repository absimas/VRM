package vrm;

import vrm.exceptions.InvalidArgumentsException;
import vrm.exceptions.InvalidCommandException;

/**
 * Created by Simas on 2017 Mar 04.
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public class VRM {

  public static void main(String[] args) throws InterruptedException {
    new VRM().vmProgramExample();
  }

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
    // Memory consisting 10 words
    final Memory memory = new Memory(10);

    // Fill words
    memory.replace(0, "PD013");
    memory.replace(1, "CR013");
    memory.replace(2, "AD012");
    memory.replace(3, "CP023");
    memory.replace(4, "JM024");
    memory.replace(5, "CM011");
    memory.replace(6, "CR013");
    memory.replace(7, "CR011");
    memory.replace(8, "CM013");
    memory.replace(9, "JP000");

    // Parse words into commands
    try {
      for (Word word : memory) {
        Command.parse(word);
        // Execute command
      }
    } catch (InvalidCommandException | InvalidArgumentsException e) {
      // Interrupt
      e.printStackTrace();
    }
  }

  private void vmProgramExample() throws InterruptedException {
    // Create a RM with a 100 word memory
    final RealMachine rm = new RealMachine(new Memory(RealMachine.MEMORY_SIZE));

    // Create a VM
    rm.execute(new Command(Command.Type.STVM, 0));
    final VirtualMachine vm = rm.virtualMachine;

    // Program of 10 words
    final Memory program = new Memory(10);
    program.replace(0, "PD013");
    program.replace(1, "CR013");
    program.replace(2, "AD012");
    program.replace(3, "CP023");
    program.replace(4, "JM024");
    program.replace(5, "CM011");
    program.replace(6, "CR013");
    program.replace(7, "CR011");
    program.replace(8, "CM013");
    program.replace(9, "JP000");

    // Loop each word in the program
    for (Word word : program) {
      // Convert word to a command
      final Command command;
      try {
        command = Command.parse(word);
      } catch (InvalidCommandException | InvalidArgumentsException e) {
        // Parsing the command failed
        e.printStackTrace();

        // Set PI
//        rm.PI = RealMachine.ProgramInterrupt.INV_OP;

        // Clear PI
//        rm.PI = null;

        // ToDo run program interrupt program
        continue;
      }

      // Execute command in a VM
      vm.execute(command);

      if (rm.SI != null) {
        // An unprivileged interruption
        // Clear SI
        rm.SI = null;

        // Get an absolute address
        final String absolute = Utils.precedeZeroes(rm.getAbsoluteAddress(command.getArgument()), 3);

        // Create a modified command
        final Command cmd = new Command(command.type,
            Character.getNumericValue(absolute.charAt(0)),
            Character.getNumericValue(absolute.charAt(1)),
            Character.getNumericValue(absolute.charAt(2)));

        // Execute the command in the RM
        rm.execute(cmd);
      } else if (rm.PI != null) {
        // Command execution failed
        // Clear PI
        rm.PI = null;

        // ToDo run program interrupt program
      } else if (rm.TI <= 0) {
        // Timer too low

        // ToDo run timer interrupt program

        // Reset timer
        rm.TI = RealMachine.DEFAULT_TIMER;
      } else if (rm.IOI > 0) {
        // I/O interruption
        // Clear IOI
        rm.IOI = 0;

        // ToDo I/O interrupt program
      }
    }


  }

}
