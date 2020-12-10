package c0.symbolTable;

import c0.util.Pos;

public class Symbol {

    //变量名称
    private String name;
    //符号类型
    private SymbolType symbolType;
    //数据类型
    private DType dType;
    //在符号表中的偏移
    private Integer offset;
    //在源代码中的位置
    private Pos pos;

    public Symbol(String name, SymbolType symbolType, DType dType, Integer offset, Pos pos) {
        this.name = name;
        this.symbolType = symbolType;
        this.dType = dType;
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

    public DType getdType() {
        return dType;
    }

    public void setdType(DType dType) {
        this.dType = dType;
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

    @Override
    public String toString() {
        return "Symbol{" +
                "name='" + name + '\'' +
                ", symbolType=" + symbolType +
                ", dataType=" + dType +
                ", offset=" + offset +
                ", pos=" + pos +
                '}';
    }
}
