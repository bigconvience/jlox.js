package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSVarDefEnum
 * @projectName LoxScript
 * @description: TODO
 * @date 2020/12/319:23 PM
 */
public enum JSVarDefEnum {
  JS_VAR_DEF_WITH,
  JS_VAR_DEF_LET,
  JS_VAR_DEF_CONST,
  JS_VAR_DEF_FUNCTION_DECL, /* function declaration */
  JS_VAR_DEF_NEW_FUNCTION_DECL, /* async/generator function declaration */
  JS_VAR_DEF_CATCH,
  JS_VAR_DEF_VAR,
}
