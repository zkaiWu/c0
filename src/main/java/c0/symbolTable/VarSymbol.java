package c0.symbolTable;

import c0.util.Pos;

public class VarSymbol extends  Symbol{

    private boolean initialized;

    public VarSymbol(String name, SymbolType symbolType, DType dType, Integer offset, Pos pos) {
        super(name, symbolType, dType, offset, pos);
    }

    public boolean setInitialized(boolean initialized) {
        this.initialized = initialized;
        return this.initialized;
    }

    public boolean getInitialized() {
        return this.initialized;
    }


}
