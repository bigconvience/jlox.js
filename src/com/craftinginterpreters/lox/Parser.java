package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
  private static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements; // [parse-error-handling]
  }

  private Expr expression() {
    return assignment();
  }

  private Stmt declaration() {
    try {
      if (match(TOK_CLASS)) return classDeclaration();
      if (match(TOK_FUNCTION)) return function("function");
      if (match(TOK_VAR)) return varDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt classDeclaration() {
    Token name = consume(TOK_IDENTIFIER, "Expect class name.");
    consume(LEFT_BRACE, "Expect '{' before class body.");

    List<Stmt.Function> methods = new ArrayList<>();
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
      initializer = varDeclaration();
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

  private Stmt varDeclaration() {
    Token name = consume(TOK_IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(TOK_ASSIGN)) {
      initializer = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
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

  private Stmt.Function function(String kind) {
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

    consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
    List<Stmt> body = block();
    return new Stmt.Function(name, parameters, body);
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

    if (match(TOK_NUMBER, TOK_STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(TOK_THIS)) return new Expr.This(previous());

    if (match(TOK_IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
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
}
