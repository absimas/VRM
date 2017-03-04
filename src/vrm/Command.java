package vrm;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class Command {

  public enum Type {

    CR(3), CM(3), AD(3), SB(3), ML(3), MD(3), DV(3), CP(3), JP(3), JE(3), JL(3), JM(3), HALT(0), GD(3), PD(3), RD(3), WD(3), SD(3), GT(3), PT(3), STVM(1), SVRG(1), LDRG(1);

    private final int argCount;

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
   * <br>{@code null} = unused argument
   */
  public final Integer x, y, z;

  public Command(@NotNull Type type, @Nullable Integer... args) {
    this.type = type;

    int argCount = 0;
    if (args != null) argCount = args.length;
    if (type.argCount != argCount) {
      throw new IllegalArgumentException(String.format("Incorrect amount of arguments (%d) specified to %s. Expecting %d.", argCount, type, type.argCount));
    }

    // No args
    if (argCount == 0) {
      x = y = z = null;
      return;
    }

    // First arg
    this.x = args[0];
    if (argCount == 1) {
      y = z = null;
      return;
    }

    // Second arg
    this.y = args[1];
    if (argCount == 2) {
      z = null;
      return;
    }

    // Third arg
    this.z = args[2];
  }

}
