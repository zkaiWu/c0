package c0.navm.instruction;

public class Instruction {

    private char opCode;
    private InstructionType instructionType;

    public Instruction(InstructionType instructionType) {
        this.instructionType = instructionType;
        this.opCode = OpCodeMap.getCode(instructionType);
    }

    public char getOpCode() {
        return opCode;
    }

    public void setOpCode(char opCode) {
        this.opCode = opCode;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    public void setInstructionType(InstructionType instructionType) {
        this.instructionType = instructionType;
    }

    public String debugString() {
        return this.getInstructionType().toString();
    }
}
