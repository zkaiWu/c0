package c0.navm.instruction;

import c0.navm.Assembler;

import java.io.DataOutputStream;
import java.io.IOException;

public class InstructionU32 extends Instruction{

    //4字节参数
    private int param;

    public InstructionU32(InstructionType instructionType, int param) {
        super(instructionType);
        this.param = param;
    }


    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public String debugString() {
        return this.getInstructionType().toString()+"("+this.getParam()+")";
    }

    @Override
    public void toAssemble(DataOutputStream output) throws IOException {
        output.write(Assembler.char2Byte(this.getOpCode()));
        output.write(Assembler.int2Byte(this.getParam()));
    }
}
