package vrm.exceptions;

import com.sun.istack.internal.NotNull;

import vrm.Command;

/**
 * Exception thrown a {@link Command} is unhandled.
 */
public class UnhandledCommandException extends RuntimeException {

  private final Command command;

  public UnhandledCommandException(@NotNull Command command) {
    this.command = command;
  }

  public UnhandledCommandException(@NotNull Command command, String message) {
    super(message);
    this.command = command;
  }

  public UnhandledCommandException(@NotNull Command command, String message, Throwable cause) {
    super(message, cause);
    this.command = command;
  }

  public Command getCommand() {
    return command;
  }

}