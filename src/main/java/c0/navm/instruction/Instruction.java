package c0.navm.instruction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

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

    public void toAssemble(List<Byte> byteList) throws IOException {
        System.err.println("Instruction to Assemble function can not be used");
        return;
    }
}
