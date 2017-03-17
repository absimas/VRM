package vrm;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import vrm.exceptions.InvalidArgumentsException;
import vrm.exceptions.InvalidCommandException;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class Command {

  public enum Type {
    CR(3), CM(3), AD(3), SB(3), ML(3), MD(3), DV(3), CP(3), JP(3), JE(3), JL(3), JM(3), HALT(0), GD(3), PD(3), RD(3), WD(3), SD(3), STVM(1), SVRG(1), LDRG(1);

    public final int argCount;

    Type(int argCount) {
      this.argCount = argCount;
    }
  }

  /**
   * Command type.
   */
  public final Type type;
  /**
   * Command arguments. Number calculation formula: x * 100 + y * 10 + z.
   *
   * <br>{@code 0} = unused argument
   */
  public final int x, y, z;

  public Command(@NotNull Type type, @Nullable int... args) {
    this.type = type;

    int argCount = 0;
    if (args != null) argCount = args.length;
    if (type.argCount != argCount) {
      throw new IllegalArgumentException(String.format("Incorrect amount of arguments (%d) specified to %s. Expecting %d.", argCount, type, type.argCount));
    }

    // No args
    if (argCount == 0) {
      x = y = z = 0;
      return;
    }

    // First arg
    this.x = args[0];
    if (argCount == 1) {
      y = z = 0;
      return;
    }

    // Second arg
    this.y = args[1];
    if (argCount == 2) {
      z = 0;
      return;
    }

    // Third arg
    this.z = args[2];
  }

  /**
   * Convert fields {@link #x}, {@link #y} and {@link #z} to a complete argument using the formula: x * 100 + y * 10 + z.
   * @return complete argument
   */
  public int getArgument() {
    return x * 100 + y * 10 + z;
  }

  /**
   * Convenience method to put all argument fields {@link #x}, {@link #y} and {@link #z} into an array.
   * @return array of {@link #x}, {@link #y} and {@link #z}
   */
  public int[] getArguments() {
    return new int[] { x, y, z };
  }

  /**
   * Convert a {@link Word} into a {@link Command}.
   * @param word word to be parsed
   * @return parsed command
   * @throws InvalidCommandException thrown when command couldn't be recognized
   * @throws InvalidArgumentsException thrown when command was recognized but its arguments were invalid
   */
  @Nullable
  public static Command parse(@NotNull Word word) throws InvalidCommandException, InvalidArgumentsException {
    final String stringWord = word.toString();

    final int[] args;
    for (Type type : Type.values()) {
      if (stringWord.startsWith(type.name())) {
        args = extractArguments(word, type);
        return new Command(type, args);
      }
    }

    throw new InvalidCommandException("Unrecognized command from word: " + word);
  }

  /**
   * Convert a {@link String} into a {@link Command}.
   * @param string word to be parsed
   * @return parsed command
   * @throws InvalidCommandException thrown when command couldn't be recognized
   * @throws InvalidArgumentsException thrown when command was recognized but its arguments were invalid
   */
  @Nullable
  public static Command parse(@NotNull String string) throws InvalidCommandException, InvalidArgumentsException {
    final int[] args;
    for (Type type : Type.values()) {
      if (string.startsWith(type.name())) {
        args = extractArguments(string, Type.CR);
        return new Command(type, args);
      }
    }

    throw new InvalidCommandException("Unrecognized command from word: " + string);
  }

  /**
   * Extract arguments from a word.
   * @param word word that contains the command and its arguments.
   * @param type command type
   * @return extracted arguments parsed into an int[]
   * @throws InvalidArgumentsException when type argument count doesn't match this word or when the arguments cannot be parsed into integers
   */
  @Nullable
  public static int[] extractArguments(@NotNull Word word, Type type) throws InvalidArgumentsException {
    return extractArguments(word.toString(), type);
  }

  /**
   * Extract arguments from a word.
   * @param string string that contains the command and its arguments.
   * @param type   command type
   * @return extracted arguments parsed into an int[]
   * @throws InvalidArgumentsException when type argument count doesn't match this word or when the arguments cannot be parsed into integers
   */
  @Nullable
  public static int[] extractArguments(@NotNull String string, Type type) throws InvalidArgumentsException {
    // If this command isn't supposed to have any arguments, ignore the remaining word.
    if (type.argCount == 0) return null;

    // Remove command from word. E.g. AD105 becomes 105.
    final String arguments = string.substring(type.name().length(), string.length());

    // Ensure char count matches the expected argument count
    if (type.argCount != arguments.length()) {
      throw new InvalidArgumentsException(String.format("Invalid arguments detected for type %s. Word was %s", type, string));
    }

    // Extract arguments converted to ints
    final int[] args = new int[arguments.length()];
    try {
      for (int i = 0; i < arguments.length(); i++) {
        args[i] = Integer.parseInt(String.valueOf(arguments.charAt(i)));
      }
    } catch (NumberFormatException ignored) {
      throw new InvalidArgumentsException(String.format("Invalid arguments detected for type %s. Word was %s", type, string));
    }

    return args;
  }

  @Override
  public String toString() {
    String string = String.valueOf(type);
    if (type.argCount > 0) {
      string += x;
      if (type.argCount > 1) {
        string += y;
        if (type.argCount > 2) {
          string += z;
        }
      }
    }
    return string;
  }

}
