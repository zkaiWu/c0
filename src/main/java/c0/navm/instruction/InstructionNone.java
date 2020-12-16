package c0.navm.instruction;

public class InstructionNone  extends  Instruction{


    public InstructionNone(InstructionType instructionType) {
        super(instructionType);
    }

    public String debugString() {
        return this.getInstructionType().toString();
    }

}
