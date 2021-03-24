package com.lox.javascript;

class Token {
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
}
