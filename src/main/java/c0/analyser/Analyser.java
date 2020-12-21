package c0.analyser;

import c0.analyser.returnChecker.BranchStack;
import c0.error.AnalyzeError;
import c0.error.CompileError;
import c0.error.ErrorCode;
import c0.navm.Lib;
import c0.navm.OoFile;
import c0.navm.instruction.*;
import c0.symbolTable.*;
import c0.tokenizer.Token;
import c0.tokenizer.TokenType;
import c0.util.Pos;

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
    //用于返回分支及其类型的检查
    BranchStack branchStack;
    //oO文件，在分析时填充
    OoFile oO;


    public Analyser(SymbolIter symbolIter) {
        this.it = symbolIter;
        symbolTable = new SymbolTable();
        typeMap = new HashMap<>();
        oO = new OoFile();
        branchStack = new BranchStack();
    }


    public OoFile analyse() throws CompileError {
        initTypeMap();
        symbolTable.initSymbolTable();
        analyseProgram();
        analyseTail();
        return this.oO;
    }

    public void initTypeMap() {
        this.typeMap.put(TokenType.INT, DType.INT);
        this.typeMap.put(TokenType.VOID, DType.VOID);
        this.typeMap.put(TokenType.DOUBLE, DType.DOUBLE);
        this.typeMap.put(TokenType.STRING, DType.STRING);
        this.typeMap.put(TokenType.CHAR, DType.CHAR);
    }


    /**
     * 分析完毕后的函数，做一些收尾的检查和生成语句的工作
     * @return boolean 是否生成成功
     * @throws AnalyzeError
     */
    public boolean analyseTail() throws AnalyzeError {
        assert this.symbolTable.getCurLevel() == 0;
        Symbol symbol = symbolTable.findAllSymbol("main");
        if(symbol==null || symbol.getSymbolType()!=SymbolType.FUNC) {
            throw new AnalyzeError(ErrorCode.NoMain, new Pos(0,0));
        }
        assert symbol instanceof FuncSymbol;
        FuncSymbol funcSymbol = (FuncSymbol) symbol;

        //为_start函数添加调用main的指令
        this.oO.addStartInstruction(new InstructionU32(InstructionType.StackAlloc, (int)1));
        this.oO.addStartInstruction(new InstructionU32(InstructionType.Call, (int)funcSymbol.getOffset()));
        this.oO.addStartInstruction(new InstructionU32(InstructionType.PopN, (int)1));

        return true;
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

        VarSymbol varSymbol = new VarSymbol(variable.getValueString(), SymbolType.VARIABLE, this.typeMap.get(ty.getTokenType()), 0, variable.getStartPos());

        //写入oOfile
        int offset = 0;
        if(this.symbolTable.getCurLevel() ==0 ) {
            varSymbol.setGlobal(true);
            offset = this.oO.addGlobVar(varSymbol);
        } else {
            offset = this.oO.addLocalVar(varSymbol);
        }
        varSymbol.setOffset(offset);

        if(it.nextIf(TokenType.ASSIGN)!=null){
            isInit = true;
            if(this.symbolTable.getCurLevel() == 0 ){
                this.oO.addInstruction(new InstructionU32(InstructionType.GlobA, (int)varSymbol.getOffset()));
            }
            else {
                this.oO.addInstruction(new InstructionU32(InstructionType.LocA, (int)varSymbol.getOffset()));
            }
            DType exprDType = analyseExpr();
            TypeChecker.typeCheck(this.typeMap.get(ty.getTokenType()), exprDType, variable.getStartPos());
            this.oO.addInstruction(new InstructionNone(InstructionType.Store64));

        }

        it.expectToken(TokenType.SEMICOLON);

        // 在符号表中注册一个新的变量符号

        varSymbol.setInitialized(isInit);
        // 如果是全局变量
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

        DType dType = this.typeMap.get(ty.getTokenType());
        VarSymbol varSymbol = new VarSymbol(variable.getValueString(), SymbolType.CONST, dType, 0, variable.getStartPos());


        //写入oofile中
        int offset = 0;
        if(this.symbolTable.getCurLevel() ==0 ) {
            varSymbol.setGlobal(true);
            offset = this.oO.addGlobVar(varSymbol);
        } else {
            offset = this.oO.addLocalVar(varSymbol);
        }
        varSymbol.setOffset(offset);


        it.expectToken(TokenType.ASSIGN);
        //将变量的地址取出
        if(this.symbolTable.getCurLevel() == 0 ){
            this.oO.addInstruction(new InstructionU32(InstructionType.GlobA, (int)varSymbol.getOffset()));
        }
        else {
            this.oO.addInstruction(new InstructionU32(InstructionType.LocA, (int)varSymbol.getOffset()));
        }
        DType exprDType=analyseExpr();
        this.oO.addInstruction(new InstructionNone(InstructionType.Store64));


        it.expectToken(TokenType.SEMICOLON);

        //获取对应的符号数据类型

        varSymbol.setInitialized(true);
        TypeChecker.typeCheck(this.typeMap.get(ty.getTokenType()), exprDType, variable.getStartPos());
        this.symbolTable.insertSymbol(varSymbol);

        //解析常量声明分析完毕
        return;

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

        it.expectToken(TokenType.R_PAREN);
        it.expectToken(TokenType.ARROW);
        Token ty = it.next();
        if(ty.getTokenType()!=TokenType.INT && ty.getTokenType()!=TokenType.DOUBLE && ty.getTokenType()!=TokenType.VOID) {
            throw new AnalyzeError(ErrorCode.InvalidType, ty.getStartPos());
        }

        //设置返回参数
        DType funcReturnType = this.typeMap.get(ty.getTokenType());
        funcSymbol.setdType(funcReturnType);

        //放入符号表中
        this.symbolTable.insertSymbol(funcSymbol);

        //将这个函数写入oOfile中
        this.oO.addFunction(funcSymbol);



        this.branchStack.addFnBranch(funcReturnType);
        //开始分析块语句
        analyseBlockStmt(funcSymbol);
        boolean needRet = this.branchStack.quitFunc(fnIdent.getStartPos());

        if(needRet) {
            this.oO.addInstruction(new InstructionNone(InstructionType.Ret));
        }
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

        VarSymbol varSymbol;
        varSymbol = new VarSymbol(fnParam.getValueString(),
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
                //是一个参数
                VarSymbol funcParam = funcParams.get(i);
                funcParam.setParam(true);
                funcParam.setOffset(i+1);
                this.symbolTable.insertSymbol(funcParam);
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
        Token whileToken = it.expectToken(TokenType.WHILE_KW);
        analyseExpr();
        this.branchStack.addWhileBranch();
        analyseBlockStmt(null);
        this.branchStack.quitBranch(whileToken.getStartPos());
    }


    /**
     * 分析If语句: if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
     * @throws CompileError
     */
    public void analyseIfStmt() throws  CompileError {
        Token ifToken = it.expectToken(TokenType.IF_KW);
        analyseExpr();

        //向分支表中插入新的分支
        this.branchStack.addIfBranch();
        analyseBlockStmt(null);
        this.branchStack.quitBranch(ifToken.getStartPos());
        if(it.check(TokenType.ELSE_KW)) {
            Token elseToken = it.expectToken(TokenType.ELSE_KW);
            //接下来对应两个分支，分别是block_stmt以及if_stmt;
            if (it.check(TokenType.L_BRACE)) {
                //这是block_stmt分支
                this.branchStack.addElseBranch();
                analyseBlockStmt(null);
                this.branchStack.quitBranch(elseToken.getStartPos());
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
        Token re = it.expectToken(TokenType.RETURN_KW);
        Token token = it.peekToken();
        DType dType = DType.VOID;

        if(token.getTokenType()==TokenType.MINUS || token.getTokenType()==TokenType.IDENT ||
            token.getTokenType() == TokenType.UINT_VALUE || token.getTokenType() == TokenType.STRING_VALUE ||
            token.getTokenType() == TokenType.DOUBLE_VALUE) {


            //有返回值,将返回值的地址放在Args[0]处
            this.oO.addInstruction(new InstructionU32(InstructionType.ArgA,(int) 0));
            dType = analyseExpr();
            this.oO.addInstruction(new InstructionNone(InstructionType.Store64));
        }
        this.branchStack.returnOnce(dType,re.getStartPos());

        it.expectToken(TokenType.SEMICOLON);
        this.oO.addInstruction(new InstructionNone(InstructionType.Ret));
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

            //生成加法或者减法指令
            if(token.getTokenType() == TokenType.PLUS) {
                this.oO.addInstruction(new InstructionNone(InstructionType.AddI));
            }
            else if(token.getTokenType() == TokenType.MINUS) {
                this.oO.addInstruction(new InstructionNone(InstructionType.SubI));
            }
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

            //生成乘法或者除法指令
            if(token.getTokenType() == TokenType.MUL) {
                this.oO.addInstruction(new InstructionNone(InstructionType.MulI));
            }
            else if(token.getTokenType() == TokenType.DIV) {
                this.oO.addInstruction(new InstructionNone(InstructionType.DivI));
            }

        }

        return leftType;
    }


    /**
     * 分析Factor表达式： Factor -> Atom { as ( INT | DOUBLE )}
     * @throws CompileError
     */
    public DType analyseFactor() throws CompileError {

        DType dType = analyseAtom();



        while(it.check(TokenType.AS_KW)) {


            //如果类型的左端是VOID则不可以转换
            if(dType == DType.VOID) {
                throw new AnalyzeError(ErrorCode.InvalidOpVoid, it.peekToken().getStartPos());
            }

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
        boolean isNeg = false;
        Token token = null;
        if(it.check(TokenType.MINUS)){
            isNeg = true;
            token = it.next();          //吃掉负号
        }
        DType dType = analyseItem();

        if(isNeg) {
            if(dType!=DType.INT&&dType!=DType.DOUBLE) throw new AnalyzeError(ErrorCode.InvalidExpr, token.getStartPos());
            this.oO.addInstruction(new InstructionNone(InstructionType.NegI));
        }
        return dType;
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
            Token uint = it.expectToken(TokenType.UINT_VALUE);
            dType = DType.INT;

            //生成指令
            this.oO.addInstruction(new InstructionU64(InstructionType.Push, (long)(int) uint.getValue()));
        }
        //对应DOUBLE_VALUE，浮点型字面量
        else if(it.check(TokenType.DOUBLE_VALUE)) {
            Token doubleValue = it.expectToken(TokenType.DOUBLE_VALUE);
            dType = DType.DOUBLE;
            //生成指令
            this.oO.addInstruction(new InstructionU64(InstructionType.Push, (long) doubleValue.getValue()));
        }
        //对应STRING_VALUE, 字符串型字面量
        else if(it.check(TokenType.STRING_VALUE)) {
            Token stringValue = it.expectToken(TokenType.STRING_VALUE);
            dType = DType.STRING;
            //装入全局变量中
            int offset = this.oO.addGlobStr(stringValue.getValueString());
            this.oO.addInstruction(new InstructionU64(InstructionType.Push,(long) offset));
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

        boolean isLib = false;
        Symbol symbol = this.symbolTable.findAllSymbol(identToken.getValueString());
        if(symbol == null) {
            if ((symbol=Lib.genLibFunc(identToken.getValueString(), identToken.getStartPos(), this.oO)) == null){
                throw new AnalyzeError(ErrorCode.NotDeclared, identToken.getStartPos());
            } else {

                //函数调用是一个lib函数
                isLib = true;
            }
        }
        DType leftType = symbol.getdType();

        //  赋值语句
        if(it.check(TokenType.ASSIGN)) {
            Token assign = it.expectToken(TokenType.ASSIGN);
            //如果被赋值的是一个常量，则抛出异常
            if(symbol.getSymbolType() == SymbolType.CONST) {
                throw new AnalyzeError(ErrorCode.AssignToConstant, assign.getStartPos());
            }
            if(symbol.getSymbolType() != SymbolType.VARIABLE) {
                throw new AnalyzeError(ErrorCode.AssignNotToVar, assign.getStartPos());
            }
            assert symbol instanceof VarSymbol;
            VarSymbol varSymbol = (VarSymbol) symbol;

            //生成加载和存储指令
            if(varSymbol.isGlobal()) {
                this.oO.addInstruction(new InstructionU32(InstructionType.GlobA, varSymbol.getOffset()));
            } else if(varSymbol.isParam()) {
                this.oO.addInstruction(new InstructionU32(InstructionType.ArgA, varSymbol.getOffset()));
            } else {
                this.oO.addInstruction(new InstructionU32(InstructionType.LocA, varSymbol.getOffset()));
            }
            DType rightType = analyseExpr();
            this.oO.addInstruction(new InstructionNone(InstructionType.Store64));

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
            assert symbol instanceof FuncSymbol;
            FuncSymbol funcSymbol = (FuncSymbol) symbol;

            //压入返回值slot
            if(funcSymbol.getdType()!=DType.VOID) {
                this.oO.addInstruction(new InstructionU32(InstructionType.StackAlloc, (int)1));
            } else {
                this.oO.addInstruction(new InstructionU32(InstructionType.StackAlloc, (int)0));
            }


            it.expectToken(TokenType.L_PAREN);

            ArrayList argsList = new ArrayList();
            if(it.check(TokenType.L_PAREN) || it.check(TokenType.MINUS) ||
                it.check(TokenType.UINT_VALUE) || it.check(TokenType.STRING_VALUE) ||
                it.check(TokenType.DOUBLE_VALUE) || it.check(TokenType.IDENT)) {

                argsList = analyseCallParam((FuncSymbol) symbol, identToken);
            }
            TypeChecker.callArgTypeCheck(funcSymbol, argsList, identToken);

            //如果调用的是库函数
            if (isLib) {
                this.oO.addInstruction(new InstructionU32(InstructionType.CallName, (int)funcSymbol.getOffset()));
            }
            else {
                this.oO.addInstruction(new InstructionU32(InstructionType.Call, (int)funcSymbol.getOffset()));
            }

            it.expectToken(TokenType.R_PAREN);
            return leftType;
        }


        //只是一个IDENT
        else {
            //生成指令，将变量的的值放在栈顶，其位置由符号表存储
            assert symbol instanceof VarSymbol;
            VarSymbol varSymbol = (VarSymbol) symbol;
            if(varSymbol.isGlobal()) {
                this.oO.addInstruction(new InstructionU32(InstructionType.GlobA, (int)varSymbol.getOffset()));
            }
            else if(varSymbol.isParam()) {
                this.oO.addInstruction(new InstructionU32(InstructionType.ArgA, (int)varSymbol.getOffset()));
            }
            else{
                this.oO.addInstruction(new InstructionU32(InstructionType.LocA, (int)varSymbol.getOffset()));
            }
            this.oO.addInstruction(new InstructionNone(InstructionType.Load64));
            return  leftType;
        }
    }


    /**
     * 分析函数的参数：call_param_list -> expr (',' expr)*
     * 然后检查函数调用的参数是否相同。
     * @param  funcSymbol 函数的符号，用来获取参数信息
     * @param funcToken 函数的Token， 用来获取位置报错
     * @return ArrayList 用来返回调用参数，交给上层检查
     * @throws CompileError
     */
    public ArrayList analyseCallParam(FuncSymbol funcSymbol, Token funcToken) throws CompileError {
        //实际参数的类型表
        ArrayList<DType> actualArgList = new ArrayList<>();
        DType dType = analyseExpr();
        actualArgList.add(dType);
        while(it.check(TokenType.COMMA)) {
            it.expectToken(TokenType.COMMA);
            dType = analyseExpr();
            actualArgList.add(dType);
        }
        return actualArgList;
    }
}
