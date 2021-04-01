package com.lox.javascript;

import com.lox.clibrary.stdlib_h;

import java.util.*;

import static com.lox.javascript.JSErrorEnum.JS_SYNTAX_ERROR;
import static com.lox.javascript.JSVarDefEnum.*;
import static com.lox.javascript.JSVarKindEnum.*;
import static com.lox.javascript.Token.token_is_ident;
import static com.lox.javascript.TokenType.*;

class Parser {


  public static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;
  private JSFunctionDef curFunc;
  private com.lox.javascript.Scanner scanner;
  JSContext ctx;
  String fileName;
  private JSRuntime rt;
  private boolean is_module;

  Parser(Scanner scanner, JSContext ctx, JSFunctionDef curFunc, JSRuntime rt) {
    this.scanner = scanner;
    this.tokens = scanner.scanTokens();
    this.ctx = ctx;
    this.curFunc = curFunc;
    this.rt = rt;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements; // [parse-error-handling]
  }

  boolean parse_program() {
    boolean success = true;
    parse_program(this);
    return success;
  }

  static void parse_program(Parser s) {
    JSFunctionDef fd = s.curFunc;
    int idx;
    fd.is_global_var = (fd.eval_type == LoxJS.JS_EVAL_TYPE_GLOBAL);

    fd.is_global_var = (fd.eval_type == LoxJS.JS_EVAL_TYPE_GLOBAL) ||
      (fd.eval_type == LoxJS.JS_EVAL_TYPE_MODULE) ||
      (fd.js_mode & LoxJS.JS_MODE_STRICT) == 0;

    if (!s.is_module) {
      /* hidden variable for the return value */
      fd.eval_ret_idx = idx = JSVarDef.add_var(s.ctx, fd, JSAtomEnum.JS_ATOM__ret_.toJSAtom());
      if (idx < 0)
         stdlib_h.abort();
    }

    
    List<Stmt> stmts = s.parse();
//    Stmt.Block block = new Stmt.Block(0, stmts);
    fd.body = stmts;
  }

  private Expr expression() {
    return assignment();
  }

  private Stmt declaration() {
    try {
      if (match(TOK_CLASS)) return classDeclaration();
      if (match(TOK_FUNCTION)) return function("function");
      if (match(TOK_VAR, TOK_LET, TOK_CONST)) return js_parse_var(previous());

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt classDeclaration() {
    Token name = consume(TOK_IDENTIFIER, "Expect class name.");
    consume(LEFT_BRACE, "Expect '{' before class body.");

    List<JSFunctionDef> methods = new ArrayList<>();
    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      methods.add(function("method"));
    }

    consume(RIGHT_BRACE, "Expect '}' after class body.");

    return new Stmt.Class(name, methods);
  }

  private Stmt statement() {
    if (match(TOK_FOR)) return forStatement();
    if (match(TOK_IF)) return ifStatement();
    if (match(PRINT)) return printStatement();
    if (match(TOK_RETURN)) return returnStatement();
    if (match(WHILE)) return whileStatement();
    if (match(LEFT_BRACE)) {
      int start_line = previous().line;
      return new Stmt.Block(start_line, block());
    }

    return expressionStatement();
  }

  private Stmt forStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'for'.");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(TOK_VAR)) {
      initializer = js_parse_var(previous());
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expect ';' after loop condition.");

    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Expect ')' after for clauses.");
    Stmt body = statement();

    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(
        body,
        new Stmt.Expression(increment)));
    }

    if (condition == null) condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt ifStatement() {
    int expr_line = previous().line;
    Expr condition = js_parse_expr_paren();

    int then_line = previous().line;
    Stmt thenBranch = statement();
    thenBranch.line_number = then_line;
    Stmt elseBranch = null;
    if (match(TOK_ELSE)) {
      int else_line = previous().line;
      elseBranch = statement();
      elseBranch.line_number = else_line;
    }

    int end_line = previous().line;
    Stmt.If stmt = new Stmt.If(condition, thenBranch, elseBranch);
    stmt.line_number = expr_line;
    stmt.end_line = end_line;
    return stmt;
  }

  private Expr js_parse_expr_paren() {
    consume(LEFT_PAREN, "Expect '('");
    Expr expr = expression();
    consume(RIGHT_PAREN, "Expect ')'"); // [parens]
    return expr;
  }

  private Stmt printStatement() {
    Expr value = expression();
    Token tok = consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(tok.line, value);
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(SEMICOLON)) {
      value = expression();
    }

    consume(SEMICOLON, "Expect ';' after return value.");
    return new Stmt.Return(keyword, value);
  }

  private Stmt js_parse_var(Token tok) {
    JSFunctionDef fd = curFunc;

    Token name = consume(TOK_IDENTIFIER, "Expect variable name.");

    JSVarDefEnum varDefEnum= getJsVarDefEnum(tok);

    Expr initializer = null;
    if (match(TOK_ASSIGN)) {
      initializer = expression();
    }
    TokenType type= tok.type;
    if (initializer == null) {
      if (type == TOK_CONST) {
        js_parse_error("missing initializer for const variable");
      }
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");
    Stmt stmt = new Stmt.Var(tok.line, varDefEnum, name.ident_atom, initializer);
    return stmt;
  }

  private JSVarDefEnum getJsVarDefEnum(Token tok) {
    JSVarDefEnum varDefType = null;
    switch (tok.type) {
      case TOK_LET:
        varDefType = JS_VAR_DEF_LET;
        break;
      case TOK_CONST:
        varDefType = JS_VAR_DEF_CONST;
        break;
      case TOK_VAR:
        varDefType = JS_VAR_DEF_VAR;
        break;
      case TOK_CATCH:
        varDefType = JS_VAR_DEF_CATCH;
        break;
      default:
        error(tok, "unknown declaration token");
    }
    return varDefType;
  }
  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
//    Token tok = consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(previous().line, expr);
  }

  private JSFunctionDef function(String kind) {
    boolean isExpr = false;
    JSFunctionDef fd = curFunc;
    Token name = consume(TOK_IDENTIFIER, "Expect " + kind + " name.");
    consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Cannot have more than 255 parameters.");
        }

        parameters.add(consume(TOK_IDENTIFIER, "Expect parameter name."));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Expect ')' after parameters.");

    fd = ctx.js_new_function_def(fd, false, isExpr, fileName, name.line);
    fd.func_name = rt.JS_NewAtomStr(name.lexeme);
    curFunc = fd;

    consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
