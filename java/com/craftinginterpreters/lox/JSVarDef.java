package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSVarDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2020/12/313:47 PM
 */
public class JSVarDef {
  Token name;
  boolean isConst;
  boolean isLexical;
  int scopeLevel;
  int scopeNext;
  JSVarScope scope;
  JSVarKindEnum varKind;
  String varName;
  int funcPoolOrScopeIdx;
  boolean isGlobalVar;
  int cpoolIdx;
  int varIdx;
  boolean forceInit;
}
