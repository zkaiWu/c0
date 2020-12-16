package c0.error;

public enum ErrorCode {
    NoError, // Should be only used internally.
    StreamError, EOF, InvalidInput, InvalidIdentifier, IntegerOverflow, // int32_t overflow.
    NeedIdentifier, ConstantNeedValue, NoSemicolon, InvalidVariableDeclaration, IncompleteExpression,
    NotDeclared, AssignToConstant, DuplicateDeclaration, NotInitialized, InvalidAssignment, InvalidPrint, ExpectedToken,
    InvalidVoid, InvalidType, UnExpectToken,DuplicateArgs,FuncNotDeclared,InvalidOpVoid,NotMatchedType,callArgNotMatched,AssignNotToVar,
    NoMain
}
