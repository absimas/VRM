package vrm;

import com.sun.istack.internal.NotNull;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class VirtualMachine {

  /**
   * Real machine memory size in words.
   */
  public static final int VM_MEMORY_SIZE = 100;

  /**
   * Temporary. Size 5 bytes.
   */
  public int[] TMP = new int[] { 0, 0, 0, 0, 0 };
  /**
   * Instruction Counter. [0..99]. Size 2 bytes.
   */
  public int[] IC = new int[] { 0, 0 };
  /**
   * Comparison result. Size 1 byte.
   */
  public Comparison C = Comparison.EQUAL;

  /**
   * Memory in words. The size is determined by {@link #VM_MEMORY_SIZE}.
   */
  private final Word[] MEMORY;

  /**
   *
   * @param memory
   */
  public VirtualMachine(@NotNull Word[] memory) {
    if (memory.length != VM_MEMORY_SIZE) {
      throw new IllegalArgumentException("VM memory must be of size " + VM_MEMORY_SIZE);
    }

    MEMORY = memory;
  }

}