package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSVarKindNum
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/511:00 AM
 */
public enum JSVarKindEnum {
  /* XXX: add more variable kinds here instead of using bit fields */
  JS_VAR_NORMAL,
  JS_VAR_FUNCTION_DECL, /* lexical var with function declaration */
  JS_VAR_NEW_FUNCTION_DECL, /* lexical var with async/generator
                                 function declaration */
  JS_VAR_CATCH,
  JS_VAR_PRIVATE_FIELD,
  JS_VAR_PRIVATE_METHOD,
  JS_VAR_PRIVATE_GETTER,
  JS_VAR_PRIVATE_SETTER, /* must come after JS_VAR_PRIVATE_GETTER */
}
