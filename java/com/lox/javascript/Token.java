package com.lox.javascript;
import static com.lox.javascript.TokenType.*;

class Token {
  public static final TokenType TOK_FIRST_KEYWORD = TOK_NULL;
  public static final TokenType TOK_LAST_KEYWORD = TOK_AWAIT;
  TokenType type;
  String lexeme;
  Object literal;
  int line; // [location]
  JSAtom ident_atom;
  JSAtom str_str;

  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  public String toString() {
    return type + " " + lexeme + " " + literal;
  }

  static boolean token_is_ident(TokenType tok)
  {
    /* Accept keywords and reserved words as property names */
    return (tok == TokenType.TOK_IDENTIFIER ||
      (tok.ordinal() >= TOK_FIRST_KEYWORD.ordinal() &&
        tok.ordinal() <= TOK_LAST_KEYWORD.ordinal()));
  }
}
