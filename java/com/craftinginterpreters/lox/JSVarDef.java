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
  boolean is_lexical;
  int scope_level;
  int scope_next;
  JSVarScope scope;
  JSVarKindEnum varKind;
  JSAtom var_name;
  int funcPoolOrScopeIdx;
  boolean isGlobalVar;
  int cpool_idx;
  int varIdx;
  boolean forceInit;
}
