package c0.navm.instruction;

import c0.navm.Assembler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class InstructionNone  extends  Instruction{


    public InstructionNone(InstructionType instructionType) {
        super(instructionType);
    }

    public String debugString() {
        return this.getInstructionType().toString();
    }

    @Override
    public void toAssemble(List<Byte> byteList) throws IOException {
        byte by = Assembler.char2Byte(this.getOpCode());
        byteList.add(by);
    }
}
