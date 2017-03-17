package vrm;

import vrm.exceptions.MemoryOutOfBoundsException;
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

  public VirtualMachine(RealMachine realMachine, Memory memory) {
    super(memory);
    this.realMachine = realMachine;
  }

  @Override
  protected void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException, InterruptedException {
    System.out.println("Execute " + command + " in " + this);
    // Commands executed in a VM must have an x argument of 0
    if (command.x != 0) {
      throw new MemoryOutOfBoundsException(String.format("Invalid command address encountered when executing %s!", command));
    }

    switch (command.type) {
      case HALT:
        realMachine.SI = RealMachine.SuperInterrupt.HALT;
        break;
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
      // Other commands invoke the default handling
      default:
        super.execute(command);
        break;
    }
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
    final String tmp = String.format("%5s", TMP.toString());
    // VM's IC register's max length is 2
    final String ic = Utils.precedeZeroes(IC, 2);
    // C as String
    final String c = String.valueOf(C.ordinal());
    // Complete string
    final String registers = String.format("%s%s%s00", tmp, ic, c);

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

    TMP = new Word(registers.substring(0, 5));
    IC = Integer.valueOf(registers.substring(5, 7));
    C = Comparison.values()[Character.getNumericValue(registers.charAt(7))];
  }

}