//    fd.body = new Stmt.Block(block());
    fd.body = block();
    curFunc = fd.parent;
    return fd;
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

  private Expr condition() {
    Expr expr = coalesce();

    if (match(TOK_QUESTION)) {
      Expr middle = assignment();
      if (match(TOK_COLON)) {
        Expr last = assignment();
        return new Expr.Condition(expr, middle, last);
      }
    }
    return expr;
  }

  private Expr coalesce() {
    Expr expr = or();
    if (match(TOK_DOUBLE_QUESTION_MARK)) {
      Expr right = equality();
      return new Expr.Coalesce(expr, right);
    }
    return expr;
  }

  private Expr assignment() {

    if (match(TOK_YIELD)) {
      return null;
    }

    Expr expr = condition();

    if (match(TOK_ASSIGN,
      TOK_MUL_ASSIGN,
      TOK_DIV_ASSIGN,
      TOK_MOD_ASSIGN,
      TOK_PLUS_ASSIGN,
      TOK_MINUS_ASSIGN,
      TOK_SHL_ASSIGN,
      TOK_SAR_ASSIGN,
      TOK_SHR_ASSIGN,
      TOK_AND_ASSIGN,
      TOK_XOR_ASSIGN,
      TOK_OR_ASSIGN,
      TOK_POW_ASSIGN,
      TOK_LAND_ASSIGN,
      TOK_LOR_ASSIGN,
      TOK_LAND_ASSIGN,
      TOK_LOR_ASSIGN, TOK_DOUBLE_QUESTION_MARK_ASSIGN)) {
      Token operator = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Get) {
        Expr.Get get = (Expr.Get) expr;
        return new Expr.Set(get.object, get.name, value);
      } else {
        Expr.Assign assign = new Expr.Assign(expr, operator.type, value);
        return assign;
      }

//      error(operator, "Invalid assignment target."); // [no-throw]
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (match(TOK_LOR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = bitwiseOr();

    while (match(TOK_LAND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwiseOr() {
    Expr expr = bitwiseXor();
    while (match(TOK_BIT_OR)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Bitwise(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwiseXor() {
    Expr expr = bitwiseAnd();
    while (match(TOK_XOR)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Bitwise(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwiseAnd() {
    Expr expr = equality();
    while (match(TOK_BIT_AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Bitwise(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while (match(TOK_NEQ, TokenType.TOK_EQ,
      TOK_STRICT_EQ, TOK_STRICT_NEQ)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = bitwiseShift();

    while (match(TokenType.TOK_GT, TokenType.TOK_GTE, TokenType.TOK_LT, TokenType.TOK_LTE,
      TOK_IN, TOK_INSTANCEOF)) {
      Token operator = previous();
      Expr right = addition();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwiseShift() {
    Expr expr = addition();

    while (match(TOK_SHL, TOK_SHR, TOK_SAR)) {
      Token operator = previous();
      Expr right = addition();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr addition() {
    Expr expr = multiplication();

    while (match(TOK_MINUS, TOK_PLUS)) {
      Token operator = previous();
      Expr right = multiplication();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr multiplication() {
    Expr expr = pow();

    while (match(TOK_SLASH, TOK_STAR, TOK_MOD)) {
      Token operator = previous();
      Expr right = pow();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr pow() {
    Expr expr = unary();
    while (match(TOK_POW)) {
      Token operator = previous();
      Expr right = pow();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr unary() {
    if (match(TOK_MINUS, TOK_PLUS, TOK_BANG, TOK_BITWISE_BANG,
      TOK_DEC, TOK_INC,
      TOK_VOID, TOK_TYPEOF, TOK_DELETE, TOK_AWAIT)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return postfix();
  }

  private Expr postfix() {
    Expr expr = primary();
    if (match(TOK_DEC, TOK_INC)) {
      Token token = previous();
      return new Expr.Postfix(token, expr);
    }

    int optional_chaining_label = -1;
    for (;;) {

      if (match(TOK_DOT)) {
        if (match(TOK_PRIVATE_NAME)) {

        } else {
          Token token = advance();
          if (!token_is_ident(token.type)) {
            js_parse_error("expecting field name");
          }
          Expr.Get get = new Expr.Get(expr, token);
          return get;
        }
      } else {
        break;
      }
    }
    return expr;
  }


  private Expr primary() {
    Object val;
    if (match(TOK_FALSE)) return new Expr.Literal(false);
    if (match(TOK_TRUE)) return new Expr.Literal(true);
    if (match(TOK_NULL)) return new Expr.Literal(null);

    if (match(TOK_NUMBER)) {
      return new Expr.Literal(previous().literal);
    }
    if (match(TOK_TEMPLATE)) {
      return new Expr.Literal(previous().literal);
    }
    if (match(TOK_STRING)) {
      return emit_push_const(previous(), true);
    }

    if (match(TOK_THIS)) return new Expr.This(previous());

    if (match(TOK_IDENTIFIER)) {
      return new Expr.Variable(previous(), curFunc.scope_level);
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    if (match(LEFT_BRACE)) {
      Expr.ObjectLiteral literal = parseObjectLiteral();
      return literal;
    }

    if (match(TOK_LEFT_BRACKET)) {
      Expr.Literal literal = parseArrayLiteral();
      return literal;
    }

    throw error(peek(), "Expect expression.");
  }

  private  Expr.Literal emit_push_const(Token token, boolean asAtom) {
    Expr.Literal literal = null;
    if (token.type == TOK_STRING && asAtom) {
      JSAtom atom = token.str_str;
      if (atom != JSAtom.JS_ATOM_NULL && !atom.__JS_AtomIsTaggedInt()) {
        literal = new Expr.Literal(atom);
      }
    }

    return literal;
  }

  private Expr.ObjectLiteral parseObjectLiteral() {
    Map<String, Expr> prop = new HashMap<>();
    Expr.ObjectLiteral literal = new Expr.ObjectLiteral(prop);
    while (!match(RIGHT_BRACE)) {
      String name = parsePropertyName();
      match(TOK_COLON);
      Expr expr = assignment();
      prop.put(name, expr);
      match(COMMA);
    }

    return literal;
  }

  private String parsePropertyName() {
    if (match(TOK_IDENTIFIER, TOK_STRING)) {
      return previous().lexeme;
    } else if (match(TOK_NUMBER)) {
      return previous().lexeme;
    }
    return null;
  }

  private Expr.Literal parseArrayLiteral() {
    return new Expr.Literal(null);
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case TOK_CLASS:
        case TOK_FUNCTION:
        case TOK_VAR:
        case TOK_FOR:
        case TOK_IF:
        case WHILE:
        case PRINT:
        case TOK_RETURN:
          return;
      }

      advance();
    }
  }

  public int push_scope() {
    if (curFunc != null) {
      JSFunctionDef fd = curFunc;
      int scope = fd.add_scope();
      return scope;
    }
    return 0;
  }

  int  js_parse_error(String fmt, Object... args)
  {
    JSThrower.JS_ThrowError2(ctx, JS_SYNTAX_ERROR, fmt, Arrays.asList(args), false);

    return -1;
  }
}
