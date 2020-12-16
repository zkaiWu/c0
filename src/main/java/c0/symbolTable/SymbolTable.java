package c0.symbolTable;


import c0.error.AnalyzeError;

import java.util.ArrayList;

/**
 * 多个block结合起来的SymbolTable，是全局的SymbolTable
 * 来管理level等重要的信息
 */
public class SymbolTable {

    //各个block块
    private ArrayList<BlockSymbolTable> symbolTable;
    //记录当前的level, 最外层的level是0
    private Integer level;

    public SymbolTable() {
        this.symbolTable = new ArrayList<>();
        level = -1;
    }


    /**
     * 初始化symbolTable, 程序开始的时候应该有一个level为0的block
     */
    public void initSymbolTable() {
        this.level++;
        assert this.level == 0;
        BlockSymbolTable blockSymbolTable = new BlockSymbolTable(this.level, null);
        this.symbolTable.add(blockSymbolTable);
    }


    /**
     * 添加新的Block块, 其上一层为当前的块，也就是栈顶
     */
    public void addBlockSymbolTable() {
        this.level++;
        //获取最顶层的块
        BlockSymbolTable prevBlock = this.symbolTable.get(this.symbolTable.size()-1);
        assert  prevBlock!=null;
        //生成新的符号表块
        BlockSymbolTable newBlock = new BlockSymbolTable(level, prevBlock);
        this.symbolTable.add(newBlock);
    }


    /**
     * 移除当前分析完毕的符号表块
     */
    public void rmBlockSymbolTable() {
        this.level--;
        this.symbolTable.remove(this.symbolTable.size()-1);
    }

    /**
     * 查找其能够访问到的所有symbol
     * @param name 符号名字
     * @return Symbol 查找到的Symbol
     * @throws AnalyzeError
     */
    public Symbol findAllSymbol(String name) throws AnalyzeError {
        BlockSymbolTable curBlock = this.symbolTable.get(this.symbolTable.size()-1);
        return curBlock.findAllSymbol(name);
    }

    /**
     * 在当前块查找symbol，主要用于变量的声明
     * @param name 符号名字
     * @return 查找到的Symbol
     * @throws AnalyzeError
     */
    public Symbol findBlockSymbol(String name) throws AnalyzeError {
        BlockSymbolTable curBlock = this.symbolTable.get(this.symbolTable.size()-1);
        return curBlock.findBlockSymbol(name);
    }

    /**
     * 添加一个symbol，声明一个变量，如果是一个全局变量，则改变其global位
     * @param symbol
     * @return
     * @throws AnalyzeError
     */
    public boolean insertSymbol(Symbol symbol) throws AnalyzeError {
        BlockSymbolTable curBlock = this.symbolTable.get(this.symbolTable.size()-1);
        return curBlock.insertSymbol(symbol);
    }

    /**
     * 获取当前在第几层，第0层为全局变量
     * @return
     */
    public int getCurLevel() {
        return this.level;
    }

}
