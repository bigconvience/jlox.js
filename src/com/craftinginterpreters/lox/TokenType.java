package com.craftinginterpreters.lox;

/**
 * 参考
 * 1.ecma @https://ecma-international.org/ecma-262/10.0/index.html#sec-tokens
 * 2.MDN @https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Operators
 * 3.quickjs @https://github.com/bellard/quickjs
 */
enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, TOK_STAR, TOK_MOD,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    TOK_ASSIGN,

    // Keywords.
    AND,  NIL, OR,
    PRINT,

    //CommonToken:: https://ecma-international.org/ecma-262/10.0/index.html#sec-tokens
    // Literals.
    TOK_NUMBER,
    TOK_STRING,
    TOK_TEMPLATE,
    TOK_IDENTIFIER,
    TOK_REGEXP,

    // Punctuators:: https://ecma-international.org/ecma-262/10.0/index.html#prod-Punctuator
    TOK_MUL_ASSIGN,
    TOK_DIV_ASSIGN,
    TOK_MOD_ASSIGN,
    TOK_PLUS_ASSIGN,
    TOK_MINUS_ASSIGN,
    TOK_SHL_ASSIGN,
    TOK_SAR_ASSIGN,
    // >>>= @https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Unsigned_right_shift_assignment
    TOK_SHR_ASSIGN,
    TOK_AND_ASSIGN,
    TOK_AND,
    TOK_XOR_ASSIGN,
    TOK_XOR,
    TOK_OR_ASSIGN,
    TOK_OR,

    TOK_POW_ASSIGN,
    TOK_LAND_ASSIGN,
    TOK_LOR_ASSIGN,
    // ??= @https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Logical_nullish_assignment
    TOK_DOUBLE_QUESTION_MARK_ASSIGN,
    TOK_DEC,
    TOK_INC,
    TOK_SHL,
    TOK_SAR,
    // >>> @https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Unsigned_right_shift
    TOK_SHR,
    TOK_LT,
    TOK_LTE,
    TOK_GT,
    TOK_GTE,
    TOK_EQ,
    // === @https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Strict_equality
    TOK_STRICT_EQ,
    TOK_NEQ,
    // !== @ https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Strict_inequality
    TOK_STRICT_NEQ,
    TOK_LAND,
    TOK_LOR,
    TOK_POW,
    TOK_ARROW,
    TOK_ELLIPSIS,
    TOK_DOUBLE_QUESTION_MARK,
    TOK_QUESTION_MARK_DOT,
    TOK_QUESTION,
    TOK_ERROR,
    TOK_PRIVATE_NAME,
    TOK_EOF,

    // keywords: @https://ecma-international.org/ecma-262/10.0/index.html#sec-reserved-words
    TOK_NULL, /* must be first */
    TOK_FALSE,
    TOK_TRUE,

    TOK_IF,
    TOK_ELSE,
    TOK_RETURN,
    TOK_VAR,
    TOK_LET,
    TOK_THIS,
    TOK_DELETE,
    TOK_VOID,
    TOK_TYPEOF,
    TOK_NEW,
    TOK_IN,
    TOK_INSTANCEOF,
    TOK_DO,
    WHILE,
    FOR,
    TOK_BREAK,
    TOK_CONTINUE,
    TOK_SWITCH,
    TOK_CASE,
    TOK_DEFAULT,
    TOK_THROW,
    TOK_TRY,
    TOK_CATCH,
    TOK_FINALLY,
    TOK_FUNCTION,
    TOK_DEBUGGER,
    TOK_WITH,
    TOK_CLASS,
    TOK_CONST,
    TOK_ENUM,
    TOK_EXPORT,
    TOK_EXTENDS,
    TOK_IMPORT,
    TOK_SUPER,

    // FutureReservedWord:: https://ecma-international.org/ecma-262/10.0/index.html#prod-FutureReservedWord`
    TOK_IMPLEMENTS,
    TOK_INTERFACE,
    TOK_PACKAGE,
    TOK_PRIVATE,
    TOK_PROTECTED,
    TOK_PUBLIC,

    TOK_STATIC,
    TOK_YIELD,
    TOK_AWAIT, /* must be last */
    TOK_OF,

    EOF
}
