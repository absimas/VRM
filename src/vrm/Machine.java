package vrm;

import vrm.exceptions.MemoryOutOfBoundsException;
import vrm.exceptions.NumberOverflowException;
import vrm.exceptions.UnhandledCommandException;

/**
 * Main class that allows:
 * <ul>
 *   <li>{@link Command} execution</li>
 * </ul>
 */
public abstract class Machine {

  /**
   * Instruction Counter. Points to a memory address of the currently executed instruction.
   */
  public int IC;
  /**
   * Memory.
   */
  public final Memory memory;

  /**
   * Temporary. Size word.
   */
  public Word TMP = new Word("00000");
  /**
   * Comparison result. Size 1 byte.
   */
  public Comparison C = Comparison.EQUAL;

  protected Machine(Memory memory) {
    this.memory = memory;
  }

  /**
   * Execute a specific command in this machine.
   * @param command  command to be executed
   * @throws UnhandledCommandException when a command wasn't handled by this machine for whatever the causes (e.g. inadequate privileges)
   * @throws MemoryOutOfBoundsException when a command refers to a word outside the visible memory
   * @throws NumberFormatException when either {@link #TMP} or referenced memory contains something that's not a number
   * @throws NumberOverflowException when number arithmetic result does not fit in a word
   */
  protected void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException, NumberFormatException, NumberOverflowException, InterruptedException {
    switch (command.type) {
      case CR:
        TMP = memory.get(command.getArgument());
        IC++;
        break;
      case CM:
        memory.replace(command.getArgument(), TMP);
        IC++;
        break;
      case AD: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Add
        final String result = String.valueOf(tmp + mem);


        // Overflow
        if (result.length() > Word.MAX_LENGTH) {
          throw new NumberOverflowException(String.format("%d + %d = %s which does not fit within a word!", tmp, mem, result));
        }

        // Save the result in TMP
        TMP = new Word(result);

        IC++;
        break;
      }
      case SB: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Subtract
        final String result = String.valueOf(tmp - mem);

        // Overflow
        if (result.length() > Word.MAX_LENGTH) {
          throw new NumberOverflowException(String.format("%d - %d = %s which does not fit within a word!", tmp, mem, result));
        }

        // Save the result in TMP
        TMP = new Word(result);

        IC++;
        break;
      }
      case ML: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Multiply
        final String result = String.valueOf(tmp * mem);

        // Overflow
        if (result.length() > Word.MAX_LENGTH) {
          throw new NumberOverflowException(String.format("%d * %d = %s which does not fit within a word!", tmp, mem, result));
        }

        // Save the result in TMP
        TMP = new Word(result);

        IC++;
        break;
      }
      case MD: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Mod
        final String result = String.valueOf(tmp % mem);

        // Save the result in TMP
        TMP = new Word(result);

        IC++;
        break;
      }
      case DV: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Overflow (division by 0)
        if (mem == 0) {
          throw new NumberOverflowException(String.format("%d / %d. Division by zero!", tmp, mem));
        }

        // Divide
        final String result = String.valueOf(tmp / mem);

        // Save the result in TMP
        TMP = new Word(result);

        IC++;
        break;
      }
      case CP:
        // Convert
        final String tmp = TMP.toString();
        final String mem = memory.get(command.getArgument()).toString();

        final int c = tmp.compareTo(mem);
        if (c == 0) {
          C = Comparison.EQUAL;
        } else if (c > 0) {
          C = Comparison.MORE;
        } else {
          C = Comparison.LESS;
        }

        IC++;
        break;
      case JP:
        // Overflow
        if (command.getArgument() > memory.size()) {
          throw new MemoryOutOfBoundsException("JP referenced an invalid memory point: " + command.getArgument());
        }

        // Write IC
        IC = command.getArgument();
        break;
      case JE:
        // If not equal, increment IC and leave
        if (C != Comparison.EQUAL) {
          IC++;
          break;
        }
        // Execute JP
        execute(new Command(Command.Type.JP, command.getArguments()));
        break;
      case JL:
        // If not less, increment IC and leave
        if (C != Comparison.LESS) {
          IC++;
          break;
        }
        // Execute JP
        execute(new Command(Command.Type.JP, command.getArguments()));
        break;
      case JM:
        // If not more, increment IC and leave
        if (C != Comparison.MORE) {
          IC++;
          break;
        }
        // Execute JP
        execute(new Command(Command.Type.JP, command.getArguments()));
        break;
      default:
        throw new IllegalStateException(String.format("Tried to handle an unexpected command %s in %s!", command, this));
    }
  }

  /**
   * Executes instruction pointed by {@code #IC}.
   * This method returns false if the executing the current instruction caused an interruption.
   * @return true if the instruction was executed successfully, false otherwise.
   * @throws UnhandledCommandException when a command cannot be handled in this machine. E.g. STVMx in a VM.
   * @throws InterruptedException when a machine block (e.g. when waiting for a channel) is interrupted
   */
  public abstract boolean step() throws UnhandledCommandException, InterruptedException;

}