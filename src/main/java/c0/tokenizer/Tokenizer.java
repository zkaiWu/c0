package c0.tokenizer;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import c0.error.ErrorCode;
import c0.error.TokenizeError;
import c0.util.Pos;

public class Tokenizer {

    private StringIter it;
    private Map<String, TokenType> keywordTable;

    public Tokenizer(StringIter it){
        this.it = it;

        //存入c0所规定的关键词
        this.keywordTable = new Hashtable<>();
        this.keywordTable.put("fn", TokenType.FN_KW);
        this.keywordTable.put("let", TokenType.LET_KW);
        this.keywordTable.put("const", TokenType.CONST_KW);
        this.keywordTable.put("as", TokenType.AS_KW);
        this.keywordTable.put("while", TokenType.WHILE_KW);
        this.keywordTable.put("if", TokenType.IF_KW);
        this.keywordTable.put("else", TokenType.ELSE_KW);
        this.keywordTable.put("return", TokenType.RETURN_KW);
        this.keywordTable.put("break", TokenType.BREAK_KW);
        this.keywordTable.put("continue", TokenType.CONTINUE_KW);
        this.keywordTable.put("int", TokenType.INT);
        this.keywordTable.put("string", TokenType.STRING);
        this.keywordTable.put("void", TokenType.VOID);
    }


