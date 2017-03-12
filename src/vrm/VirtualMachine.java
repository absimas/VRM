package vrm;

/**
 * Created by Simas on 2017 Mar 04.
 */
public class VirtualMachine extends Machine {

  private final RealMachine realMachine;

  public VirtualMachine(RealMachine realMachine, Memory memory) {
    super(memory);
    this.realMachine = realMachine;
  }

}