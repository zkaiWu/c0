package c0.error;

import c0.util.Pos;

public class TokenizeError extends CompileError{
    // auto-generated
    private static final long serialVersionUID = 1L;

    private ErrorCode err;
    private Pos errorPos;

    public TokenizeError(ErrorCode err, Pos errorPos) {
        super();
        this.err = err;
        this.errorPos = errorPos;
    }

    public TokenizeError(ErrorCode err, Integer row, Integer col) {
        super();
        this.err = err;
        this.errorPos = new Pos(row, col);
    }

    @Override
    public ErrorCode getErr() {
        return this.err;
    }

    @Override
    public Pos getPos(){
        return this.errorPos;
    }

}
