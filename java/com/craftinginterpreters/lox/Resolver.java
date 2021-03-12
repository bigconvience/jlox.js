package com.craftinginterpreters.lox;

import java.util.*;

import static com.craftinginterpreters.lox.OPCodeEnum.*;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;
  private JSFunctionDef cur_func;
  private final JSContext ctx;
  private final List<Stmt> stmtOut;
  private final JSRuntime rt;

  Resolver(JSContext jsContext, JSFunctionDef fd) {
    ctx = jsContext;
    cur_func = fd;
    rt = ctx.rt;
    stmtOut = new ArrayList<>();
  }

  private enum FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD
  }

  private enum ClassType {
    NONE,
    CLASS
  }

  private ClassType currentClass = ClassType.NONE;

  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    JSFunctionDef fd = cur_func;
    enter_scope(fd, stmt.scope, fd.byte_code);
    cur_func = fd;
    resolve(stmt.statements);
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;

    declare(stmt.name);
    define(stmt.name);

    beginScope();
    scopes.peek().put("this", true);

    for (JSFunctionDef method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("init")) {
        declaration = FunctionType.INITIALIZER;
      }

      resolveFunction(method, declaration); // [local]
    }

    endScope();

    currentClass = enclosingClass;
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(JSFunctionDef stmt) {
    ctx.resolve_variables(stmt);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) resolve(stmt.elseBranch);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Expr expr = stmt.expression;
    expr.accept(this);

    DynBuf bc = cur_func.byte_code;
    bc.putOpcode(OP_print);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE) {
      Lox.error(stmt.keyword, "Cannot return from top-level code.");
    }

    if (stmt.value != null) {
      if (currentFunction == FunctionType.INITIALIZER) {
        Lox.error(stmt.keyword,
          "Cannot return a value from an initializer.");
      }

      resolve(stmt.value);
    }

    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    if (stmt.tok == TokenType.TOK_LET) {
      JSFunctionDef s = cur_func;
      DynBuf bc = cur_func.byte_code;
      bc.putOpcode(OP_undefined);
      OPCodeEnum opCode = OP_scope_put_var_init;
      ctx.resolve_scope_var(s, stmt.name, stmt.scope,
        opCode.ordinal(), bc,
        s.byte_code.buf, 0, true,
        PutLValueEnum.PUT_LVALUE_NOKEEP);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    JSFunctionDef s = cur_func;
    Expr value = expr.value;
    if (value != null) {
      value.accept(this);
    }

    Expr.Variable left = expr.left;
    JSAtom var_name = left.name.ident_atom;
    int scope = left.scope_level;
    OPCodeEnum opCode = null;
    TokenType tok = left.tok;
    if (tok == TokenType.TOK_VAR || tok == null) {
      opCode = OP_scope_make_ref;
    } else if (tok == TokenType.TOK_LET || tok == TokenType.TOK_CONST) {
      opCode = OP_scope_put_var_init;
    } else {
      opCode = OP_scope_put_var;
    }
    ctx.resolve_scope_var(s, var_name, scope,
      opCode.ordinal(), s.byte_code,
      s.byte_code.buf, 0, true,
      expr.putLValueEnum);

    return null;
  }

  @Override
  public Void visitConditionExpr(Expr.Condition expr) {
    resolve(expr.first);
    resolve(expr.middle);
    resolve(expr.last);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee);

    for (Expr argument : expr.arguments) {
      resolve(argument);
    }

    return null;
  }

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    DynBuf db = cur_func.byte_code;
    if (expr.value instanceof JSAtom) {
      db.putOpcode(OP_push_atom_value);
    }
    db.putValue(expr.value);
    return null;
  }

  @Override
  public Void visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitBitwiseExpr(Expr.Bitwise expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitThisExpr(Expr.This expr) {
    if (currentClass == ClassType.NONE) {
      Lox.error(expr.keyword,
        "Cannot use 'this' outside of a class.");
      return null;
    }

    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitPostfixExpr(Expr.Postfix expr) {
    resolve(expr.left);
    return null;
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    JSAtom name = expr.name.ident_atom;
    int scope = expr.scope_level;
    DynBuf bc = cur_func.byte_code;
    ctx.resolve_scope_var(cur_func, name, scope,
      OP_scope_get_var.ordinal(), bc, bc.buf, 0, true);
    return null;
  }


  @Override
  public Void visitCoalesceExpr(Expr.Coalesce expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void resolveFunction(
    JSFunctionDef function, FunctionType type) {
    FunctionType enclosingFunction = currentFunction;
    currentFunction = type;
    JSFunctionDef enclosureFunc = cur_func;
    cur_func = function;

    beginScope();
    for (Token param : function.params) {
      declare(param);
      define(param);
    }
    function.instantiate_hoisted_definitions(function.byte_code);
    resolve(function.body);
    enter_scope(function, 1, null);
    endScope();
    currentFunction = enclosingFunction;
    cur_func = enclosureFunc;
  }

  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) return;

    Map<String, Boolean> scope = scopes.peek();
    if (scope.containsKey(name.lexeme)) {
      Lox.error(name,
        "Variable with this name already declared in this scope.");
    }

    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        return;
      }
    }

    // Not found. Assume it is global.
  }

  private static void enter_scope(JSFunctionDef s, int scope, DynBuf bcOut) {
    if (scope == 1) {
      s.instantiate_hoisted_definitions(bcOut);
    }

    for (int scopeIdx = s.scopes.get(scope).first; scopeIdx >= 0; ) {
      JSVarDef vd = s.vars.get(scopeIdx);
      if (vd.scope_level == scopeIdx) {
        if (JSFunctionDef.isFuncDecl(vd.varKind)) {
          bcOut.putOpcode(OP_fclosure);
          bcOut.putU32(vd.funcPoolOrScopeIdx);
          bcOut.putOpcode(OP_put_loc);
        } else {
          bcOut.putOpcode(OP_set_loc_uninitialized);
        }
        bcOut.putU16((short) scopeIdx);
        scopeIdx = vd.scope_next;
      } else {
        break;
      }
    }
  }
}
