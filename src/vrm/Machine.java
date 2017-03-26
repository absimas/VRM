package vrm;

import javafx.collections.ObservableList;
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
   * List containing all executed commands. This is managed for UI purposes only.
   */
  protected final ObservableList<String> commandLog;

  /**
   * Temporary. Size word.
   */
  public Word TMP = new Word("00000");
  /**
   * Comparison result. Size 1 byte.
   */
  public Comparison C = Comparison.EQUAL;

  protected Machine(ObservableList<String> commandLog, Memory memory) {
    this.commandLog = commandLog;
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
  protected synchronized void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException, NumberFormatException, NumberOverflowException, InterruptedException {
    switch (command.type) {
      case CR:
        TMP = memory.get(command.getArgument());
        break;
      case CM:
        memory.replace(command.getArgument(), TMP);
        break;
      case AD: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Add
        final int result = tmp + mem;


        // Overflow
        if (String.valueOf(result).length() > Word.LENGTH) {
          throw new NumberOverflowException(String.format("%d + %d = %s which does not fit within a word!", tmp, mem, result));
        }

        // Save the result in TMP
        TMP = new Word(Utils.precedeZeroes(result, Word.LENGTH));

        break;
      }
      case SB: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Subtract
        final int result = tmp - mem;

        // Overflow
        if (String.valueOf(result).length() > Word.LENGTH) {
          throw new NumberOverflowException(String.format("%d - %d = %s which does not fit within a word!", tmp, mem, result));
        }

        // Save the result in TMP
        TMP = new Word(Utils.precedeZeroes(result, Word.LENGTH));

        break;
      }
      case ML: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Multiply
        final int result = tmp * mem;

        // Overflow
        if (String.valueOf(result).length() > Word.LENGTH) {
          throw new NumberOverflowException(String.format("%d * %d = %s which does not fit within a word!", tmp, mem, result));
        }

        // Save the result in TMP
        TMP = new Word(Utils.precedeZeroes(result, Word.LENGTH));

        break;
      }
      case MD: {
        // Convert
        final int tmp = TMP.toNumber();
        final int mem = memory.get(command.getArgument()).toNumber();

        // Mod
        final int result = tmp % mem;

        // Save the result in TMP
        TMP = new Word(Utils.precedeZeroes(result, Word.LENGTH));

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
        final int result = tmp / mem;

        // Save the result in TMP
        TMP = new Word(Utils.precedeZeroes(result, Word.LENGTH));

        break;
      }
      case CP:
        // "2" > "10" happens because:
        // https://docs.oracle.com/javase/7/docs/api/java/lang/String.html#compareTo(java.lang.String)
        // "If there is no index position at which they differ, then the shorter string lexicographically precedes the longer string."
        // But in our case all strings will be the same length (LENGTH), so we can ignore this problem.
        final String tmp = TMP.toString();
        final String mem = memory.get(command.getArgument()).toString();//?
        int cp = tmp.compareTo(mem);
        if (cp == 0) {
          C = Comparison.EQUAL;
        } else if (cp > 0) {
          C = Comparison.MORE;
        } else {
          C = Comparison.LESS;
        }
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
        if (C != Comparison.EQUAL) break;
        IC = command.getArgument();
        break;
      case JL:
        // If not less, increment IC and leave
        if (C != Comparison.LESS) break;
        IC = command.getArgument();
        break;
      case JM:
        if (C != Comparison.MORE) break;
        IC = command.getArgument();
        break;
      default:
        throw new IllegalStateException(String.format("Tried to handle an unexpected command %s in %s!", command, this));
    }
  }

}