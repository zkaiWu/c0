package c0.navm.instruction;

import c0.navm.Assembler;

import java.io.DataOutputStream;
import java.io.IOException;

public class InstructionNone  extends  Instruction{


    public InstructionNone(InstructionType instructionType) {
        super(instructionType);
    }

    public String debugString() {
        return this.getInstructionType().toString();
    }

    @Override
    public void toAssemble(DataOutputStream output) throws IOException {
        output.write(Assembler.char2Byte(this.getOpCode()));
    }
}
