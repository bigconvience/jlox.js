package com.craftinginterpreters.lox;

import java.util.*;

import static com.craftinginterpreters.lox.JSAtomEnum.JS_ATOM__default_;
import static com.craftinginterpreters.lox.JConstants.DEFINE_GLOBAL_FUNC_VAR;
import static com.craftinginterpreters.lox.JConstants.DEFINE_GLOBAL_LEX_VAR;
import static com.craftinginterpreters.lox.LoxJS.JS_EVAL_TYPE_GLOBAL;
import static com.craftinginterpreters.lox.JSProperty.JS_PROP_CONFIGURABLE;
import static com.craftinginterpreters.lox.JSProperty.JS_PROP_WRITABLE;
import static com.craftinginterpreters.lox.OPCodeEnum.*;
import static com.craftinginterpreters.lox.Parser.ARGUMENT_VAR_OFFSET;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;
  private JSFunctionDef curFunc;
  private final JSContext ctx;
  private final List<Stmt> stmtOut;
  private final JSRuntime rt;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
    ctx = interpreter.jsContext;
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
    JSFunctionDef fd = curFunc;
    enterScope(fd, stmt.scope, fd.byteCode);
    curFunc = fd;
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
    curFunc = stmt;
    resoleVariables(stmt);
    return null;
  }

  private boolean resoleVariables(JSFunctionDef s) {
    int pos, pos_next, bc_len, op, len, i, idx, arg_valid, line_num;
    CodeContext cc = new CodeContext();

    boolean ret = false;
    DynBuf bcOut = new DynBuf();
    s.byteCode = bcOut;
    if (s.isGlobalVar) {
      for (JSHoistedDef hd : s.hoistedDef) {
        int flags;
        if (hd.varName != null) {
          //todo benpeng closure

        }
        bcOut.putOpcode(OP_check_define_var);
        bcOut.putAtom(hd.varName);
        flags = 0;
        if (hd.isLexical) {
          flags |= DEFINE_GLOBAL_LEX_VAR;
        }
        if (hd.cpool_idx >= 0) {
          flags |= DEFINE_GLOBAL_FUNC_VAR;
        }
        bcOut.putc(flags);
      }
      next:
      ;
    }

    s.bodyBlock.accept(this);
    return ret;
  }

  void instantiateHostedDef(JSFunctionDef s, DynBuf bc) {
    int i, idx, var_idx;
    for (i = 0; i < s.hoistedDef.size(); i++) {
      JSHoistedDef hf = s.hoistedDef.get(i);
      int has_closure = 0;
      boolean force_init = hf.forceInit;
      if (s.isGlobalVar && hf.varName != JSAtom.JS_ATOM_NULL) {
        for (idx = 0; idx < s.closureVar.size(); idx++) {
          JSClosureVar cv = s.closureVar.get(idx);
          if (hf.varName.equals(cv.var_name)) {
            has_closure = 2;
            force_init = false;
            break;
          }
        }
        if (has_closure == 0) {
          int flags = 0;
          if (s.evalType == JS_EVAL_TYPE_GLOBAL) {
            flags |= JS_PROP_CONFIGURABLE;
          }

          if (hf.cpool_idx >= 0 && !hf.isLexical) {
            bc.putOpcode(OP_fclosure);
            bc.putU32(hf.cpool_idx);
            bc.putOpcode(OP_define_func);
            bc.putAtom(hf.varName);
            bc.putc(flags);
            continue;
          } else {
            if (hf.isLexical) {
              flags |= DEFINE_GLOBAL_LEX_VAR;
              if (!hf.isConst) {
                flags |= JS_PROP_WRITABLE;
              }
              bc.putOpcode(OP_define_var);
              bc.putAtom(hf.varName);
              bc.putc(flags);
            }
          }
        }

        if (hf.cpool_idx >= 0 || force_init) {
          if (hf.cpool_idx >= 0) {
            bc.putOpcode(OP_fclosure);
            bc.putU32(hf.cpool_idx);
            if (hf.varName.getVal() == JS_ATOM__default_.ordinal()) {
              /* set default export function name */
              bc.putOpcode(OP_set_name);
              bc.putAtom(hf.varName);
            }
          } else {
            bc.putOpcode(OP_undefined);
          }
          if (s.isGlobalVar) {
            if (has_closure == 2) {
              bc.putOpcode(OP_put_var_ref);
              bc.putU16(idx);
            } else if (has_closure == 1) {
              bc.putOpcode(OP_define_field);
              bc.putAtom(hf.varName);
              bc.putOpcode(OP_drop);
            } else {
              /* XXX: Check if variable is writable and enumerable */
              bc.putOpcode(OP_put_var);
              bc.putAtom(hf.varName);
            }
          } else {
            var_idx = hf.varIdx;
            if ((var_idx & ARGUMENT_VAR_OFFSET) != 0) {
              bc.putOpcode(OP_put_arg);
              bc.putU16(var_idx - ARGUMENT_VAR_OFFSET);
            } else {
              bc.putOpcode(OP_put_loc);
              bc.putU16(var_idx);
            }
          }
        }
      }
    }
    s.hoistedDef.clear();
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
    resolve(stmt.expression);
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
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);
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
    if (curFunc != null) {
      Token varName = expr.name;
      JSVarDef varDef = curFunc.getVarDef(varName.lexeme);
      if (varDef != null && varDef.isConst) {
        Lox.error(expr.name, "is read-only");
        return null;
      }
    }
    resolve(expr.value);
    resolveLocal(expr, expr.name);
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
    if (expr.value instanceof JSAtom) {
      DynBuf db = curFunc.byteCode;
      db.putOpcode(OP_push_atom_value);
      db.putAtom((JSAtom) expr.value);
    }
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
    String varName = expr.name.lexeme;
    int scope = expr.scopeLevel;
    resolveScopeVar(varName, scope);
    return null;
  }

  private int resolveScopeVar(String varName, int scopeLevel) {
    int var_idx = -1;
    JSFunctionDef fd = curFunc;
    JSVarDef vd;
    for (int idx = fd.scopes.get(scopeLevel).first; idx >= 0; ) {
      vd = fd.getVar(idx);
      if (vd.varName.equals(varName)) {

        var_idx = idx;
        break;
      } else {

      }
      idx = vd.scopeNext;
    }

    return var_idx;
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
    JSFunctionDef enclosureFunc = curFunc;
    curFunc = function;

    beginScope();
    for (Token param : function.params) {
      declare(param);
      define(param);
    }
    instantiateHostedDef(function, function.byteCode);
    resolve(function.body);
    enterScope(function, 1, null);
    endScope();
    currentFunction = enclosingFunction;
    curFunc = enclosureFunc;
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
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }

    // Not found. Assume it is global.
  }

  private void enterScope(JSFunctionDef s, int scope, DynBuf bcOut) {
    if (scope == 1) {
      instantiateHostedDef(s, bcOut);
    }

    for (int scopeIdx = s.scopes.get(scope).first; scopeIdx >= 0; ) {
      JSVarDef vd = s.vars.get(scopeIdx);
      if (vd.scopeLevel == scopeIdx) {
        if (ParserUtils.isFuncDecl(vd.varKind)) {
          bcOut.putOpcode(OP_fclosure);
          bcOut.putU32(vd.funcPoolOrScopeIdx);
          bcOut.putOpcode(OP_put_loc);
        } else {
          bcOut.putOpcode(OP_set_loc_uninitialized);
        }
        bcOut.putU16((short) scopeIdx);
        scopeIdx = vd.scopeNext;
      } else {
        break;
      }
    }
  }
}
