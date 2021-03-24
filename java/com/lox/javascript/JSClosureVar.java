package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSClosureVar
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/111:54 PM
 */
public class JSClosureVar {
  boolean is_local = true;
  boolean is_arg = true;
  boolean is_const = true;
  boolean is_lexical = true;
  JSVarKindEnum var_kind = JSVarKindEnum.JS_VAR_CATCH; /* see JSVarKindEnum */
  /* 9 bits available */
  int var_idx; /* is_local = TRUE: index to a normal variable of the
                    parent function. otherwise: index to a closure
                    variable of the parent function */
  JSAtom var_name;
}
