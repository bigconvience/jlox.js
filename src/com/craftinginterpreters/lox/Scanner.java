package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*; // [static-import]

class Scanner {
  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);

    // 关键字 @https://ecma-international.org/ecma-262/10.0/index.html#sec-reserved-words
    keywords.put("null", TOK_NULL);
    keywords.put("false", TOK_FALSE);
    keywords.put("true", TOK_TRUE);
    keywords.put("if", TOK_IF);
    keywords.put("else", TOK_ELSE);
    keywords.put("return", TOK_RETURN);

    keywords.put("var",  TOK_VAR);
    keywords.put("let",  TOK_LET);
    keywords.put("this", TOK_THIS);
    keywords.put("delete", TOK_DELETE);
    keywords.put("void", TOK_VOID);
    keywords.put("typeof", TOK_TYPEOF);
    keywords.put("new", TOK_NEW);
    keywords.put("in", TOK_IN);
    keywords.put("instance", TOK_INSTANCEOF);
    keywords.put("do", TOK_DO);
    keywords.put("while",  WHILE);
    keywords.put("for",    FOR);
    keywords.put("break", TOK_BREAK);
    keywords.put("continue", TOK_CONTINUE);
    keywords.put("switch", TOK_SWITCH);
    keywords.put("case", TOK_CASE);
    keywords.put("default", TOK_DEFAULT);
    keywords.put("throw", TOK_THROW);
    keywords.put("try", TOK_TRY);
    keywords.put("catch", TOK_CATCH);
    keywords.put("finally", TOK_FINALLY);
    keywords.put("function", TOK_FUNCTION);
    keywords.put("debugger", TOK_DEBUGGER);
    keywords.put("with", TOK_WITH);
    keywords.put("class", TOK_CLASS);
    keywords.put("const", TOK_CONST);
    keywords.put("enum", TOK_ENUM);
    keywords.put("export", TOK_EXPORT);
    keywords.put("extends", TOK_EXTENDS);
    keywords.put("import", TOK_IMPORT);
    keywords.put("super", TOK_SUPER);

    keywords.put("implements", TOK_IMPLEMENTS);
    keywords.put("interface", TOK_INTERFACE);
    keywords.put("package", TOK_PACKAGE);
    keywords.put("private", TOK_PRIVATE);
    keywords.put("protected", TOK_PROTECTED);
    keywords.put("public", TOK_PUBLIC);

    keywords.put("static", TOK_STATIC);
    keywords.put("yield", TOK_YIELD);
    keywords.put("await", TOK_AWAIT);
  }
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break; // [slash]
      case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
      case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
      case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
      case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
      case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } if (match('*')) {
          while (!isAtEnd() && (peek() != '*' || peekNext() != '/')) {
            if (peek() == '\n') {
              line++;
            }
            advance();
          }
          if (peek() == '*' && peekNext() == '/') {
              advance();
              advance();
          } else {
            Lox.error(line, "Need */ for comment");
          }
      } else {
          addToken(SLASH);
        }
        break;

      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;

      case '\'':
      case '"': string(c); break;

      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    // See if the identifier is a reserved word.
    String text = source.substring(start, current);

    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }
  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(NUMBER,
        Double.parseDouble(source.substring(start, current)));
  }
  private void string(char quote) {
    while (peek() != quote && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    // Unterminated string.
    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  } // [peek-next]
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '$' ||
            c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  } // [is-digit]
  private boolean isAtEnd() {
    return current >= source.length();
  }
  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
