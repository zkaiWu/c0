package c0.error;

import c0.util.Pos;

public abstract class CompileError extends Exception {

    private static final long serialVersionUID = 1L;

    public abstract ErrorCode getErr();

    public abstract Pos getPos();
}
