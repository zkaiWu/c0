package c0.symbolTable;

import c0.util.Pos;

public class VarSymbol extends  Symbol{


    //是否已经初始化
    private boolean initialized;
    //是否是全局变量
    private boolean isGlobal;
    //是否是函数参数
    private boolean isParam;

    public VarSymbol(String name, SymbolType symbolType, DType dType, Integer offset, Pos pos) {
        super(name, symbolType, dType, offset, pos);
        this.initialized = false;
        this.isGlobal = false;
        this.isParam = false;
    }

    public boolean setInitialized(boolean initialized) {
        this.initialized = initialized;
        return this.initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public VarSymbol setGlobal(boolean global) {
        isGlobal = global;
        return  this;
    }

    public boolean isParam() {
        return isParam;
    }

    public VarSymbol setParam(boolean param) {
        isParam = param;
        return  this;
    }
}
