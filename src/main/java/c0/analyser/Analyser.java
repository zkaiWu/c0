package c0.analyser;

import c0.error.AnalyzeError;
import c0.error.CompileError;
import c0.error.ErrorCode;
import c0.symbolTable.*;
import c0.tokenizer.Token;
import c0.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Analyser {

    //封装了词法分析器，用来解析token
    SymbolIter it;
    //符号表
    SymbolTable symbolTable;
    //用于Type类型之间的映射，不然手写判断可太蠢了
    Map<TokenType, DType> typeMap;


    public Analyser(SymbolIter symbolIter) {
        this.it = symbolIter;
        symbolTable = new SymbolTable();
        typeMap = new HashMap<>();
    }


    public void analyse() throws CompileError {
        initTypeMap();
        symbolTable.initSymbolTable();
        analyseProgram();
    }

    public void initTypeMap() {
        this.typeMap.put(TokenType.INT, DType.INT);
        this.typeMap.put(TokenType.VOID, DType.VOID);
        this.typeMap.put(TokenType.DOUBLE, DType.DOUBLE);
        this.typeMap.put(TokenType.STRING, DType.STRING);
        this.typeMap.put(TokenType.CHAR, DType.CHAR);
    }

    /**
     * 分析程序：program -> decl_stmt* function*
     * 这是可以混搭的版本
     *
     * @throws CompileError
     */
    public void analyseProgram() throws CompileError {

        while(it.check(TokenType.LET_KW) || it.check(TokenType.CONST_KW) ||
                it.check(TokenType.FN_KW)) {

            if( it.check(TokenType.LET_KW) ) {
                analyseVariableDecl();
            }
            else if( it.check(TokenType.CONST_KW) ) {
                analyseConstDecl();
            }
            else if( it.check(TokenType.FN_KW) ) {
                analyseFuncDecl();
            }
        }

        /** 如果分析完毕后没有到eof，则编译出错*/
        Token eof = it.next();
        if(eof.getTokenType()!=TokenType.EOF) {
            throw new AnalyzeError(ErrorCode.EOF, eof.getStartPos());
        }

    }

    /**
     * 解析变量声明：let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'   注意这儿没有等于号
     *
     * @throws CompileError
     */
    public void analyseVariableDecl() throws  CompileError {

        boolean isInit = false;
        it.expectToken(TokenType.LET_KW);
        Token variable = it.expectToken(TokenType.IDENT);
        it.expectToken(TokenType.COLON);

        //分析ty， 在声明中，ty不能为void
        Token ty = it.next();
        if(ty.getTokenType()!=TokenType.INT && ty.getTokenType()!=TokenType.DOUBLE && ty.getTokenType()!=TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
        }
        if(ty.getTokenType() == TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidVoid, ty.getStartPos());
        }

        if(it.nextIf(TokenType.ASSIGN)!=null){
            isInit = true;               //变量被初始化
            analyseExpr();
        }

        it.expectToken(TokenType.SEMICOLON);

        // 在符号表中注册一个新的变量符号
        DType dType = this.typeMap.get(ty.getTokenType());
        VarSymbol varSymbol = new VarSymbol(variable.getValueString(), SymbolType.VARIABLE, dType, 0, variable.getStartPos());
        varSymbol.setInitialized(isInit);
        this.symbolTable.insertSymbol(varSymbol);

        //解析变量声明分析完毕
        return;
    }

    /**
     * 解析常量声明：const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     * @throws CompileError
     */
    public void analyseConstDecl() throws  CompileError {
        it.expectToken(TokenType.CONST_KW);
        Token variable = it.expectToken(TokenType.IDENT);
        it.expectToken(TokenType.COLON);
        Token ty = it.next();
        if(ty.getTokenType()!=TokenType.INT && ty.getTokenType()!=TokenType.DOUBLE && ty.getTokenType()!=TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
        }
        if(ty.getTokenType() == TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidVoid, ty.getStartPos());
        }

        if(it.check(TokenType.ASSIGN)==false) {
            throw new AnalyzeError(ErrorCode.ConstantNeedValue, it.peekToken().getStartPos());
        }
        it.expectToken(TokenType.ASSIGN);
        analyseExpr();
        it.expectToken(TokenType.SEMICOLON);

        //获取对应的符号数据类型
        DType dType = this.typeMap.get(ty.getTokenType());
        VarSymbol varSymbol = new VarSymbol(variable.getValueString(), SymbolType.CONST, dType, 0, variable.getStartPos());
        varSymbol.setInitialized(true);
        this.symbolTable.insertSymbol(varSymbol);

    }


    /**
     * 分析函数声明：'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     * @throws CompileError
     */
    public void analyseFuncDecl() throws  CompileError {


        it.expectToken(TokenType.FN_KW);
        Token fnIdent = it.expectToken(TokenType.IDENT);

        //生成新的函数符号
        FuncSymbol funcSymbol = new FuncSymbol(fnIdent.getValueString(), SymbolType.FUNC, DType.INT, 0, fnIdent.getStartPos());


        it.expectToken(TokenType.L_PAREN);


        //paramlist 是可选的
        if(it.peekToken().getTokenType() == TokenType.IDENT || it.peekToken().getTokenType() == TokenType.CONST_KW) {
            analyseFuncParamList(funcSymbol);
        }
//        System.out.println("fn "+ fnIdent.getValueString() + " param is : " + funcSymbol.getArgsList());

        it.expectToken(TokenType.R_PAREN);
        it.expectToken(TokenType.ARROW);
        Token ty = it.next();
        if(ty.getTokenType()!=TokenType.INT && ty.getTokenType()!=TokenType.DOUBLE && ty.getTokenType()!=TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
        }

        DType funcReturnType = this.typeMap.get(ty.getTokenType());
        funcSymbol.setdType(funcReturnType);

        this.symbolTable.insertSymbol(funcSymbol);
        analyseBlockStmt(funcSymbol);
    }

    /**
     * 分析函数参数列表：function_param_list -> function_param (',' function_param)*
     * @throws CompileError
     */
    public void analyseFuncParamList(FuncSymbol funcSymbol) throws CompileError {
        VarSymbol varSymbol = analyseFuncParam();
        funcSymbol.addArgs(varSymbol);
        while(it.nextIf(TokenType.COMMA)!=null) {
            VarSymbol varSymbolMany = analyseFuncParam();
            funcSymbol.addArgs(varSymbolMany);
        }
    }

    /**
     * 分析函数参数：function_param -> 'const'? IDENT ':' ty
     * @return Symbol 返回解析的函数参数符号
     * @throws CompileError
     */
    public VarSymbol analyseFuncParam() throws  CompileError {

        boolean isConst = false;
        if(it.check(TokenType.CONST_KW)) {
            it.expectToken(TokenType.CONST_KW);
            isConst = true;
        }

        Token fnParam = it.expectToken(TokenType.IDENT);
        it.expectToken(TokenType.COLON);

        //解析类型
        Token ty = it.next();
        if(ty.getTokenType()!=TokenType.INT && ty.getTokenType()!=TokenType.DOUBLE && ty.getTokenType()!=TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
        }
        if(ty.getTokenType() == TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidVoid, ty.getStartPos());
        }

        VarSymbol varSymbol= new VarSymbol(fnParam.getValueString(),
                                            isConst ? SymbolType.CONST:SymbolType.VARIABLE,
                                            this.typeMap.get(ty.getTokenType()),
                                            0,
                                            fnParam.getStartPos());


        return varSymbol;
    }



    /**
     * 解析各种语句，主要是分发的功能
     * stmt ->
     *       expr_stmt
     *     | decl_stmt
     *     | if_stmt
     *     | while_stmt
     *     | return_stmt
     *     | block_stmt
     *     | empty_stmt
     * first集为：
     * first(expr_stmt) = {'-',IDNET, UINT_VALUE, DOUBLE_VALUE, STRING_VALUE, '('}
     * first(decal_stmt) = {LET_KW,CONST_KW}
     * first(if_stmt) = {IF_KW}
     * first(while_stmt) = {WHILE_KW}
     * first(return_stmt) = {RETURN_KW}
     * first(block_stmt) = {L_BRACE}
     * first(empty_stmt) = {SEMICOLON}
     * @throws CompileError
     */
    public void analyseStmt() throws CompileError {

        Token token = it.peekToken();
        if(token.getTokenType() == TokenType.SEMICOLON) {
            // 空语句
            analyseEmptyStmt();
        }
        else if(token.getTokenType() == TokenType.L_BRACE) {
            analyseBlockStmt(null);
        }
        else if(token.getTokenType() == TokenType.RETURN_KW) {
            analyseReturnStmt();
        }
        else if(token.getTokenType() == TokenType.WHILE_KW) {
            analyseWhileStmt();
        }
        else if(token.getTokenType() == TokenType.IF_KW) {
            analyseIfStmt();
        }
        else if(token.getTokenType() == TokenType.LET_KW) {
            analyseVariableDecl();
        }
        else if(token.getTokenType() == TokenType.CONST_KW) {
            analyseConstDecl();
        }
        else if(token.getTokenType() == TokenType.MINUS || token.getTokenType() == TokenType.IDENT ||
                token.getTokenType() == TokenType.UINT_VALUE || token.getTokenType() == TokenType.DOUBLE_VALUE ||
                token.getTokenType() == TokenType.STRING_VALUE || token.getTokenType() == TokenType.L_PAREN) {
            analyseExprStmt();
        }
        else {
            throw new AnalyzeError(ErrorCode.UnExpectToken, it.peekToken().getStartPos());
        }

    }

    /**
     * 分析语句块：block_stmt -> '{' stmt* '}'
     * @param funcSymbol 函数的符号，用来获取函数的参数
     * @throws CompileError
     */
    public void analyseBlockStmt(FuncSymbol funcSymbol) throws  CompileError {
        it.expectToken(TokenType.L_BRACE);

        //生成新的符号表块
        this.symbolTable.addBlockSymbolTable();

        //将函数的参数表插入符号表中
        if(funcSymbol!=null) {
            int agrsSize = funcSymbol.getArgsList().size();
            ArrayList<VarSymbol> funcParams = funcSymbol.getArgsList();
            for (int i = 0; i < agrsSize; i++) {
                this.symbolTable.insertSymbol(funcParams.get(i));
            }
        }

        while(it.check(TokenType.R_BRACE) == false) {
            analyseStmt();
        }
        it.expectToken(TokenType.R_BRACE);

        //删除这个符号表块
        this.symbolTable.rmBlockSymbolTable();
    }


    /**
     * 空语句：empty_stmt -> ';'
     * @throws CompileError
     */
    public void analyseEmptyStmt() throws CompileError {
        it.expectToken(TokenType.SEMICOLON);
        return ;
    }

    /**
     * 分析while语句：while_stmt -> 'while' expr block_stmt
     * @throws CompileError
     */
    public void analyseWhileStmt() throws  CompileError {
        it.expectToken(TokenType.WHILE_KW);
        analyseExpr();
        analyseBlockStmt(null);
    }


    /**
     * 分析If语句: if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
     * @throws CompileError
     */
    public void analyseIfStmt() throws  CompileError {
        it.expectToken(TokenType.IF_KW);
        analyseExpr();
        analyseBlockStmt(null);
        if(it.check(TokenType.ELSE_KW)) {
            it.expectToken(TokenType.ELSE_KW);
            //接下来对应两个分支，分别是block_stmt以及if_stmt;
            if (it.check(TokenType.L_BRACE)) {
                //这是block_stmt分支
                analyseBlockStmt(null);
            }
            else if(it.peekToken().getTokenType() == TokenType.IF_KW) {
                analyseIfStmt();
            }
            else {
                throw new AnalyzeError(ErrorCode.UnExpectToken, it.peekToken().getStartPos());
            }
        }
        return ;
    }

    /**
     * 分析return语句: return_stmt -> 'return' expr? ';'
     * 写法可以优化, 可以直接判断下一个是不是';'，如果不是直接丢给expr，用expr来抛出异常
     * @throws CompileError
     */
    public void analyseReturnStmt() throws CompileError {
        it.expectToken(TokenType.RETURN_KW);
        Token token = it.peekToken();
        if(token.getTokenType()==TokenType.MINUS || token.getTokenType()==TokenType.IDENT ||
            token.getTokenType() == TokenType.UINT_VALUE || token.getTokenType() == TokenType.STRING_VALUE ||
            token.getTokenType() == TokenType.DOUBLE_VALUE) {

            analyseExpr();
        }

        it.expectToken(TokenType.SEMICOLON);
    }

    /**
     * 分析: empty_stmt -> ';'
     * @throws CompileError
     */
    public void analyseExprStmt() throws CompileError {
        analyseExpr();
        it.expectToken(TokenType.SEMICOLON);
    }


    /**
     * 分析expr,将文法改写如下
     * Expr -> Cond ( (== | != | < | > | <= | >=) Cond )?
     * Cond -> Term {(+ | -) Term}
     * Term -> Factor { (* | /) Factor}
     * Factor -> Atom { as ( INT | DOUBLE )}?
     * Atom -> '-'? Item
     * Item -> '(' Expr ')' |IDENT | UINT_VALUE | DOUBLE_VALUE | func_call | IDENT '=' E
     * 每一个表达式都会返回其类型用来做判断
     * @return 返回一个SymbolType用来做类型判断
     * @throws CompileError
     */
    public DType analyseExpr() throws CompileError {



        DType leftType = analyseCond();
        Token token = it.peekToken();

        //可选的比较
        if(token.getTokenType() == TokenType.EQ || token.getTokenType() == TokenType.NEQ ||
            token.getTokenType() == TokenType.LT || token.getTokenType() == TokenType.GT ||
            token.getTokenType() == TokenType.LE || token.getTokenType() == TokenType.GE) {

            Token compare = it.next();
            DType rightType = analyseCond();
            TypeChecker.typeCheck(leftType, rightType, compare.getStartPos());
        }

        return leftType;
    }

    /**
     * 分析Cond表达式：Cond -> Term {(+ | -) Term}
     * @throws CompileError
     */
    public DType analyseCond() throws  CompileError {

        DType leftType = analyseTerm();
        while(it.check(TokenType.PLUS)||it.check(TokenType.MINUS)) {
            Token token = it.next();
            DType rightType = analyseTerm();
            TypeChecker.typeCheck(leftType, rightType, token.getStartPos());
            leftType = rightType;
        }

        return leftType;
    }


    /**
     * 分析Term表达式 ：Term -> Factor { (* | /) Factor}
     * @throws CompileError
     */
    public DType analyseTerm() throws CompileError {

        DType leftType = analyseFactor();
        while(it.check(TokenType.MUL)||it.check(TokenType.DIV)) {
            Token token = it.next();
            DType rightType = analyseFactor();
            TypeChecker.typeCheck(leftType, rightType, token.getStartPos());
        }

        return leftType;
    }


    /**
     * 分析Factor表达式： Factor -> Atom { as ( INT | DOUBLE )}
     * @throws CompileError
     */
    public DType analyseFactor() throws CompileError {

        DType dType = analyseAtom();

        if(dType == DType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidOpVoid, it.peekToken().getStartPos());
        }

        while(it.check(TokenType.AS_KW)) {
            it.expectToken(TokenType.AS_KW);      //吃掉as符号
            Token ty = it.next();
            if(ty.getTokenType()!=TokenType.INT&&ty.getTokenType()!=TokenType.DOUBLE) {         //as 只能接INT和DOUBLE
                throw new AnalyzeError(ErrorCode.UnExpectToken, ty.getStartPos());
            }
            dType = this.typeMap.get(ty.getTokenType());
        }

        return dType;
    }


    /**
     * 分析Atom语句：Atom -> '-'? Item
     * @throws CompileError
     */
    public DType analyseAtom() throws CompileError {

        //如果存在'-'号
        if(it.check(TokenType.MINUS)){
            it.next();          //吃掉负号
        }

        return analyseItem();
    }

    /**
     * 分析Item语句：'(' Expr ')' |IDENT | UINT_VALUE | DOUBLE_VALUE | func_call | IDENT '=' Expr
     * 其中func_call 的first集也是IDent
     * @throws CompileError
     */
    public DType analyseItem() throws  CompileError {

        DType dType = DType.INT;
        //对应 '(' Expr ')'
        if(it.check(TokenType.L_PAREN)) {
            it.expectToken(TokenType.L_PAREN);
            dType = analyseExpr();
            it.expectToken(TokenType.R_PAREN);
        }
        //对应 UINT_VALUE, 整型字面量
        else if(it.check(TokenType.UINT_VALUE)) {
            it.expectToken(TokenType.UINT_VALUE);
            dType = DType.INT;
        }
        //对应DOUBLE_VALUE，浮点型字面量
        else if(it.check(TokenType.DOUBLE_VALUE)) {
            it.expectToken(TokenType.DOUBLE_VALUE);
            dType = DType.DOUBLE;
        }
        //对应剩下的三个，其前缀相同
        else if(it.check(TokenType.IDENT)) {
            dType = analyseCallOrAssignOrIdent();
        }
        else {
            throw new AnalyzeError(ErrorCode.UnExpectToken, it.peekToken().getStartPos());
        }

        return dType;
    }


    /**
     * 分析赋值语句，Ident表达式以及函数调用表达式
     * call_expr -> IDENT '(' call_param_list? ')'  类型为函数的返回类型
     * ident_expr -> IDENT 类型为IDENT的类型
     * assign_expr -> l_expr '=' expr； l_expr -> IDENT   类型为void
     * 因为其
     * @throws CompileError
     */
    public DType analyseCallOrAssignOrIdent() throws CompileError {

        Token identToken = it.expectToken(TokenType.IDENT);

        Symbol symbol = this.symbolTable.findAllSymbol(identToken.getValueString());
        if(symbol == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, identToken.getStartPos());
        }
        DType leftType = symbol.getdType();

        //  赋值语句
        if(it.check(TokenType.ASSIGN)) {
            Token assign = it.expectToken(TokenType.ASSIGN);
            //如果被赋值的是一个常量，则抛出异常
            if(symbol.getSymbolType() == SymbolType.CONST) {
                throw new AnalyzeError(ErrorCode.AssignToConstant, assign.getStartPos());
            }
            DType rightType = analyseExpr();
            TypeChecker.typeCheck(leftType, rightType, assign.getStartPos());

            //赋值语句的类型是void
            return DType.VOID;
        }


        // 函数调用语句
        else if(it.check(TokenType.L_PAREN)) {

            //如果这个函数不是一个标识符号
            if(symbol.getSymbolType()!=SymbolType.FUNC) {
                throw new AnalyzeError(ErrorCode.NotDeclared, identToken.getStartPos());
            }
            it.expectToken(TokenType.L_PAREN);
            if(it.check(TokenType.L_PAREN) || it.check(TokenType.MINUS) ||
                it.check(TokenType.UINT_VALUE) || it.check(TokenType.STRING_VALUE) ||
                it.check(TokenType.DOUBLE_VALUE) || it.check(TokenType.IDENT)) {

                analyseCallParam((FuncSymbol) symbol, identToken);
            }
            it.expectToken(TokenType.R_PAREN);
        }

        return leftType;
    }


    /**
     * 分析函数的参数：call_param_list -> expr (',' expr)*
     * 然后检查函数调用的参数是否相同。
     * @param  funcSymbol 函数的符号，用来获取参数信息
     * @param funcToken 函数的Token， 用来获取位置报错
     * @throws CompileError
     */
    public void analyseCallParam(FuncSymbol funcSymbol, Token funcToken) throws CompileError {
        //实际参数的类型表
        ArrayList<DType> actualArgList = new ArrayList<>();
        DType dType = analyseExpr();
        actualArgList.add(dType);
        while(it.check(TokenType.COMMA)) {
            it.expectToken(TokenType.COMMA);
            dType = analyseExpr();
            actualArgList.add(dType);
        }
        TypeChecker.callArgTypeCheck(funcSymbol, actualArgList, funcToken);

        return;
    }
}
