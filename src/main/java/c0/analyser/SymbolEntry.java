package c0.analyser;

import java.util.ArrayList;

public class SymbolEntry {

    private String name;
    private boolean isConstant;
    private boolean isInitialized;
    private SymbolType symbolType;
    private ArrayList<SymbolType> paramList;

    public SymbolEntry(String name, SymbolType symbolType){
        this.name = name;
        this.symbolType = symbolType;
        this.isConstant = false;
        this.isInitialized = false;
        paramList = new ArrayList<>();
    }

    public boolean isConstant() {
        return this.isConstant;
    }

    public boolean setConstant(boolean constant) {
        this.isConstant = constant;
        return this.isConstant;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public boolean setInitialized(boolean initialized) {
        this.isInitialized = initialized;
        return this.isInitialized;
    }
}
