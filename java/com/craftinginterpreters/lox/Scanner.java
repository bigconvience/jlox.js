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
    keywords.put("print", PRINT);

    // 关键字 @https://ecma-international.org/ecma-262/10.0/index.html#sec-reserved-words
    keywords.put("null", TOK_NULL);
    keywords.put("false", TOK_FALSE);
    keywords.put("true", TOK_TRUE);
    keywords.put("if", TOK_IF);
    keywords.put("else", TOK_ELSE);
    keywords.put("return", TOK_RETURN);

    keywords.put("var", TOK_VAR);
    keywords.put("let", TOK_LET);
    keywords.put("this", TOK_THIS);
    keywords.put("delete", TOK_DELETE);
    keywords.put("void", TOK_VOID);
    keywords.put("typeof", TOK_TYPEOF);
    keywords.put("new", TOK_NEW);
    keywords.put("in", TOK_IN);
    keywords.put("instance", TOK_INSTANCEOF);
    keywords.put("do", TOK_DO);
    keywords.put("while", WHILE);
    keywords.put("for", FOR);
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
  private JSContext ctx;

  Scanner(String source, JSContext ctx) {
    this.source = source;
    this.ctx = ctx;
  }

  List<Token> scanTokens() {
    while (!isAtEnd() && !Lox.hadError) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "",  null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case ':':
        addToken(TOK_COLON);
        break;
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case '[':
        addToken(LEFT_BRACKET);
        break;
      case ']':
        addToken(RIGHT_BRACKET);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        if (match('.') && match('.')) {
          addToken(TOK_ELLIPSIS);
        } else if (isDigit(peekNext())) {
          number(c);
        } else {
          addToken(DOT);
        }
        break;
      case '-':
        if (match('=')) {
          addToken(TOK_MINUS_ASSIGN);
        } else if (match('-')) {
          addToken(TOK_DEC);
        } else {
          addToken(MINUS);
        }
        break;
      case '+':
        if (match('=')) {
          addToken(TOK_PLUS_ASSIGN);
        } else if (match('+')) {
          addToken(TOK_INC);
        } else {
          addToken(PLUS);
        }
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        if (match('=')) {
          addToken(TOK_MUL_ASSIGN);
        } else if (match('*')) {
          if (match('=')) {
            addToken(TOK_POW_ASSIGN);
          } else {
            addToken(TOK_POW);
          }
        } else {
          addToken(TOK_STAR);
        }
        break; // [slash]
      case '%':
        if (match('=')) {
          addToken(TOK_MOD_ASSIGN);
        } else {
          addToken(TOK_MOD);
        }
        break;
      case '!':
        if (match('=')) {
          addToken(match('=') ? TOK_STRICT_NEQ : TOK_NEQ);
        } else {
          addToken(BANG);
        }
        break;
      case '~':
        addToken(BITWISE_BANG);
        break;
      case '&':
        if (match('=')) {
          addToken(TOK_AND_ASSIGN);
        } else if (match('&')){
          addToken(match('=') ? TOK_LAND_ASSIGN : TOK_LAND);
        } else {
          addToken(TOK_BIT_AND);
        }
        break;
      case '|':
        if (match('=')) {
          addToken(TOK_OR_ASSIGN);
        } else if (match('|')){
          addToken(match('=') ? TOK_LOR_ASSIGN : TOK_LOR);
        } else {
          addToken(TOK_BIT_OR);
        }
        break;
      case '^':
        addToken(match('=')?TOK_XOR_ASSIGN: TOK_XOR);
        break;
      case '?':
        if (match('?')) {
          addToken(match('=') ? TOK_DOUBLE_QUESTION_MARK_ASSIGN : TOK_DOUBLE_QUESTION_MARK);
        } else if (match('.') && !isDigit(peek())){
          addToken(TOK_QUESTION_MARK_DOT);
        } else {
          addToken(TOK_QUESTION);
        }
        break;
      case '=':
        if (match('=')) {
          addToken(match('=') ? TOK_STRICT_EQ : TOK_EQ);
        } else {
          addToken(match('>') ? TOK_ARROW : TOK_ASSIGN);
        }
        break;
      case '<':
        if (match('=')) {
          addToken(TOK_LTE);
        } else if(match('<')) {
          addToken(match('=') ? TOK_SHL_ASSIGN : TOK_SHL);
        } else {
          addToken(TOK_LT);
        }
        break;
      case '>':
        if (match('=')) {
          addToken(TOK_GTE);
        } else if (match('>')) {
          if (match('>')) {
            addToken(match('=') ? TOK_SHR_ASSIGN : TOK_SHR);
          } else {
            addToken(match('=') ? TOK_SAR_ASSIGN : TOK_SAR);
          }
        } else {
          addToken(TokenType.TOK_GT);
        }
        break;
      case '`':
        template(c);
        break;
      case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else if (match('*')) {
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
        } else if (match('=')) {
          addToken(TOK_DIV_ASSIGN);
        } else {
          addToken(SLASH);
        }
        break;
      case '\\':
      case '#':
        if (match('u')) {
          //todo
        }
        break;
      case '\f':
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;

      case '\'':
      case '\"':
        string(c);
        break;

      default:
        if (isDigit(c)) {
          number(c);
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
    if (type == null) type = TOK_IDENTIFIER;
    addToken_Ident(type);
  }

  //https://ecma-international.org/ecma-262/10.0/index.html#prod-NumericLiteral
  private void number(char c) {
    int radix = 0;
    int startIndex = start;
    if (c == '0') {
      if (match('x') || match('X')) {
        radix = 16;
      } else if (match('o') || match('O')) {
        radix = 8;
      } else if (match('b') || match('B')) {
        radix = 2;
      }
      if (radix == 0) {
        radix = 10;
      } else {
        startIndex = start + 2;
        while (toDigit(peek()) < radix) {
          advance();
        }
      }

      addToken(TOK_NUMBER,
        (double)Integer.parseInt(source.substring(startIndex, current), radix));
      return;
    }

    radix = 10;

    while (toDigit(peek()) < radix) {
      advance();
    }
    if (c != '.'
      && peek() != '.'
      && peek() != 'e'
      && peek() != 'E') {
      addToken(TOK_NUMBER,
        (double)Integer.parseInt(source.substring(start, current), radix));
      return;
    }

    if (c == '.') {
      if (!isDigit(peek())) {
       unknowToken(c);
      }
    } else if (peek() == '.') {
      advance();
    }
    while (isDigit(peek())) {
      advance();
    }

    if (match('e') || match('E')) {
      match('+');
      match('-');
      if (!isDigit(peek())) {
        unknowToken(peek());
        return;
      }
      while (isDigit(peek())) {
        advance();
      }
    }
    addToken(TOK_NUMBER,
      Double.parseDouble(source.substring(start, current)));
  }

  private void unknowToken(char c) {
    Lox.error(line, "Uncaught SyntaxError: Unexpected token '"+c+"'");
  }


  private int toDigit(int c) {
    if (c >= '0' && c <= '9')
      return c - '0';
    else if (c >= 'A' && c <= 'Z')
      return c - 'A' + 10;
    else if (c >= 'a' && c <= 'z')
      return c - 'a' + 10;
    else
      return 36;
  }

  /**
   * https://ecma-international.org/ecma-262/10.0/index.html#sec-literals-string-literals
   *
   * @param quote
   */
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
    addToken_String(TOK_STRING, value);
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

  private void addToken_String(TokenType type, String literal) {
    String text = source.substring(start, current);
    JSAtom str = ctx.rt.JS_NewAtomStr(literal);
    Token token = new Token(type, text, literal, line);
    token.str_str = str;
    tokens.add(token);
  }

  private void addToken_Ident(TokenType type) {
    String text = source.substring(start, current);
    JSAtom ident = ctx.rt.JS_NewAtomStr(text);
    Token token = new Token(type, text, null, line);
    token.ident_atom = ident;
    tokens.add(token);
  }

  //todo
  private void template(char c) {

  }
}
