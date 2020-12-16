package c0.symbolTable;

import c0.util.Pos;

public class Symbol {

    //变量名称
    private String name;
    //符号类型
    private SymbolType symbolType;
    //数据类型
    //1.如果是变量或常量则记录其类型
    //2。如果是函数则记录他们的返回值类型
    private DType dType;
    //有多种情况
    //1：如果是局部变量和函数形参，则为他们的相对地址以供
    //2：如果是全局变量，则为他们在全局变量表中的位移
    //3：如果是普通函数，则为他们在函数列表中的位移
    //4：如果是库函数，则为他们在全局变量表中的位移，用来callName
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
