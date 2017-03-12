package vrm;

/**
 * ToDo make this an extension of VM. The memory will be passed by the creator, hmmm who creates the RM?
 * Let's say someone creates a RM and gives it memory. Then VM ban be a direct descendant of it.
 * BUT, who creates the VMs? Is it the same entity that created RM?
 *   Let's say that for now VM's are a part of VRM class. VRM creates the RM and the VM.
 *     Then, when VM encounters STVM, we throw and go up to the VRM which will then create a new VM. Then the control goes back to VM?
 *       Klausimas: Turime komandą sukurti VM. Ar tą turime pavaizduoti savo programoje ar galime teigti, kad VMai jau sukurti?
 *         Jei turime leisti kurti VMus, kur juos saugoti?
 *           VRM.VM[]
 *           VRM creates VMs and stores them in VM[]
 *           Each VM is used to execute programs
 *           When VMs throw, VRM handles and possibly gives control to RM (no control passing for now?)
 *             But shouldn't this be the work of the OS? Maybe VRM will later become the OS? But no. VMs should be created by the OS and the OS should be created by the RM.
 *               Thus, if VM throws, OS receives the error.
 *               ----
 *               For now, if VM throws, VRM receives and transfers control to RM to handle the error.
 *               If RM encounters STVM(1), it also throws and asks VRM to create a VM.
 *               ----
 * The CREATOR also needs to catch the interrupt exceptions thrown when executing commands in VM.
 * When VM throws when executing a command, RM comes to control and executes interruption paprogramę.
 *   RM comes into control - undefined for now.
 */
public class RealMachine extends Machine {

  public enum Mode {
    /**
     * Supervisor
     */
    S,
    /**
     * User
     */
    U
  }



  /**
   * Program Interruptions caused internal and source code problems.
   */
  public enum ProgramInterrupt {
    /**
     * Invalid memory address.
     */
    INV_ADDRESS,
    /**
     * Invalid operation code.
     */
    INV_OP,
    OVERFLOW,
    /**
     * Invalid/Incorrect assignment.
     */
    INV_ASSIGN
  }

  /**
   * Default timer value. [0..99].
   */
  public static final int DEFAULT_TIMER = 10;

  /**
   * Current VM memory Page Table Register. Size 4 bytes. ToDo change to a reference to VM or PTR class.
   */
  public int[] PTR = new int[] { 0, 0, 0, 0 };
  /**
   * Program Interrupt. Default value = null. Size 1 byte.
   */
  public ProgramInterrupt PI;
  /**
   * Timer Interrupt. [0.99]. Size 1 byte.
   */
  public int TI = DEFAULT_TIMER;
  /**
   * Super Interrupt. Default value = null. Size 1 byte.
   */
  public SuperInterrupt SI;
  /**
   * I/O Interrupt. Bitmask [1..3]. 1 byte.
   */
  public int IOI = 0;
  /**
   * Processor mode. 1 byte.
   */
  public Mode MODE = Mode.U;
  /**
   * Busy channels. Bitmask [1..3]. 1 Byte.
   */
  public int BUSY = 0;

  protected RealMachine(Memory memory) {
    super(memory);
  }

  /**
   * Check whether a channel has been marked as busy.
   * @param i channel number [1..3]
   * @return true if channel is busy, false otherwise.
   */
  public boolean isChannelBusy(int i) {
    if (i < 1 || i > 3) {
      throw new IllegalArgumentException("Busy state for an invalid channel: " + i);
    }

    return Utils.checkFlag(BUSY, i);
  }

}
