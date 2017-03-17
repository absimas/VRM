package vrm;

import java.util.ArrayList;
import java.util.List;

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

    // Imitate VM creation command
    rm.memory.replace(0, "STVM0");
    rm.IC = 0;
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
    program.replace(12, "0");
    program.replace(13, "1");
    program.replace(23, "1000");
    program.replace(24, "HALT");

    // Point IC to the start of the program
    vm.IC = 0;

    // Loop execution
    while (true) {
      // Execute VM
      vm.step();

      // No interruptions - nothing to do
      if (!rm.isInterrupted()) continue;

      if (rm.SI.ordinal() > 0) {
        // When a VM can't execute a command, we duplicate the IC and try executing the same command in the RM.
        // When RM executes a command that's located in the context of a VM, the argument is converted to an absolute address.

        // Switch to super mode
        rm.MODE = RealMachine.Mode.S;

        // Convert VMs IC into an absolute address
        rm.IC = rm.getAbsoluteAddress(vm.IC);

        // Execute the command, now in the RM
        rm.step();

        // Unprivileged command was executed in the RM.
        // Now we can move on with the VM execution so we increment its IC and clear the SI register.
        vm.IC++;
        rm.SI = RealMachine.SuperInterrupt.NONE;
      } else if (rm.PI.ordinal() > 0) {
        // Command execution failed
        // Clear PI
        rm.PI = RealMachine.ProgramInterrupt.NONE;

        // ToDo run PI program
        // ToDo include the new commands into command list so they're displayed and we know what's going on
      } else if (rm.TI <= 0) {
        // Timer too low

        // ToDo run TI program
        // ToDo include the new commands into command list so they're displayed and we know what's going on

        // Reset timer
        rm.TI = RealMachine.DEFAULT_TIMER;
      } else if (rm.IOI > 0) {
        // I/O interruption
        // Clear IOI
        rm.IOI = 0;

        // ToDo I/O interrupt program
        // ToDo include the new commands into command list so they're displayed and we know what's going on
      }
    }
  }

}
