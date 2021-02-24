package com.craftinginterpreters.lox;

import java.util.*;

import static com.craftinginterpreters.lox.JSVarDefEnum.*;
import static com.craftinginterpreters.lox.JSVarKindEnum.*;
import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
  static final int GLOBAL_VAR_OFFSET = 0x40000000;
  static final int ARGUMENT_VAR_OFFSET = 0x20000000;

  private static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;
  private JSFunctionDef curFunc;
  private Scanner scanner;
  JSContext ctx;
  String fileName;

  Parser(Scanner scanner, JSContext ctx, JSFunctionDef curFunc) {
    this.scanner = scanner;
    this.tokens = scanner.scanTokens();
    this.ctx = ctx;
    this.curFunc = curFunc;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements; // [parse-error-handling]
  }

  boolean parseProgram() {
    boolean success = true;
    JSFunctionDef fd = curFunc;
    fd.isGlobalVar = (fd.evalType == JSEvaluator.JS_EVAL_TYPE_GLOBAL);

    curFunc.body = parse();
    return success;
  }

  private Expr expression() {
    return assignment();
  }

  private Stmt declaration() {
    try {
      if (match(TOK_CLASS)) return classDeclaration();
      if (match(TOK_FUNCTION)) return function("function");
      if (match(TOK_VAR, TOK_LET, TOK_CONST)) return varDeclaration(previous());

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
    if (match(FOR)) return forStatement();
    if (match(TOK_IF)) return ifStatement();
    if (match(PRINT)) return printStatement();
    if (match(TOK_RETURN)) return returnStatement();
    if (match(WHILE)) return whileStatement();
    if (match(LEFT_BRACE)) return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt forStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'for'.");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(TOK_VAR)) {
      initializer = varDeclaration(previous());
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
    consume(LEFT_PAREN, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after if condition."); // [parens]

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(TOK_ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
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

  private Stmt varDeclaration(Token tok) {
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
    Token name = consume(TOK_IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(TOK_ASSIGN)) {
      initializer = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");
    defineVar(curFunc, name, varDefType);
    if (initializer != null && varDefType == JS_VAR_DEF_VAR) {
      Expr assign = new Expr.Assign(name, new Expr.Variable(name, curFunc.scopeLevel), TOK_ASSIGN, initializer);
      return new Stmt.Expression(assign);
    }
    return new Stmt.Var(varDefType, name, initializer);
  }


  private int defineVar(JSFunctionDef fd, Token name, JSVarDefEnum varDefType) {
    JSVarDef vd;
    JSHoistedDef hf;
    String varName = name.lexeme;
    int idx = -1;
    switch (varDefType) {
      case JS_VAR_DEF_LET:
      case JS_VAR_DEF_CONST:
      case JS_VAR_DEF_CATCH:
        vd = fd.findLexicalDef(varName);
        if (vd != null) {
          if (!vd.isGlobalVar) {
            if (vd.scope == fd.curScope) {
              error(name, "invalid redefinition of lexical identifier");
            }
          } else {
            if (fd.scopeLevel == 1) {
              error(name, "invalid redefinition of lexical identifier");
            }
          }
        }

        if (fd.findVarInChildScope(varName) != null) {
          error(name, "invalid redefinition of variable, find a scope caterpillar");
        }

        if (fd.isGlobalVar) {
          hf = fd.findHoistedDef(name);
          if (hf != null && fd.isChildScope(hf.scopeLevel, fd.scopeLevel)) {
            error(name, "invalid redefinition of global identifier");
          }
        }

        if (fd.isEval &&
          (fd.evalType == JSEvaluator.JS_EVAL_TYPE_GLOBAL ||
            fd.evalType == JSEvaluator.JS_EVAL_TYPE_MODULE)
          && fd.scopeLevel == 1) {
          hf = fd.addHoistedDef(-1, varName, -1, true);
          hf.isConst = varDefType == JS_VAR_DEF_CONST;
          idx = GLOBAL_VAR_OFFSET;
        } else {
          JSVarKindEnum varKind;
          if (varDefType == JS_VAR_DEF_FUNCTION_DECL)
            varKind = JS_VAR_FUNCTION_DECL;
          else if (varDefType == JS_VAR_DEF_NEW_FUNCTION_DECL)
            varKind = JS_VAR_NEW_FUNCTION_DECL;
          else
            varKind = JS_VAR_NORMAL;
          idx = fd.addScopeVar(varName, varKind);
          vd = fd.getVar(idx);
          if (vd != null) {
            vd.isLexical = true;
            vd.isConst = varDefType == JS_VAR_DEF_CONST;
          }
        }
        break;

      case JS_VAR_DEF_VAR:
        if (fd.isGlobalVar) {
          vd = fd.findLexicalDef(varName);
          if (vd != null) {
            invalid_lexical_redefinition:
            error(name, "invalid redefinition of lexical identifier");
          }
          if (fd.isGlobalVar) {
            hf = fd.findHoistedDef(varName);
            if (hf != null && hf.isLexical
              && hf.scopeLevel == fd.scopeLevel && fd.evalType == JSEvaluator.JS_EVAL_TYPE_MODULE) {
              error(name, "invalid redefinition of lexical identifier");
            }
            hf = fd.addHoistedDef(-1,  varName, -1, false);
            idx = GLOBAL_VAR_OFFSET;
          } else {
            idx = fd.findVar(varName);
            if (idx >= 0) {
              break;
            }
            idx = fd.addVar(varName);
            if (idx >= 0) {
              vd = fd.getVar(idx);
              vd.funcPoolOrScopeIdx = fd.scopeLevel;
            }
          }
        }
        break;
    }

    return idx;
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
    consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
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

    fd = ParserUtils.jsNewFunctionDef(ctx, fd, false, isExpr, fileName, name.line);
    fd.funcName = name.lexeme;
    curFunc = fd;

    consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
    List<Stmt> body = block();
    fd.body = body;
    curFunc = fd.parent;
    return fd;
  }

  private List<Stmt> block() {
    pushScope();
    List<Stmt> statements = new ArrayList<>();

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Expect '}' after block.");
    popScope();
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

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, (Expr.Variable) expr, operator.type, value);
      } else if (expr instanceof Expr.Get) {
        Expr.Get get = (Expr.Get) expr;
        return new Expr.Set(get.object, get.name, value);
      }

      error(operator, "Invalid assignment target."); // [no-throw]
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

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = multiplication();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr multiplication() {
    Expr expr = pow();

    while (match(SLASH, TOK_STAR, TOK_MOD)) {
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
    if (match(MINUS, PLUS, BANG, BITWISE_BANG,
      TOK_DEC, TOK_INC,
      TOK_VOID, TOK_TYPEOF, TOK_DELETE, TOK_AWAIT)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return postfix();
  }

  private Expr postfix() {
    Expr expr = call();
    if (match(TOK_DEC, TOK_INC)) {
      Token token = previous();
      return new Expr.Postfix(token, expr);
    }
    return expr;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Cannot have more than 255 arguments.");
        }
        arguments.add(expression());
      } while (match(COMMA));
    }

    Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

    return new Expr.Call(callee, paren, arguments);
  }

  private Expr call() {
    Expr expr = primary();

    while (true) { // [while-true]
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(DOT)) {
        Token name = consume(TOK_IDENTIFIER,
          "Expect property name after '.'.");
        expr = new Expr.Get(expr, name);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr primary() {
    if (match(TOK_FALSE)) return new Expr.Literal(false);
    if (match(TOK_TRUE)) return new Expr.Literal(true);
    if (match(TOK_NULL)) return new Expr.Literal(null);

    if (match(TOK_NUMBER, TOK_STRING, TOK_TEMPLATE)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(TOK_THIS)) return new Expr.This(previous());

    if (match(TOK_IDENTIFIER)) {
      return new Expr.Variable(previous(), curFunc.scopeLevel);
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

    if (match(LEFT_BRACKET)) {
      Expr.Literal literal = parseArrayLiteral();
      return literal;
    }

    throw error(peek(), "Expect expression.");
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
        case FOR:
        case TOK_IF:
        case WHILE:
        case PRINT:
        case TOK_RETURN:
          return;
      }

      advance();
    }
  }

  public int pushScope() {
    if (curFunc != null) {
      JSFunctionDef fd = curFunc;
      int scope = fd.addScope();
      return scope;
    }
    return 0;
  }

  public void popScope() {
    if (curFunc != null) {
      JSVarScope varScope = curFunc.curScope;
      curFunc.curScope = varScope.prev;
    }
  }
}
