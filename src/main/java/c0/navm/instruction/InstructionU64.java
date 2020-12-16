package c0.navm.instruction;

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
}
