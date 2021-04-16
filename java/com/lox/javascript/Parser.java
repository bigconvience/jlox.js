package com.lox.javascript;

import com.lox.clibrary.stdlib_h;

import java.util.*;

import static com.lox.javascript.FuncCallType.*;
import static com.lox.javascript.JSAtom.*;
import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSContext.*;
import static com.lox.javascript.JSErrorEnum.*;
import static com.lox.javascript.JSExportTypeEnum.*;
import static com.lox.javascript.JSFunctionDef.*;
import static com.lox.javascript.JSFunctionKindEnum.*;
import static com.lox.javascript.JSParseExportEnum.*;
import static com.lox.javascript.JSParseFunctionEnum.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JSVarDef.add_arg;
import static com.lox.javascript.JSVarDefEnum.*;
import static com.lox.javascript.LoxJS.*;
import static com.lox.javascript.Token.token_is_ident;
import static com.lox.javascript.Token.token_is_pseudo_keyword;
import static com.lox.javascript.TokenType.*;
import static java.lang.Boolean.*;

class Parser {


  public static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;
  private JSFunctionDef cur_func;
  private com.lox.javascript.Scanner scanner;
  JSContext ctx;
  String fileName;
  private JSRuntime rt;
  private boolean is_module;
  private String source;

  Parser(Scanner scanner, JSContext ctx, JSFunctionDef cur_func) {
    this.scanner = scanner;
    this.source = scanner.source;
    this.tokens = scanner.scanTokens();
    this.ctx = ctx;
    this.cur_func = cur_func;
    this.rt = rt;
  }

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements; // [parse-error-handling]
  }

  public static void js_parse_program(Parser s) {
    JSFunctionDef fd = s.cur_func;
    int idx;
    fd.is_global_var = (fd.eval_type == LoxJS.JS_EVAL_TYPE_GLOBAL);

    fd.is_global_var = (fd.eval_type == LoxJS.JS_EVAL_TYPE_GLOBAL) ||
      (fd.eval_type == LoxJS.JS_EVAL_TYPE_MODULE) ||
      (fd.js_mode & JS_MODE_STRICT) == 0;

    if (!s.is_module) {
      /* hidden variable for the return value */
      fd.eval_ret_idx = idx = JSVarDef.add_var(s.ctx, fd, JSAtomEnum.JS_ATOM__ret_.toJSAtom());
      if (idx < 0)
        stdlib_h.abort();
    }


    List<Stmt> stmts = s.parse();
    fd.body = stmts;
  }

  private Expr expression() {
    return assignment();
  }

  private Stmt declaration() {
    try {
      Parser s = this;
      if (match(TOK_FUNCTION) ||
        (token_is_pseudo_keyword(peek(), JS_ATOM_async) &&
          peek_token(s, true).type == TOK_FUNCTION)) {
        PJSFunctionDef pfd = new PJSFunctionDef();
        js_parse_function_decl(s, JS_PARSE_FUNC_STATEMENT,
          JS_FUNC_NORMAL, JS_ATOM_NULL,
          null, previous().start, peek().line_num, pfd);
        return pfd.fd;
      }
      if (match(TOK_CLASS)) return classDeclaration();
      if (match(TOK_VAR, TOK_LET, TOK_CONST)) return js_parse_var(previous());

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt classDeclaration() {
    Token name = consume(TOK_IDENT, "Expect class name.");
    consume(TOK_LEFT_BRACE, "Expect '{' before class body.");

    List<JSFunctionDef> methods = new ArrayList<>();
    while (!check(TOK_RIGHT_BRACE) && !isAtEnd()) {

    }

    consume(TOK_RIGHT_BRACE, "Expect '}' after class body.");

    return new Stmt.Class(name, methods);
  }

  private Stmt statement() {
    if (match(TOK_FOR)) return forStatement();
    if (match(TOK_IF)) return ifStatement();
    if (match(PRINT)) return printStatement();
    if (match(TOK_RETURN)) return returnStatement();
    if (match(TOK_THROW)) return throwStatement();
    if (match(WHILE)) return whileStatement();
    if (match(TOK_LEFT_BRACE)) {
      int start_line = previous().line_num;
      return new Stmt.Block(start_line, block());
    }

    return expressionStatement();
  }


  private Stmt forStatement() {
    consume(TOK_LEFT_PAREN, "Expect '(' after 'for'.");

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
    if (!check(TOK_RIGHT_PAREN)) {
      increment = expression();
    }
    consume(TOK_RIGHT_PAREN, "Expect ')' after for clauses.");
    Stmt body = statement();

    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(
        body,
        new Stmt.Expression(increment)));
    }

    if (condition == null) condition = new Expr.Literal(true, getPreviousLineNum());
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt ifStatement() {
    int expr_line = previous().line_num;
    Expr condition = js_parse_expr_paren();

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(TOK_ELSE)) {
      int else_line = previous().line_num;
      elseBranch = statement();
      elseBranch.line_number = else_line;
    }

    int end_line = previous().line_num;
    Stmt.If stmt = new Stmt.If(condition, thenBranch, elseBranch);
    stmt.line_number = expr_line;
    stmt.end_line = end_line;
    return stmt;
  }

  private Expr js_parse_expr_paren() {
    consume(TOK_LEFT_PAREN, "Expect '('");
    Expr expr = expression();
    consume(TOK_RIGHT_PAREN, "Expect ')'"); // [parens]
    return expr;
  }

  private Stmt printStatement() {
    Expr value = expression();
    Token tok = consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(tok.line_num, value);
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    int line_num = keyword.line_num;
    if (cur_func.is_eval) {
      js_parse_error(this, "return not in a function");
    }

    Expr value = null;
    if (!check(SEMICOLON) && !check(TOK_RIGHT_BRACE)) {
      value = expression();
    }

    consume(SEMICOLON, "Expect ';' after return value.");
    Stmt stmt = new Stmt.Return(keyword, value);
    stmt.line_number = line_num;
    return stmt;
  }

  private Stmt throwStatement() {
    Token keyword = previous();
    Expr value = expression();
    consume(SEMICOLON, "Expect ';' after throw expression.");
    Stmt stmt = new Stmt.Throw(keyword, value);
    return stmt;
  }

  private Stmt js_parse_var(Token tok) {
    JSFunctionDef fd = cur_func;

    List<Var> vars = new ArrayList<>();
    for (;;) {
      Token name = consume(TOK_IDENT, "Expect variable name.");

      JSVarDefEnum varDefEnum = getJsVarDefEnum(tok);

      Expr initializer = null;
      if (match(TOK_ASSIGN)) {
        initializer = expression();
      }
      TokenType type = tok.type;
      if (initializer == null) {
        if (type == TOK_CONST) {
          js_parse_error(this, "missing initializer for const variable");
        }
      }

      Stmt.Var stmt = new Stmt.Var(tok.line_num, varDefEnum, name.ident_atom, initializer);
      vars.add(stmt);
      if (!match(TOK_COMMA)) {
        break;
      }
    }
    match(SEMICOLON);
    VarDecl varDecl = new VarDecl(vars);
    return varDecl;
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
    consume(TOK_LEFT_PAREN, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(TOK_RIGHT_PAREN, "Expect ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    Token tok = consume(SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(previous().line_num, expr);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(TOK_RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TOK_RIGHT_BRACE, "Expect '}' after block.");
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
    return postfix(true);
  }

  private Expr postfix(boolean accept_lparen) {
    FuncCallType call_type;
    int optional_chaining_label;
    Parser s = this;

    call_type = FUNC_CALL_NORMAL;
    Expr expr = primary();
    if (match(TOK_DEC, TOK_INC)) {
      Token token = previous();
      return new Expr.Postfix(token, expr);
    }

    optional_chaining_label = -1;
    for (; ; ) {

      if (match(TOK_LEFT_PAREN) && accept_lparen) {
        int opcode, arg_count, drop_count;
        Expr.Call call = null;
        if (call_type == FUNC_CALL_NORMAL) {
          call = new Expr.Call(expr, null, null);
          call.call_type = call_type;
        }

        /* parse arguments */
        List<Expr> args = new ArrayList<>();
        arg_count = 0;
        while (!match(TOK_RIGHT_PAREN)) {
          if (arg_count >= 65535) {
            js_parse_error(s, "Too many call arguments");
          }
          if (match(TOK_ELLIPSIS))
            break;
          args.add(assignment());
          arg_count++;
          if (match(TOK_RIGHT_PAREN))
            break;
          /* accept a trailing comma before the ')' */
          if (js_parse_expect(s, TOK_COMMA))
            js_parse_error(s, "expect ','");
        }
        if (call != null) {
          call.arguments = args;
        }
        expr = call;
      } else if (match(TOK_DOT)) {
        if (match(TOK_PRIVATE_NAME)) {

        } else {
          Token token = advance();
          if (!token_is_ident(token.type)) {
            js_parse_error(this, "expecting field name");
          }
          expr = new Expr.Get(expr, token);
        }
      } else {
        break;
      }
    }
    return expr;
  }


  private Expr primary() {
    Object val;
    int line_num = getPreviousLineNum();
    if (match(TOK_FALSE)) return new Expr.Literal(false, getPreviousLineNum());
    if (match(TOK_TRUE)) return new Expr.Literal(true, getPreviousLineNum());
    if (match(TOK_NULL)) return new Expr.Literal(null, getPreviousLineNum());

    if (match(TOK_NUMBER)) {
      return new Expr.Literal(previous().literal, getPreviousLineNum());
    }
    if (match(TOK_TEMPLATE)) {
      return new Expr.Literal(previous().literal, getPreviousLineNum());
    }
    if (match(TOK_STRING)) {
      return emit_push_const(previous(), true, getPreviousLineNum());
    }

    if (match(TOK_THIS)) return new Expr.This(previous());

    if (match(TOK_IDENT)) {
      return new Expr.Variable(previous(), cur_func.scope_level, getPreviousLineNum());
    }

    if (match(TOK_LEFT_PAREN)) {
      Expr expr = expression();
      consume(TOK_RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    if (match(TOK_LEFT_BRACE)) {
      Expr.ObjectLiteral literal = parseObjectLiteral();
      return literal;
    }

    if (match(TOK_LEFT_BRACKET)) {
      Expr.Literal literal = parseArrayLiteral();
      return literal;
    }

    throw error(peek(), "Expect expression.");
  }

  private Expr.Literal emit_push_const(Token token, boolean asAtom, int line_num) {
    Expr.Literal literal = null;
    if (token.type == TOK_STRING && asAtom) {
      JSAtom atom = token.str_str;
      if (atom != JS_ATOM_NULL && !atom.__JS_AtomIsTaggedInt()) {
        literal = new Expr.Literal(atom, line_num);
      }
    }

    return literal;
  }

  private Expr.ObjectLiteral parseObjectLiteral() {
    Map<String, Expr> prop = new HashMap<>();
    Expr.ObjectLiteral literal = new Expr.ObjectLiteral(prop);
    while (!match(TOK_RIGHT_BRACE)) {
      String name = parsePropertyName();
      match(TOK_COLON);
      Expr expr = assignment();
      prop.put(name, expr);
      match(TOK_COMMA);
    }

    return literal;
  }

  private String parsePropertyName() {
    if (match(TOK_IDENT, TOK_STRING)) {
      return previous().lexeme;
    } else if (match(TOK_NUMBER)) {
      return previous().lexeme;
    }
    return null;
  }

  private Expr.Literal parseArrayLiteral() {
    return new Expr.Literal(null, getPreviousLineNum());
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

  Token consume(TokenType type, String message) {
    if (check(type)) return advance();
    js_parse_error(this, message);
    return null;
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

  public Token previous() {
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
    if (cur_func != null) {
      JSFunctionDef fd = cur_func;
      int scope = fd.add_scope();
      return scope;
    }
    return 0;
  }

  static int js_parse_error_reserved_identifier(Parser s) {
    return js_parse_error(s, "'%s' is a reserved identifier",
      JSContext.JS_AtomToString(s.ctx, s.peek().ident_atom));
  }

  static int js_parse_error(Parser s, String fmt, Object... args) {
    JSThrower.JS_ThrowError2(s.ctx, JS_SYNTAX_ERROR, fmt, Arrays.asList(args), false);
    return -1;
  }


  static int js_parse_function_decl(Parser s,
                                    JSParseFunctionEnum func_type,
                                    JSFunctionKindEnum func_kind,
                                    JSAtom func_name,
                                    byte[] ptr,
                                    int ptr_idx,
                                    int function_line_num, PJSFunctionDef pfd) {
    return js_parse_function_decl2(s, func_type, func_kind, func_name, ptr,
      ptr_idx,
      function_line_num, JS_PARSE_EXPORT_NONE,
      pfd);
  }

  static int js_parse_function_decl2(Parser s,
                                     JSParseFunctionEnum func_type,
                                     JSFunctionKindEnum func_kind,
                                     JSAtom func_name,
                                     byte[] ptr,
                                     int ptr_idx,
                                     int function_line_num,
                                     JSParseExportEnum export_flag,
                                     PJSFunctionDef pfd) {
    JSContext ctx = s.ctx;
    JSFunctionDef fd = s.cur_func;
    boolean is_expr;
    int func_idx, lexical_func_idx = -1;
    boolean has_opt_arg;
    boolean create_func_var = FALSE;
    Token token;

    is_expr = (func_type != JS_PARSE_FUNC_STATEMENT &&
      func_type != JS_PARSE_FUNC_VAR);
    if (func_type == JS_PARSE_FUNC_STATEMENT ||
      func_type == JS_PARSE_FUNC_VAR ||
      func_type == JS_PARSE_FUNC_EXPR) {
      if (func_kind == JS_FUNC_NORMAL &&
        Token.token_is_pseudo_keyword(s.peek(), JS_ATOM_async) &&
        peek_token(s, TRUE) != null) {
        s.advance();
        func_kind = JS_FUNC_ASYNC;
      }

      if (s.match(TOK_SAR)) {
        func_kind = JSFunctionKindEnum.values()[func_kind.ordinal() | JS_FUNC_GENERATOR.ordinal()];
      }

      if (s.check(TOK_IDENT)) {
        if (s.peek().is_reserved ||
          (s.peek().ident_atom == JS_ATOM_yield.toJSAtom() &&
            func_type == JS_PARSE_FUNC_EXPR &&
            (func_kind.ordinal() & JS_FUNC_GENERATOR.ordinal()) != 0) ||
          (s.peek().ident_atom == JS_ATOM_await.toJSAtom() &&
            func_type == JS_PARSE_FUNC_EXPR &&
            (func_kind.ordinal() & JS_FUNC_ASYNC.ordinal()) != 0)) {
          return js_parse_error_reserved_identifier(s);
        }
      }

      if (s.check(TOK_IDENT) ||
        (((s.check(TOK_YIELD) && (fd.js_mode & JS_MODE_STRICT) == 0) ||
          (s.check(TOK_AWAIT) && !s.is_module)) &&
          func_type == JS_PARSE_FUNC_EXPR)) {
        func_name = s.advance().ident_atom;
      } else {
        if (func_type != JS_PARSE_FUNC_EXPR &&
          export_flag != JS_PARSE_EXPORT_DEFAULT) {
          return js_parse_error(s, "function name expected");
        }
      }
    } else if (func_type != JS_PARSE_FUNC_ARROW) {
      func_name = func_name;
    }

    fd = js_new_function_def(ctx, fd, false, is_expr, s.fileName, function_line_num);

    fd.decl_line_number = s.previous().line_num;
    if (pfd != null) {
      pfd.fd = fd;
    }

    s.cur_func = fd;
    fd.func_name = func_name;
    fd.has_prototype = (func_type == JS_PARSE_FUNC_STATEMENT ||
      func_type == JS_PARSE_FUNC_VAR ||
      func_type == JS_PARSE_FUNC_EXPR) &&
      func_kind == JS_FUNC_NORMAL;
    fd.has_home_object = (func_type == JS_PARSE_FUNC_METHOD ||
      func_type == JS_PARSE_FUNC_GETTER ||
      func_type == JS_PARSE_FUNC_SETTER ||
      func_type == JS_PARSE_FUNC_CLASS_CONSTRUCTOR ||
      func_type == JS_PARSE_FUNC_DERIVED_CLASS_CONSTRUCTOR);

    fd.has_arguments_binding = (func_type != JS_PARSE_FUNC_ARROW);
    fd.has_this_binding = fd.has_arguments_binding;
    fd.is_derived_class_constructor = (func_type == JS_PARSE_FUNC_DERIVED_CLASS_CONSTRUCTOR);
    if (func_type == JS_PARSE_FUNC_ARROW) {
      fd.new_target_allowed = fd.parent.new_target_allowed;
      fd.super_call_allowed = fd.parent.super_call_allowed;
      fd.super_allowed = fd.parent.super_allowed;
      fd.arguments_allowed = fd.parent.arguments_allowed;
    } else {
      fd.new_target_allowed = TRUE;
      fd.super_call_allowed = fd.is_derived_class_constructor;
      fd.super_allowed = fd.has_home_object;
      fd.arguments_allowed = TRUE;
    }

    fd.func_kind = func_kind;
    fd.func_type = func_type;

    if (func_type == JS_PARSE_FUNC_CLASS_CONSTRUCTOR ||
      func_type == JS_PARSE_FUNC_DERIVED_CLASS_CONSTRUCTOR) {
      /* error if not invoked as a constructor */

    }

    if (func_type == JS_PARSE_FUNC_CLASS_CONSTRUCTOR) {

    }

    fd.has_simple_parameter_list = TRUE;
    has_opt_arg = FALSE;
    if (func_type == JS_PARSE_FUNC_ARROW && s.match(TOK_IDENT)) {
      JSAtom name;
      if (s.peek().is_reserved) {

      }
    } else {
      if (js_parse_expect(s, TOK_LEFT_PAREN)) {
        return on_faile(s, fd, pfd);
      }
      while (!s.match(TOK_RIGHT_PAREN)) {
        JSAtom name;
        boolean rest = false;
        int idx;

        if (s.check(TOK_ELLIPSIS)) {
          fd.has_simple_parameter_list = false;
          rest = true;
          if (s.isAtEnd()) {
            return on_faile(s, fd, pfd);
          }
        }

        if (s.match(TOK_LEFT_BRACKET, TOK_LEFT_BRACE)) {

        } else if (s.check(TOK_IDENT)) {
          if (s.peek().is_reserved) {
            js_parse_error_reserved_identifier(s);
            return on_faile(s, fd, pfd);
          }
          name = s.peek().ident_atom;
          if (name.getVal() == JS_ATOM_yield.ordinal() && fd.func_kind == JS_FUNC_GENERATOR) {
            js_parse_error_reserved_identifier(s);
            return on_faile(s, fd, pfd);
          }
          idx = add_arg(ctx, fd, name);
          if (idx < 0) {
            return on_faile(s, fd, pfd);
          }
          if (s.advance() == null) {
            return on_faile(s, fd, pfd);
          }
          if (rest) {

          } else if (s.check(TOK_ASSIGN)) {

          } else if (!has_opt_arg) {
            fd.defined_arg_count++;
          }
        } else {
          js_parse_error(s, "missing formal parameter");
          return on_faile(s, fd, pfd);
        }

        if (rest && !s.match(TOK_RIGHT_PAREN)) {
          js_parse_expect(s, TOK_RIGHT_PAREN);
          return on_faile(s, fd, pfd);
        }
        if (s.match(TOK_RIGHT_PAREN)) {
          break;
        }
        if (js_parse_expect(s, TOK_COMMA)) {
          return on_faile(s, fd, pfd);
        }
      }

      if ((func_type == JS_PARSE_FUNC_GETTER && fd.args.size() != 0) ||
        (func_type == JS_PARSE_FUNC_SETTER && fd.args.size() != 1)) {
        js_parse_error(s, "invalid number of arguments for getter or setter");
        return on_faile(s, fd, pfd);
      }
    }

    if (s.isAtEnd()) {
      return on_faile(s, fd, pfd);
    }

    fd.in_function_body = true;

    if (s.check(TOK_ARROW)) {

    }

    if (js_parse_expect(s, TOK_LEFT_BRACE)) {
      return on_faile(s, fd, pfd);
    }

    if (js_parse_directives(s))
      return on_faile(s, fd, pfd);

    /* in strict_mode, check function and argument names */
    if (js_parse_function_check_names(s, fd, func_name))
      return on_faile(s, fd, pfd);

    fd.body = s.block();
    if (fd.body == null) {
      return on_faile(s, fd, pfd);
    }
    int end_idx = s.previous().end;
    fd.leave_line_number = s.previous().line_num;

    if ((fd.js_mode & JS_MODE_STRIP) == 0) {
      /* save the function source code */
      fd.source_len = end_idx - ptr_idx;
      fd.source = s.source.substring(ptr_idx, end_idx);
      if (fd.source == null)
        return on_faile(s, fd, pfd);
    }


    return on_done(s, fd, pfd, is_expr, export_flag);
  }

  static int on_done(Parser s, JSFunctionDef fd, PJSFunctionDef pfd, boolean is_expr, JSParseExportEnum export_flag) {
    s.cur_func = fd.parent;
    /* create the function object */
    {
      int idx;
      JSAtom func_name = fd.func_name;
      JSParseFunctionEnum func_type = fd.func_type;

      /* the real object will be set at the end of the compilation */
      idx = cpool_add(s, JS_NULL);
      fd.parent_cpool_idx = idx;
      if (is_expr) {

      } else if (func_type == JS_PARSE_FUNC_VAR) {

      } else {
        if (!s.cur_func.is_global_var) {

        } else {
          JSAtom func_var_name;
          if (func_name == JS_ATOM_NULL)
            func_var_name = JS_ATOM__default_.toJSAtom(); /* export default */
          else
            func_var_name = func_name;

          if (!add_hoisted_def(s.ctx, s.cur_func, idx, func_var_name, -1, FALSE))
            return on_faile(s, fd, pfd);
          if (export_flag != JS_PARSE_EXPORT_NONE) {
            if (add_export_entry(s, s.cur_func.module, func_var_name,
              export_flag == JS_PARSE_EXPORT_NAMED ? func_var_name : JS_ATOM_default.toJSAtom(), JS_EXPORT_TYPE_LOCAL) == null)
              return on_faile(s, fd, pfd);
          }
        }
      }
    }
    return 0;
  }

  static int on_faile(Parser s, JSFunctionDef fd, PJSFunctionDef pfd) {
    s.cur_func = fd.parent;
    if (pfd != null) {
      pfd = null;
    }
    return -1;
  }

  static Token peek_token(Parser s, boolean no_line_terminator) {
    return s.advance();
  }

  static boolean js_parse_expect(Parser s, TokenType tok) {
    return s.consume(tok, "") == null;
  }

  static boolean js_parse_function_check_names(Parser s, JSFunctionDef fd,
                                               JSAtom func_name) {

    return false;
  }

  static boolean js_parse_directives(Parser s) {
    return false;
  }


  /* return the constant pool index. 'val' is not duplicated. */
  static int cpool_add(Parser s, JSValue val) {
    JSFunctionDef fd = s.cur_func;
    fd.cpool.add(val);
    return fd.cpool.size() - 1;
  }

  static JSExportEntry add_export_entry(Parser s, JSModuleDef m,
                                        JSAtom local_name, JSAtom export_name,
                                        JSExportTypeEnum export_type) {
    return add_export_entry2(s.ctx, s, m, local_name, export_name,
      export_type);
  }

  static JSExportEntry add_export_entry2(JSContext ctx,
                                         Parser s, JSModuleDef m,
                                         JSAtom local_name, JSAtom export_name,
                                         JSExportTypeEnum export_type) {
    return null;
  }

  int getPreviousLineNum() {
    return previous().line_num;
  }
  int getCurrentLinenum() {
    return peek().line_num;
  }
}
