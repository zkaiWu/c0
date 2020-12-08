package c0.symbolTable;

import c0.util.Pos;

public class Symbol {

    //变量名称
    private String name;
    //符号类型
    private SymbolType symbolType;
    //数据类型
    private DataType dataType;
    //在符号表中的偏移
    private Integer offset;
    //在源代码中的位置
    private Pos pos;

    public Symbol(String name, SymbolType symbolType, DataType dataType, Integer offset, Pos pos) {
        this.name = name;
        this.symbolType = symbolType;
        this.dataType = dataType;
        this.offset = offset;
        this.pos = pos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Pos getPos() {
        return pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }
}
