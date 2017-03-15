package vrm;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class VRM {

  /**
   * VM memory size in words.
   */
  public static final int VM_MEMORY_SIZE = 100;
  /**
   * RM memory size in words.
   */
  public static final int RM_MEMORY_SIZE = 1000;

  public static void main(String[] args) {

  }

  private void machineExample() {
    final RealMachine rm = new RealMachine(new Memory(RM_MEMORY_SIZE));

    // Create 3 VMs that use independent memory blocks of the RM
    // Created VMs will have memories: [0..9], [10..19], [20..29]
    for (int i = 0; i < 3; i++) {
      final Memory memory = rm.memory.sublist(i * VM_MEMORY_SIZE, VM_MEMORY_SIZE);
      final VirtualMachine vm1 = new VirtualMachine(rm, memory);
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

  private void vmProgramExample() {
    // Create a RM with a 100 word memory
    final RealMachine rm = new RealMachine(new Memory(100));

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
        e.printStackTrace();
        // ToDo Interrupt
        continue;
      }

      // Execute command
      try {
        // First try in a VM
        vm.execute(command);
      } catch (UnhandledCommandException e) {
        e.printStackTrace();
        // When VM fails to handle - execute in the RM
        rm.execute(command);
      }

      // ToDo isInterrupted() to check RM interrupts
    }


  }

}
