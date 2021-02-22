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
    vars = new HashMap<>();
    args = new HashMap<>();
    hoistDef = new HashMap<>();
    scopes = new ArrayList<>();
    statements = new ArrayList<>();
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

  final Map<String, JSVarDef> vars;
  final Map<String, JSVarDef> args;
  final Map<String, JSHoistedDef> hoistDef;
  List<Stmt> body;
  int evalType;
  boolean isEval;
  boolean isGlobalVar;
  JSVarScope curScope;
  final List<Stmt> statements;

  String funcName;

  void addStmt(Stmt stmt) {
    statements.add(stmt);
  }

  void addVarDef(String name, JSVarDef varDef) {
    vars.put(name, varDef);
  }

  JSVarDef getVarDef(String name) {
    return vars.get(name);
  }


  JSVarDef getArgDef(String name) {
    return vars.get(name);
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
      JSVarScope varScope = new JSVarScope();
      scopes.add(varScope);
      int scope = getScopeCount();
      scopeLevel = scope;
      return scope;
  }
}
