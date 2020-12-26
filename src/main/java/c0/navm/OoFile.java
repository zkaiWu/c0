package c0.navm;


import c0.navm.instruction.Instruction;
import c0.symbolTable.DType;
import c0.symbolTable.FuncSymbol;
import c0.symbolTable.SymbolType;
import c0.symbolTable.VarSymbol;
import c0.util.Pos;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * 用来模拟oO的二进制文件
 */
public class OoFile {


    //魔数
    private int magicNumber = 0x72303b3e;
    //版本号
    private int version = 0x00000001;

    //全局变量表
    private ArrayList<GlobalDef> globals;

    //函数列表
    private ArrayList<FunctionDef> functions;

    //_start函数在函数列表中的位置
    private int _START_POSITION = 0;


    public OoFile() {
        this.globals = new ArrayList<>();
        this.functions = new ArrayList<>();
        //手动生成一个_start函数

        this.addFunction(new FuncSymbol("_start", SymbolType.FUNC, DType.VOID, _START_POSITION, new Pos(0,0)));
    }


    /**
     * 将一个全局函数写入oO中
     * @param funcSymbol
     * @return offset 函数在函数表中的位置
     */
    public int addFunction(FuncSymbol funcSymbol) {
        //获取函数名
        String funcName = funcSymbol.getName();
        //获取函数返回值，如果是void则没有returnSlot，反之有
        DType dType = funcSymbol.getdType();
        int returnSlots = dType==DType.VOID? 0:1;
        //获取参数长度
        int paramSlots = funcSymbol.getArgsList().size();
        //变量长度设置为0
        int locSlots = 0;

        //首先将名字注册到全局变量表中,函数名一定是const的
        GlobalDef globalDef = new GlobalDef((char)1,funcName);
        this.globals.add(globalDef);
        //之后将名字注册到函数表中
        FunctionDef functionDef = new FunctionDef(this.globals.size()-1, returnSlots, paramSlots,locSlots);
        //记录函数在函数列表中的偏移，主要作用是输出debug信息
        this.functions.add(functionDef);
        functionDef.setFuncPosition(this.functions.size()-1);


        //记录funcSymbol在函数列表中的偏移
        funcSymbol.setOffset(this.functions.size()-1);
        return this.functions.size()-1;
    }


    /**
     * 将一个库函数放入声明变量中，并且返回其全局偏移量以供调用
     * @return offset 函数在全局变量中的偏移
     */
    public int addLibFunc(String funcName) {

        //为一个常量
        char isConst = (char) 0x01;
        GlobalDef globalDef = new GlobalDef(isConst, funcName);
        this.globals.add(globalDef);

        //  返回其函数所在的偏移
        return this.globals.size()-1;
    }

    /**
     * 将一个变量全局变量写入oO中，先不赋予初值，初值在_start函数中赋予, 不管是整形还是浮点型，其长度为8
     * @param varSymbol
     * @return offset 返回全局变量的位置
     */
    public int addGlobVar(VarSymbol varSymbol) {

        //获取变量是否是一个常量
        char isConst = varSymbol.getSymbolType() == SymbolType.CONST ? (char)0x01:(char)00;
        //获取变量的数据长度
        int varLen = 8;
        //生成一个GlobalDef
        GlobalDef globalDef = new GlobalDef(isConst, varLen);
        this.globals.add(globalDef);

        varSymbol.setOffset(this.globals.size()-1);
        return this.globals.size()-1;
    }

    public int addGlobStr(String str) {
        char isConst = (char)0x01;
        GlobalDef globalDef = new GlobalDef(isConst, str);
        this.globals.add(globalDef);
        return this.globals.size()-1;
    }


    /**
     * 为函数增加一个局部变量，并返回其偏移
     * @param varSymbol 变量的类型
     * @return offset 变量在此函数栈帧中的偏移
     */
    public int addLocalVar(VarSymbol varSymbol) {
        FunctionDef curFunctionDef = this.curFunctionDef();
        int offset = curFunctionDef.addLocVar();
        varSymbol.setOffset(offset);
        return offset;
    }


    /**
     * 返回现在程序运行到的函数定义，其实也就是函数定义列表尾部的列表
     * @return FunctionDef 一个函数的定义
     */
    public FunctionDef curFunctionDef() {
        return this.functions.get(this.functions.size()-1);
    }


    /**
     * 向当前分析到的函数插入一条指令
     * @param instruction
     * @return InstructionOffset 返回当前指令在这个函数中的偏移
     */
    public int addInstruction(Instruction instruction) {
        FunctionDef curFunctionDef  = this.curFunctionDef();
        int offset = curFunctionDef.addInstruction(instruction);
        return offset;
    }

    public void addStartInstruction(Instruction instruction) {
        FunctionDef startFunctionDef = this.functions.get(this._START_POSITION) ;
        startFunctionDef.addInstruction(instruction);
    }

    public void modInstructionU32(int offset, int num) {
        FunctionDef curFunctionDef  = this.curFunctionDef();
        curFunctionDef.modInstructionU32(offset, num);
        return;
    }

    /**
     * 获取当前最后一条指令在函数体中的偏移
     * @return
     */
    public int getCurOffset() {
        FunctionDef curFunctionDef = this.curFunctionDef();
        return curFunctionDef.getCurOffset();
    }


    /**
     * 将人可以看的debug信息输出到指定的文件上
     * @param output 指定的输出文件
     */
    public void writeDebug(PrintStream output) {

        //打印全局变量
        for(int i=0 ;i<this.globals.size(); i++ ) {
            output.print("static : ");
            GlobalDef globalDefTemp = this.globals.get(i);
            StringBuffer valueTemp  = new StringBuffer();
            for(int j=0; j<globalDefTemp.getValue().size(); j++) {
                output.printf("%x ",(int)globalDefTemp.getValue().get(j));
                valueTemp.append(globalDefTemp.getValue().get(j));
            }
            output.print("('"+valueTemp.toString()+"')");
            output.println();
        }

        //打印函数以及指令
        for(int i=0; i<this.functions.size(); i++) {
            FunctionDef functionDefTemp = this.functions.get(i);
            output.println("fn ["+functionDefTemp.getPosition()+"] "+
                    functionDefTemp.getFuncPosition()+" "+functionDefTemp.getParamSlot()+" -> "+functionDefTemp.getReturnSlot()+" {");
            for(int j=0; j<functionDefTemp.getBody().size(); j++) {
                output.println("    "+j+": "+functionDefTemp.getBody().get(j).debugString());
            }
            output.println("}");
        }
        return;
    }


    public void toAssemble(DataOutputStream output) throws IOException {

        //写入模数
        byte[] magicNum = {0x72,0x30,0x3b,0x3e};
        output.write(magicNum);
        //写入版本号
        byte[] version = {0x00, 0x00, 0x00, 0x01};
        output.write(version);
        //写入全局变量的个数
        byte[] globalCount = Assembler.int2Byte(this.globals.size());
        output.write(globalCount);
        //写入每一个全局变量
        for(GlobalDef globalDef: this.globals) {
            globalDef.toAssemble(output);
        }
        for(FunctionDef functionDef: this.functions) {
            functionDef.toAssemble(output);
        }
    }
}
