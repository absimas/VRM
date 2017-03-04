package vrm;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class VirtualMachine {

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

}
