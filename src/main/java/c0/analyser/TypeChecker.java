package c0.analyser;

import c0.error.AnalyzeError;
import c0.error.ErrorCode;
import c0.symbolTable.DType;
import c0.symbolTable.FuncSymbol;
import c0.tokenizer.Token;
import c0.util.Pos;

import java.util.List;

public class TypeChecker {

    /**
     * 用来检查表达式左端和右端的类型匹配
     * @param left
     * @param right
     * @return
     * @throws AnalyzeError
     */
    public static DType typeCheck(DType left, DType right, Pos pos) throws AnalyzeError {

        // 表达式的左部和右部不能是void
        if (left == DType.VOID || right == DType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidOpVoid, pos);
        }

        // 表达式右边和左边的类型不一致
        if (left != right ){
            throw new AnalyzeError(ErrorCode.NotMatchedType, pos);
        }

        return left;
    }

    /**
     * 用来检查函数调用时的参数类型是否符合
     * @param funcSymbol
     * @param argsList
     * @return
     */
    public static DType callArgTypeCheck(FuncSymbol funcSymbol, List<DType> argsList, Token funcToken) throws  AnalyzeError{

        Integer argsLen = funcSymbol.getArgsList().size();
        if(argsLen != argsList.size()) {
            throw new AnalyzeError(ErrorCode.callArgNotMatched, funcToken.getStartPos());
        }

        for(int i=0; i<argsLen; i++){
            if(funcSymbol.getArgsList().get(i).getdType() != argsList.get(i)) {
                throw new AnalyzeError(ErrorCode.callArgNotMatched, funcToken.getStartPos());
            }
        }

        return funcSymbol.getdType();
    }
}
