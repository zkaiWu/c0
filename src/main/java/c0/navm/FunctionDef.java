package c0.navm;


import c0.navm.instruction.Instruction;

import java.util.ArrayList;

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


    public void addInstruction(Instruction instruction){
        this.body.add(instruction);
    }

    public int addLocVar() {
        return this.locSlot ++ ;
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


}
