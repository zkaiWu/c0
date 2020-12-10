package c0.symbolTable;

import c0.error.AnalyzeError;
import c0.error.ErrorCode;
import c0.util.Pos;

import java.util.ArrayList;

public class FuncSymbol extends Symbol{


    //函数的形参表
    private ArrayList<VarSymbol> argsList;

    public FuncSymbol(String name, SymbolType symbolType, DType dType, Integer offset, Pos pos) {
        super(name, symbolType, dType, offset, pos);
        this.argsList = new ArrayList<>();
    }


    /**
     * 为函数的声明增加参数
     * @param varSymbol
     * @return
     * @throws AnalyzeError
     */
    public void addArgs(VarSymbol varSymbol) throws AnalyzeError {
        int listSize = this.argsList.size();
        for(int i=0; i<listSize; i++){
            if(varSymbol.getName().contentEquals(argsList.get(i).getName())) {
                throw new AnalyzeError(ErrorCode.DuplicateArgs, varSymbol.getPos());
            }
        }
        argsList.add(varSymbol);
    }

    public ArrayList<VarSymbol> getArgsList() {
        return argsList;
    }

    public void setArgsList(ArrayList<VarSymbol> argsList) {
        this.argsList = argsList;
    }
}
