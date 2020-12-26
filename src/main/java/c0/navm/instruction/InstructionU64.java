package c0.navm.instruction;

import c0.navm.Assembler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class InstructionU64 extends Instruction{


    //8字节指令长度
    private long param;

    public  InstructionU64(InstructionType instructionType, long param) {
        super(instructionType);
        this.param = param;
    }

    public long getParam() {
        return param;
    }

    public void setParam(long param) {
        this.param = param;
    }

    public String debugString() {
        return this.getInstructionType().toString()+"("+this.getParam()+")";
    }

    @Override
    public void toAssemble(List<Byte> byteList) throws IOException {
        byte by = Assembler.char2Byte(this.getOpCode());
        byteList.add(by);
        byte []bytes = Assembler.long2Byte(this.getParam());
        for(int i=0;i<bytes.length;i++ ){
            byteList.add(bytes[i]);
        }
    }
}
