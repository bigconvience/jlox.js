package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.JSAtomEnum.JS_ATOM__default_;
import static com.craftinginterpreters.lox.JSContext.DEFINE_GLOBAL_LEX_VAR;
import static com.craftinginterpreters.lox.JS_PROP.JS_PROP_CONFIGURABLE;
import static com.craftinginterpreters.lox.JS_PROP.JS_PROP_WRITABLE;
import static com.craftinginterpreters.lox.LoxJS.JS_EVAL_TYPE_GLOBAL;
import static com.craftinginterpreters.lox.OPCodeEnum.*;
import static com.craftinginterpreters.lox.Parser.ARGUMENT_VAR_OFFSET;

/**
 * @author benpeng.jiang
 * @title: FunctionDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/228:17 PM
 */ //< stmt-expression
//> stmt-function
public class JSFunctionDef extends Stmt {
  JSContext ctx;
  JSFunctionDef parent;
  int parent_cpool_idx;

  int parent_scope_level;
  final List<JSFunctionDef> child_list;

  final List<JSValue> cpool;


  JSFunctionDef(JSFunctionDef parent,
                boolean isEval, boolean isFuncExpr, String filename, int lineNum) {
    this.parent = parent;
    this.isEval = isEval;

    params = new ArrayList<>();
    varDefMap = new HashMap<>();
    hoistDef = new HashMap<>();
    scopes = new ArrayList<>();
    vars = new ArrayList<>();
    hoistedDef = new ArrayList<>();
    args = new ArrayList<>();
    closureVar = new ArrayList<>();
    child_list = new ArrayList<>();
    cpool = new ArrayList<>();
  }

  @Override
  <R> R accept(Visitor<R> visitor) {
    return visitor.visitFunctionStmt(this);
  }


  final Token name = null;
  final List<Token> params;

  final List<JSVarScope> scopes;
  int scope_level;
  int scopeFirst;

  final Map<String, JSVarDef> varDefMap;
  final List<JSVarDef> vars;
  final List<JSVarDef> args;
  final List<JSHoistedDef> hoistedDef;
  final List<JSClosureVar> closureVar;
  final Map<String, JSHoistedDef> hoistDef;
  List<Stmt> body;
  int evalType;
  boolean isEval;
  boolean isGlobalVar;
  JSVarScope curScope;
  DynBuf byte_code;

  JSAtom func_name;

  void addStmt(Stmt stmt) {
    body.add(stmt);
  }

  void addVarDef(String name, JSVarDef varDef) {
    varDefMap.put(name, varDef);
  }

  JSVarDef getVarDef(String name) {
    return varDefMap.get(name);
  }


  JSVarDef getArgDef(String name) {
    return varDefMap.get(name);
  }

  JSHoistedDef findHoistedDef(Token name) {
    return hoistDef.get(name.lexeme);
  }

  JSHoistedDef addHoistedDef(Token name) {
    JSHoistedDef hoistedDef = new JSHoistedDef();
    hoistedDef.name = name;
    hoistDef.put(name.lexeme, hoistedDef);
    return hoistedDef;
  }

  int getScopeCount() {
    return scopes.size();
  }

  int addScope() {
    int scope = getScopeCount();
    JSVarScope varScope = new JSVarScope();
    scopes.add(varScope);
    scope_level = scope;
    return scope;
  }

  public JSVarDef findLexicalDef(JSAtom varName) {
    JSVarScope scope = curScope;
    while (scope != null) {
      JSVarDef varDef = scope.get(varName);
      if (varDef != null && varDef.isLexical) {
        return varDef;
      }
      scope = scope.prev;
    }

    if (isEval && evalType == LoxJS.JS_EVAL_TYPE_GLOBAL) {
      return findLexicalHoistedDef(varName);
    }
    return null;
  }

  public JSHoistedDef findLexicalHoistedDef(JSAtom varName) {
    JSHoistedDef hoistedDef = findHoistedDef(varName);
    if (hoistedDef != null && hoistedDef.isLexical) {
      return hoistedDef;
    }
    return null;
  }

  JSHoistedDef findHoistedDef(JSAtom varName) {
    for (JSHoistedDef hf : hoistedDef) {
      if (hf.varName.equals(varName)) {
        return hf;
      }
    }
    return null;
  }

  public JSVarDef findVarInChildScope(JSAtom name) {
    for (JSVarDef vd : vars) {
      if (vd != null && vd.varName.equals(name) && vd.scope_level == 0) {
        if (isChildScope(vd.funcPoolOrScopeIdx, scope_level)) {
          return vd;
        }
        return vd;
      }
    }

    return null;
  }

  public boolean isChildScope(int scope, int parentScope) {
    while (scope > 0) {
      if (scope == parentScope) {
        return true;
      }
      scope = scopes.get(scope).parent;
    }
    return false;
  }

  public JSHoistedDef addHoistedDef(int cpoolIdx, JSAtom varName,
                                    int varIdx,
                                    boolean isLexical) {
    JSHoistedDef hf = new JSHoistedDef();
    hoistedDef.add(hf);
    hf.varName = varName;
    hf.cpool_idx = cpoolIdx;
    hf.isLexical = isLexical;
    hf.forceInit = false;
    hf.varIdx = varIdx;
    hf.scope_level = scope_level;
    return hf;
  }

  public int addScopeVar(JSAtom varName, JSVarKindEnum varKind) {
    int idx = addVar(varName);
    if (idx >= 0) {
      JSVarDef vd = vars.get(idx);
      vd.varKind = varKind;
      vd.scope_level = scope_level;
      vd.scope_next = scopeFirst;
      curScope.first = idx;
      scopeFirst = idx;
    }

    return idx;
  }

  public int addVar(JSAtom varName) {
    JSVarDef vd = new JSVarDef();
    vars.add(vd);
    vd.varName = varName;
    return vars.size() - 1;
  }

  public int findVar(JSAtom varName) {
    for (int i = 0; i < vars.size(); i++) {
      JSVarDef vd = vars.get(i);
      if (vd.varName.equals(varName) && vd.scope_level == 0) {
        return i;
      }
    }

    return findArg(varName);
  }

  public int findArg(JSAtom varName) {
    for (int i = 0; i < args.size(); i++) {
      JSVarDef vd = args.get(i);
      if (vd.varName.equals(varName)) {
        return i | Parser.ARGUMENT_VAR_OFFSET;
      }
    }
    return -1;
  }

  void enter_scope(int scope, DynBuf bcOut) {
    JSFunctionDef s = this;
    if (scope == 1) {
      instantiate_hoisted_definitions(bcOut);
    }

    for (int scopeIdx = s.scopes.get(scope).first; scopeIdx >= 0; ) {
      JSVarDef vd = s.vars.get(scopeIdx);
      if (vd.scope_level == scopeIdx) {
        if (isFuncDecl(vd.varKind)) {
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


  void instantiate_hoisted_definitions(DynBuf bc) {
    JSFunctionDef s = this;
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
            }
            bc.putOpcode(OP_define_var);
            bc.putAtom(hf.varName);
            bc.putc(flags);
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

  public static boolean isFuncDecl(JSVarKindEnum varKind) {
    return varKind == JSVarKindEnum.JS_VAR_FUNCTION_DECL ||
      varKind == JSVarKindEnum.JS_VAR_NEW_FUNCTION_DECL;
  }
}
