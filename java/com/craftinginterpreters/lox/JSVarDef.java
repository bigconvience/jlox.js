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
  JSAtom varName;
  int funcPoolOrScopeIdx;
  boolean isGlobalVar;
  int cpool_idx;
  int varIdx;
  boolean forceInit;
}
