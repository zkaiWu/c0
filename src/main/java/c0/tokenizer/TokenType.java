package c0.tokenizer;

public enum TokenType {

    /** 空 */
    None,
    /** 标识符 */
    IDENT,

    /** ------------- 基本类型token ------------**/
    /** unsigned int 字面量*/
    UINT_VALUE,
    /** string 字符串字面量*/
    STRING_VALUE,
    /** double 浮点型字面量*/
    DOUBLE_VALUE,
    /** char 字符型字面量 */

    /** ------------- 关键字token --------------**/
    /** 整型声明 */
    INT,
    /** 字符串声明 */
    STRING,
    /** 空类型声明 */
    VOID,
    /** 浮点数声明 */
    DOUBLE,
    /** 字符声明 */
    CHAR,
    /** 函数 fn */
    FN_KW,
    /** 声明 let */
    LET_KW,
    /** const */
    CONST_KW,
    /** as */
    AS_KW,
    /** while */
    WHILE_KW,
    /** if */
    IF_KW,
    /** else */
    ELSE_KW,
    /** return */
    RETURN_KW,
    /** break */
    BREAK_KW,
    /** continue */
    CONTINUE_KW,


    /** -------------- 运算符token ------------------**/

    /** 加号 */
    PLUS,
    /** 减号 */
    MINUS,
    /** 乘号 */
    MUL,
    /** 除号 */
    DIV,
    /** 赋值号 */
    ASSIGN,
    /** 等于号 */
    EQ,
    /** 不等号*/
    NEQ,
    /** 小于号 */
    LT,
    /** 大于号 */
    GT,
    /** 小于等于 */
    LE,
    /** 大于等于 */
    GE,
    /** 左括号 */
    L_PAREN,
    /** 右括号 */
    R_PAREN,
    /** 左大括号 */
    L_BRACE,
    /** 右大括号 */
    R_BRACE,
    /** 箭头符号 */
    ARROW,
    /** 逗号 */
    COMMA,
    /** 冒号 */
    COLON,
    /** 分号 */
    SEMICOLON,

    /** 文件结束符 **/
    EOF;

}
