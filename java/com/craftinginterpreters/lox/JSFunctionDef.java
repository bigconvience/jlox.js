package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author benpeng.jiang
 * @title: FunctionDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/228:17 PM
 */ //< stmt-expression
//> stmt-function
public class JSFunctionDef extends Stmt {
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
  }

  @Override
  <R> R accept(Visitor<R> visitor) {
    return visitor.visitFunctionStmt(this);
  }


  final Token name = null;
  final List<Token> params;
  final JSFunctionDef parent;

  final List<JSVarScope> scopes;
  int scopeLevel;
  int scopeFirst;

  final Map<String, JSVarDef> varDefMap;
  final List<JSVarDef> vars;
  final List<JSVarDef> args;
  final List<JSHoistedDef> hoistedDef;
  final List<JSClosureVar> closureVar;
  final Map<String, JSHoistedDef> hoistDef;
  List<Stmt> body;
  Stmt.Block bodyBlock;
  int evalType;
  boolean isEval;
  boolean isGlobalVar;
  JSVarScope curScope;
  DynBuf byteCode;

  String funcName;

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
    scopeLevel = scope;
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

    if (isEval && evalType == JSEvaluator.JS_EVAL_TYPE_GLOBAL) {
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
      if (vd != null && vd.varName.equals(name) && vd.scopeLevel == 0) {
        if (isChildScope(vd.funcPoolOrScopeIdx, scopeLevel)) {
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
    hf.scopeLevel = scopeLevel;
    return hf;
  }

  JSVarDef getVar(int idx) {
    if (idx >= 0) {
      return vars.get(idx);
    }
    return null;
  }

  public int addScopeVar(JSAtom varName, JSVarKindEnum varKind) {
    int idx = addVar(varName);
    if (idx >= 0) {
      JSVarDef vd = vars.get(idx);
      vd.varKind = varKind;
      vd.scopeLevel = scopeLevel;
      vd.scopeNext = scopeFirst;
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
      if (vd.varName.equals(varName) && vd.scopeLevel == 0) {
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
}
