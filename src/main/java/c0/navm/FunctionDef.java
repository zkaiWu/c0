package c0.navm;


import c0.navm.instruction.Instruction;
import c0.navm.instruction.InstructionU32;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FunctionDef {

    /// 函数名称在全局变量中的位置
    private int position;
    // 返回值占据的 slot 数
    private int returnSlot;
    // 参数占据的 slot 数
    private int paramSlot;
    // 局部变量占据的 slot 数
    private int locSlot;
    // 函数体
    private ArrayList<Instruction> body;

    //记录其在函数表中的偏移，主要用来debug
    private int funcPosition;

    public FunctionDef(int position, int returnSlot, int paramSlot, int locSlot){
        this.position = position;
        this.returnSlot = returnSlot;
        this.paramSlot = paramSlot;
        this.locSlot = locSlot;
        this.body = new ArrayList<>();
    }


    public int addInstruction(Instruction instruction){
        int offset = this.body.size();
        this.body.add(instruction);
        return offset;
    }

    public int addLocVar() {
        return this.locSlot ++ ;
    }


    /**
     * 用来在br回填参数的时候使用
     * @param offset
     * @param num
     */
    public void modInstructionU32(int offset, int num) {
        InstructionU32 instructionU32  = (InstructionU32)this.body.get(offset);
        instructionU32.setParam(num);
        return;
    }

    /**
     * 返回最后一条指令在这个函数里的偏移
     * @return 最后一条指令的偏移
     */
    public int getCurOffset() {
        return this.body.size()-1;
    }


    public int addParamSlot() {
        return this.paramSlot += 1;
    }

    public int addLocSlot() {
        return this.paramSlot += 1;
    }

    public int getReturnSlot() {
        return returnSlot;
    }

    public void setReturnSlot(int returnSlot) {
        this.returnSlot = returnSlot;
    }

    public int getParamSlot() {
        return paramSlot;
    }

    public void setParamSlot(int paramSlot) {
        this.paramSlot = paramSlot;
    }

    public int getLocSlot() {
        return locSlot;
    }

    public void setLocSlot(int locSlot) {
        this.locSlot = locSlot;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ArrayList<Instruction> getBody() {
        return body;
    }

    public void setBody(ArrayList<Instruction> body) {
        this.body = body;
    }

    public int getFuncPosition() {
        return funcPosition;
    }

    public void setFuncPosition(int funcPosition) {
        this.funcPosition = funcPosition;
    }


    public void toAssemble(List<Byte> byteList) throws IOException{
        byte []bytes = Assembler.int2Byte(this.position);
        for(int i=0;i<bytes.length;i++){
            byteList.add(bytes[i]);
        }
        bytes = Assembler.int2Byte(this.returnSlot);
        for(int i=0;i<bytes.length;i++){
            byteList.add(bytes[i]);
        }
        bytes = Assembler.int2Byte(this.paramSlot);
        for(int i=0;i<bytes.length;i++){
            byteList.add(bytes[i]);
        }
        bytes = Assembler.int2Byte(this.locSlot);
        for(int i=0;i<bytes.length;i++){
            byteList.add(bytes[i]);
        }
        bytes = Assembler.int2Byte(this.body.size());
        for(int i=0;i<bytes.length;i++){
            byteList.add(bytes[i]);
        }
        for(Instruction instruction: this.body) {
            instruction.toAssemble(byteList);
        }
    }


}
