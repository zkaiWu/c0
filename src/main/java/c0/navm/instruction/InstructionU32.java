package c0.navm.instruction;

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
}
