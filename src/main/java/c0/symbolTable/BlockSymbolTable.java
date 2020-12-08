package c0.symbolTable;

import c0.error.AnalyzeError;
import c0.error.ErrorCode;

import java.util.ArrayList;


/**
 * 为一个块的符号表
 */
public class BlockSymbolTable {


    // 这个块的符号表所在层级, 定义从0开始
    private Integer blockLevel;
    //符号表
    private ArrayList<Symbol> symbols;
    //上一层符号表
    private BlockSymbolTable prevBlockSymbolTable;


    public BlockSymbolTable(Integer blockLevel, BlockSymbolTable prevBlockSymbolTable) {
        this.blockLevel = blockLevel;
        this.prevBlockSymbolTable = prevBlockSymbolTable;
        this.symbols = new ArrayList<>();
    }


    /**
     * 根据名字查找, 如果在这一个block没有找到，则交给他上一层的block查找
     * 如果没有则返回null
     * @param name
     * @return
     */
    public Symbol findAllSymbol(String name) {
        int symbolsSize = this.symbols.size();
        for(int i=0; i<symbolsSize; i++) {
            // 如果名字相同，则返回这个symbol
            if(name.contentEquals(this.symbols.get(i).getName())) {
                return this.symbols.get(i);
            }
        }

        //如果已经搜索到了第一层还没有则没有
        if(blockLevel == 0){
            assert this.prevBlockSymbolTable == null;
            return null;
        } else {
            assert  this.prevBlockSymbolTable != null;
            return this.prevBlockSymbolTable.findAllSymbol(name);
        }
    }

    /**
     * 根据名字来查找，仅限于当前块，在当前块声明的变量会覆盖外层的变量
     * 如果没有则返回null
     * @param name
     * @return
     */
    public Symbol findBlockSymbol(String name) {
        int symbolsSize = this.symbols.size();
        for(int i=0; i<symbolsSize; i++) {
            // 如果名字相同，则返回这个symbol
            if(name.contentEquals(this.symbols.get(i).getName())) {
                return this.symbols.get(i);
            }
        }

        return null;
    }


    /**
     * 添加一个Symbol
     * @return
     */
    public boolean insertSymbol(Symbol symbol) throws AnalyzeError{
        String name = symbol.getName();
        if(findBlockSymbol(name)!=null){
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, symbol.getPos());
        }
        this.symbols.add(symbol);
        return true;
    }
}
