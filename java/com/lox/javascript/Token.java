package com.lox.javascript;
import static com.lox.javascript.TokenType.*;

class Token {
  public static final TokenType TOK_FIRST_KEYWORD = TOK_NULL;
  public static final TokenType TOK_LAST_KEYWORD = TOK_AWAIT;
  TokenType type;
  String lexeme;
  Object literal;
  int line_num; // [location]
  JSAtom ident_atom;
  boolean ident_has_escape;
  JSAtom str_str;
  boolean is_reserved;
  int start;
  int end;

  Token(TokenType type, String lexeme, Object literal, int line_num) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line_num = line_num;
  }

  public String toString() {
    return type + " " + lexeme + " " + literal;
  }

  static boolean token_is_ident(TokenType tok)
  {
    /* Accept keywords and reserved words as property names */
    return (tok == TokenType.TOK_IDENT ||
      (tok.ordinal() >= TOK_FIRST_KEYWORD.ordinal() &&
        tok.ordinal() <= TOK_LAST_KEYWORD.ordinal()));
  }

  static boolean token_is_pseudo_keyword(Token token, JSAtomEnum atom) {
    return token_is_pseudo_keyword(token, atom.toJSAtom());
  }

  static boolean token_is_pseudo_keyword(Token token, JSAtom atom) {
    return token.type == TOK_IDENT && token.ident_atom == atom &&
      !token.ident_has_escape;
  }
}
