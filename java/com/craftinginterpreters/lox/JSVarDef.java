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
  JSVarKindEnum var_kind;
  JSAtom var_name;
  int func_pool_or_scope_idx;
  boolean isGlobalVar;
  int cpool_idx;
  int varIdx;
  boolean forceInit;
  boolean is_captured;
}
