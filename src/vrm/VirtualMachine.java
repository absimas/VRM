package vrm;

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
  public void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException {
    switch (command.type) {
      // Commands that require super privileges - throw // ToDo note that the command arguments wll need to be converted using PLR
      case GD: case PD: case RD: case WD: case SD: case HALT:
        throw new UnhandledCommandException(command, String.format("Command %s wasn't handled in %s.", command, this));
      // ToDo can we forbid the following commands in a VM?
      case GT: case PT: case STVM: case SVRG: case LDRG:
        throw new UnhandledCommandException(command, String.format("Command %s shouldn't be handled virtually.", command));
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

}