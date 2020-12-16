package c0.navm;

import c0.error.AnalyzeError;
import c0.symbolTable.DType;
import c0.symbolTable.FuncSymbol;
import c0.symbolTable.SymbolType;
import c0.symbolTable.VarSymbol;
import c0.util.Pos;

import java.util.HashMap;
import java.util.Map;

public class Lib {

    private static Map<String, String> lib;

    static {
        lib = new HashMap<>();
        lib.put("getint","getint");
        lib.put("getdouble","getdouble");
        lib.put("getchar","getchar");
        lib.put("putint","putint");
        lib.put("putdouble","putdouble");
        lib.put("putchar","putchar");
        lib.put("putstr","putstr");
        lib.put("putln","putln");
    }

    /**
     * 生成lib中的函数符号,并且在oO文件中注册这个函数
     * @param funcName  函数名字
     * @param pos       在源文件中的位置
     * @return FuncSymbol 函数符号，如果不在lib中则返回空
     */
    public static FuncSymbol genLibFunc(String funcName, Pos pos, OoFile oO) throws AnalyzeError {


        //如果lib中没有这个函数
        if(lib.get(funcName)==null) {
            return null;
        }

        System.out.println(funcName);
        int offset = oO.addLibFunc(funcName);

        //如果这个函数是getint
        if(funcName.contentEquals("getint")) {
            FuncSymbol getInt = new FuncSymbol(funcName, SymbolType.FUNC, DType.INT, offset, pos);
            return  getInt;
        }
        //如果这个函数是getdouble
        if(funcName.contentEquals("getdouble")) {
            FuncSymbol getDouble = new FuncSymbol(funcName, SymbolType.FUNC, DType.DOUBLE, offset, pos);
        }
        //如果这个函数是getchar
        if(funcName.contentEquals("getchar")) {
            FuncSymbol getChar = new FuncSymbol(funcName, SymbolType.FUNC, DType.INT, offset, pos);
        }
        //如果这个函数是putint
        if(funcName.contentEquals("putint")) {

            FuncSymbol putInt = new FuncSymbol(funcName, SymbolType.FUNC, DType.VOID, offset, pos);
            putInt.addArgs(new VarSymbol("param",SymbolType.VARIABLE, DType.INT, offset, pos));
            return  putInt;
        }
        //putdouble
        if(funcName.contentEquals("putdouble")) {
            FuncSymbol putInt = new FuncSymbol(funcName, SymbolType.FUNC, DType.VOID, offset, pos);
            putInt.addArgs(new VarSymbol("param",SymbolType.VARIABLE, DType.DOUBLE, offset, pos));
            return  putInt;
        }
        //putchar
        if(funcName.contentEquals("putchar")) {
            FuncSymbol putChar = new FuncSymbol(funcName, SymbolType.FUNC, DType.VOID, offset, pos);
            putChar.addArgs(new VarSymbol("param",SymbolType.VARIABLE, DType.INT, offset, pos));
            return  putChar;
        }
        //putstr
        if(funcName.contentEquals("putstr")) {
            FuncSymbol putStr = new FuncSymbol(funcName, SymbolType.FUNC, DType.VOID, offset, pos);
            putStr.addArgs(new VarSymbol("param",SymbolType.VARIABLE, DType.INT, offset, pos));
            return  putStr;
        }
        //putln
        if(funcName.contentEquals("putln")) {
            FuncSymbol putLn = new FuncSymbol(funcName, SymbolType.FUNC, DType.VOID, offset, pos);
            return  putLn;
        }
        //都不匹配
        else {
            return null;
        }
    }
}
