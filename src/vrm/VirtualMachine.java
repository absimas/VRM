package vrm;

import javafx.collections.ObservableList;
import vrm.exceptions.MemoryOutOfBoundsException;
import vrm.exceptions.NumberOverflowException;
import vrm.exceptions.UnhandledCommandException;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class VirtualMachine extends Machine {

  /**
   * Counter for instantiated VMs.
   */
  private static int VM_IDS = 0;

  /**
   * Reference to a RM that handles this VM.
   */
  private final RealMachine realMachine;
  /**
   * Unique id for this VM instance.
   */
  private final int id = VM_IDS++;

  public VirtualMachine(ObservableList<String> commandLog, RealMachine realMachine, Memory memory) {
    super(commandLog, memory);
    this.realMachine = realMachine;
  }

  @Override
  protected synchronized void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException, InterruptedException {
    // Log command
    commandLog.add(String.format("%s in %s", command.toString(), this));
    System.out.println("Execute " + command + " in " + this);

    // Commands executed in a VM must have an x argument of 0
    if (command.x != 0) {
      throw new MemoryOutOfBoundsException(String.format("Invalid command address encountered when executing %s!", command));
    }

    switch (command.type) {
      // Other commands invoke the default handling
      default:
        try {
          super.execute(command);
        } catch (NumberOverflowException e) {
          e.printStackTrace();
          realMachine.PI = RealMachine.ProgramInterrupt.OVERFLOW;
        }
        wait();
        return;
      case HALT:
        realMachine.SI = RealMachine.SuperInterrupt.HALT;
        realMachine.haltVM(this);
        wait();
        return;
      case GD:
        realMachine.SI = RealMachine.SuperInterrupt.GD;
        break;
      case PD:
        realMachine.SI = RealMachine.SuperInterrupt.PD;
        break;
      case RD:
        realMachine.SI = RealMachine.SuperInterrupt.RD;
        break;
      case WD:
        realMachine.SI = RealMachine.SuperInterrupt.WD;
        break;
      case SD:
        realMachine.SI = RealMachine.SuperInterrupt.SD;
        break;
      // The following commands lead to a VM modification but cannot be executed internally.
      case STVM: case SVRG: case LDRG:
        throw new UnhandledCommandException(command, String.format("Command %s wasn't handled in %s.", command, this));
    }

    // All commands that require super privileges invoke a delay to wait for the RM to suspend this VM and then for the command to be executed as super
    wait();
    realMachine.suspendVM(this);
    wait();
  }

  @Override
  public String toString() {
    return String.format("VM%d", id);
  }

  /**
   * Save registers in the last block of this VM's memory.
   * Registers are saved in the last block (word 91 and 92) of VM memory.
   */
  public void saveRegisters() {
    // TMP as String
    final String tmp = TMP.toString();
    // VM's IC register's max length is 2
    final String ic = Utils.precedeZeroes(IC, 2);
    // C as String
    final String c = String.valueOf(C.ordinal());
    // Complete string
    final String registers = String.format("%s%s%s00", tmp, ic, c);

    System.out.println("SaveReg: " + registers);

    // VM memory size is 10 blocks = 100 words
    // Last block is where we save the registers
    memory.replace(91, new Word(registers.substring(0, 5)));
    memory.replace(92, new Word(registers.substring(5, 10)));
  }

  /**
   * Restore registers from the last block of this VM's memory.
   * Registers are saved in the last block (word 91 and 92) of VM memory.
   */
  public void restoreRegisters() {
    // Complete string
    final String registers = String.format("%s%s", memory.get(91).toString(), memory.get(92).toString());

    System.out.println("RestoreReg: " + registers);

    TMP = new Word(registers.substring(0, 5));
    IC = Integer.valueOf(registers.substring(5, 7));
    C = Comparison.values()[Character.getNumericValue(registers.charAt(7))];
  }

}