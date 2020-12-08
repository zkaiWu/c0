package c0.analyser;

import c0.error.CompileError;
import c0.error.ExpectedError;
import c0.error.TokenizeError;
import c0.tokenizer.Token;
import c0.tokenizer.TokenType;
import c0.tokenizer.Tokenizer;

public class SymbolIter {

    private Tokenizer tokenizer;

    private Token peekedToken =  null;


    public SymbolIter(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    /**
     * 偷看一个Token
     * @return
     * @throws TokenizeError
     */
    public Token peekToken() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个token
     * @return
     * @throws TokenizeError
     */
    public Token next() throws  TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }


    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    public boolean check(TokenType tt) throws TokenizeError {
        var token = peekToken();
        return token.getTokenType() == tt;
    }


    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    public Token nextIf(TokenType tt) throws TokenizeError {
        var token = peekToken();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }


    /**
     * 如果下一个token和期望的token相同，则返回这个token，否则抛出异常
     * @param tt
     * @return
     * @throws CompileError
     */
    public Token expectToken(TokenType tt) throws CompileError {
        var token = peekToken();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedError(tt, token);
        }
    }

}