    /**
     * 读取下一个token
     *
     * @return 返回分析出的token
     * @throws TokenizeError
     */
    public Token nextToken() throws  TokenizeError{

        it.readAll();                       //将所有的文本读入，如果已经读入，则不用重复读入
        skipSpaceCharacters();              //跳过所有的空白符号

        //如果读到结尾，则返回一个EOF的token
        if(it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if(Character.isAlphabetic(peek) || peek == '_'){
            return lexIdentOrKeyword();
        }else if(Character.isDigit(peek)){
            return lexUInt();
        }else if(peek == '\''){
            return lexChar();
        }else if(peek == '\"'){
            return lexString();
        } else if(peek == '/'){
            return lexComment();
        } else {
            return lexOpOrUnknown();
        }
    }


    /**
     * 分析标识符或者关键字
     * @return
     */
    public Token lexIdentOrKeyword() throws TokenizeError {
        String tokenValue = "";
        Pos startPos = it.currentPos();


        //循环读取字符，将其存储在value 中
        while(Character.isDigit(it.peekChar())|| Character.isAlphabetic(it.peekChar()) || it.peekChar() == '_') {
            tokenValue += String.valueOf(it.nextChar());
            if(it.isEOF()){
                break;
            }
        }

        Pos endPos = it.currentPos();
        if(this.keywordTable.get(tokenValue)!=null){
            return new Token(this.keywordTable.get(tokenValue), tokenValue, startPos, endPos);
        }
        else{
            return new Token(TokenType.IDENT, tokenValue, startPos, endPos);
        }
    }


    /**
     * 解析一个无符号整型， 其token为UINT
     * @return
     * @throws TokenizeError
     */
    public Token lexUInt() throws TokenizeError{

        String tokenValue = "";
        Pos startPos = it.currentPos();

        //循环读取字符，保证其是一个数字符号, 存储进value中，并转为int值存入token中
        while(Character.isDigit(it.peekChar())){
            tokenValue += String.valueOf(it.nextChar());
            if(it.isEOF()) {
                break;
            }
        }

        Pos endPos = it.currentPos();
        int result = 0;
        try {
            result = Integer.valueOf(tokenValue);
        }catch(NumberFormatException ex){
            throw new TokenizeError(ErrorCode.IntegerOverflow, startPos);
        }
        return new Token(TokenType.UINT_VALUE, result, startPos, endPos);
    }


    /**
     * 解析字符串，其token为STRING_VALUE
     * @return
     * @throws TokenizeError
     */
    public Token lexString() throws  TokenizeError {


        StringBuffer value = new StringBuffer();
        Pos startPos = it.currentPos();


        char temp = it.nextChar();
        if(temp != '\"') throw new TokenizeError(ErrorCode.InvalidString, it.currentPos());
        while((temp = it.nextChar())!='\"') {
            if(it.isEOF()) {
                throw new TokenizeError(ErrorCode.InvalidString, startPos);
            }
            if(temp == '\\') {
                temp = it.nextChar();
                if(it.isEOF()) {
                    throw new TokenizeError(ErrorCode.InvalidString, startPos);
                }
                switch (temp) {
                    case '\\': temp = '\\'; break;
                    case '\'': temp = '\''; break;
                    case '\"': temp = '\"'; break;
                    case 'n': temp = '\n'; break;
                    case 't': temp = '\t'; break;
                    case 'r': temp = '\r'; break;
                    default: throw new TokenizeError(ErrorCode.InvalidString, startPos);
                }
            }
            value.append(temp);
            if(it.isEOF()) {
                throw new TokenizeError(ErrorCode.InvalidString, startPos);
            }
        }
        Pos endPos = it.currentPos();
        return new Token(TokenType.STRING_VALUE, value.toString(), startPos, endPos);
    }

    /**
     * 解析一个字符,其token为CHAR
     * @return
     * @throws TokenizeError
     */
    public Token lexChar() throws  TokenizeError{
        char value = 0;
        Pos startPos = it.currentPos();

        char temp = it.nextChar();
        if(it.isEOF()) {
            throw new TokenizeError(ErrorCode.InvalidChar, startPos);
        }
        value = it.nextChar();
        if(it.isEOF() || value == '\'') {
            throw new TokenizeError(ErrorCode.InvalidChar, startPos);
        }
        if(value == '\\') {
            value = it.nextChar();
            switch (value) {
                case '\\': value = '\\'; break;
                case '\'': value = '\''; break;
                case '\"': value = '\"'; break;
                case 'n': value = '\n'; break;
                case 't': value = '\t'; break;
                case 'r': value = '\r'; break;
                default: throw new TokenizeError(ErrorCode.InvalidChar, startPos);
            }
        }
        char c = it.nextChar();
        if(c != '\'') throw  new TokenizeError(ErrorCode.InvalidChar, startPos);
        Pos endPos=it.currentPos();
        return new Token(TokenType.UINT_VALUE, (int)value, startPos, endPos);
    }

    /**
     * 解析注释，读到行尾，然后重新调用nextToken返回下一个Token
     * @return
     * @throws TokenizeError
     */
    public Token lexComment() throws TokenizeError{
        it.nextChar();
        char temp = it.peekChar();
        if(temp!='/') {
            return new Token(TokenType.DIV, "/", it.previousPos(), it.currentPos());
        }
        it.nextChar();
        while(!it.isEOF()&&it.peekChar()!='\n') {
            it.nextChar();
        }
        return nextToken();
    }


    /**
     * 分析运算符或者对不认识的字符抛出异常
     * @return Token
     */
    public Token lexOpOrUnknown() throws TokenizeError{

        //读取一个字符
        char op = it.nextChar();
        if(op == '+') {
            //token 为 + 号
            return new Token(TokenType.PLUS, "+", it.previousPos(), it.currentPos());
        }
        else if(op == '-'){
            //有两种情况， 分别是 '-' 号以及 "->" 符号
            char peek = it.peekChar();
            Pos startPos = it.previousPos();
            if (peek == '>'){
                char op2 = it.nextChar();
                assert op2=='>';
                Pos endPos = it.currentPos();
                //token为 -> 号
                return new Token(TokenType.ARROW, "->", startPos, endPos);
            }
            else {
                return new Token(TokenType.MINUS, "-", startPos, it.currentPos());
            }
        }
        else if(op == '*'){
            // token 为 '*' 号
            return new Token(TokenType.MUL,"*", it.previousPos(), it.currentPos());
        }
        else if(op == '/'){
            //token 为 '/' 号
            return new Token(TokenType.DIV, "/", it.previousPos(), it.currentPos());
        }
        else if(op == '='){
            //有两种情况，分别是'='号和'=='号
            char peek = it.peekChar();
            Pos startPos = it.previousPos();
            if(peek == '='){
                char op2 = it.nextChar();
                assert op2 == '=';
                Pos endPos = it.currentPos();
                return new Token(TokenType.EQ, "==", startPos, endPos);
            }
            else {
                return  new Token(TokenType.ASSIGN, "=", startPos, it.currentPos());
            }
        }
        else if(op == '!') {
            // token 为 "!=" 号
            Pos startPos = it.previousPos();
            char op2 = it.nextChar();
            if(op2 == '='){
                return new Token(TokenType.NEQ, "!=", startPos, it.currentPos());
            }
            else{
                throw new TokenizeError(ErrorCode.InvalidInput, startPos);
            }
        }
        else if(op == '<') {
            //有两种情况， 分别是 "<=" 以及 "<"
            Pos startPos = it.previousPos();
            char peek = it.peekChar();
            if(peek == '='){
                // "<="
                char op2 = it.nextChar();
                assert op2 == '=';
                Pos endPos = it.currentPos();
                return new Token(TokenType.LE, "<=", startPos, endPos);
            }
            else {
                return new Token(TokenType.LT, "<", startPos, it.currentPos());
            }
        }
        else if(op == '>') {
            //有两种情况， 分别是 "<=" 以及 "<"
            Pos startPos = it.previousPos();
            char peek = it.peekChar();
            if (peek == '=') {
                // "<="
                char op2 = it.nextChar();
                assert op2 == '=';
                Pos endPos = it.currentPos();
                return new Token(TokenType.GE, ">=", startPos, endPos);
            } else {
                return new Token(TokenType.GT, ">", startPos, it.currentPos());
            }
        }
        else if(op == '('){
            //token 为 '('
            return new Token(TokenType.L_PAREN, "(", it.previousPos(), it.currentPos());
        }
        else if(op == ')'){
            //token 为 ')'
            return new Token(TokenType.R_PAREN, ")", it.previousPos(), it.currentPos());
        }
        else if(op == '{'){
            //token 为 '{'
            return new Token(TokenType.L_BRACE, "{", it.previousPos(), it.currentPos());

        }
        else if(op== '}' ){
            //token 为 '}'
            return new Token(TokenType.R_BRACE, "}", it.previousPos(), it.currentPos());
        }
        else if(op == ','){
            //token 为 ','
            return new Token(TokenType.COMMA, ",", it.previousPos(), it.currentPos());
        }
        else if(op == ':') {
            //token 为 ':'
            return new Token(TokenType.COLON, ":", it.previousPos(), it.currentPos());
        }
        else if(op == ';') {
            return new Token(TokenType.SEMICOLON, ";", it.previousPos(), it.currentPos());
        }
        else {
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }


    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
