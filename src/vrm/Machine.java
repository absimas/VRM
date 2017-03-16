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
   * Instruction Counter. Length specifies the maximum instruction size.
   * E.g. length == 2 means [0..99], while length == 3 means [0..999].
   */
  public final int[] IC;
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

    // Create an IC big enough to cover all the memory
    IC = new int[String.valueOf(this.memory.size()).length()];
  }

  /**
   * Execute a specific command in this machine.
   * @param command  command to be executed
   * @throws UnhandledCommandException when a command wasn't handled by this machine for whatever the causes (e.g. inadequate privileges)
   * @throws MemoryOutOfBoundsException when a command refers to a word outside the visible memory
   * @throws NumberFormatException when either {@link #TMP} or referenced memory contains something that's not a number
   * @throws NumberOverflowException when number arithmetic result does not fit in a word
   */
  public void execute(Command command) throws UnhandledCommandException, MemoryOutOfBoundsException, NumberFormatException, NumberOverflowException, InterruptedException {
    System.out.println("command = [" + command + "] from " + this);
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
          throw new NumberOverflowException(String.format("%d + %d = %d which does not fit within a word!", tmp, mem, result));
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
          throw new NumberOverflowException(String.format("%d - %d = %d which does not fit within a word!", tmp, mem, result));
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
          throw new NumberOverflowException(String.format("%d * %d = %d which does not fit within a word!", tmp, mem, result));
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
        break;
      case JP: {
        String ic = String.valueOf(command.getArgument());
        // Overflow
        if (ic.length() > IC.length) {
          throw new MemoryOutOfBoundsException("JP referenced an invalid memory point: " + ic);
        }

        // Precede with zeroes
        ic = Utils.precedeZeroes(ic, IC.length);

        // Write IC
        for (int i = 0; i < ic.length(); i++) {
          IC[i] = ic.charAt(i);
        }
        break;
      }
      case JE:
        if (C != Comparison.EQUAL) break;
        // Execute JP
        execute(new Command(Command.Type.JP, command.getArguments()));
        break;
      case JL:
        if (C != Comparison.LESS) break;
        // Execute JP
        execute(new Command(Command.Type.JP, command.getArguments()));
        break;
      case JM:
        if (C != Comparison.MORE) break;
        // Execute JP
        execute(new Command(Command.Type.JP, command.getArguments()));
        break;
      default:
        throw new IllegalStateException(String.format("Tried to handle an unexpected command %s in %s!", command, this));
    }
  }

}