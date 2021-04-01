package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSParseFunctionEnum
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/110:36 AM
 */
public enum JSParseFunctionEnum {
  JS_PARSE_FUNC_STATEMENT,
  JS_PARSE_FUNC_VAR,
  JS_PARSE_FUNC_EXPR,
  JS_PARSE_FUNC_ARROW,
  JS_PARSE_FUNC_GETTER,
  JS_PARSE_FUNC_SETTER,
  JS_PARSE_FUNC_METHOD,
  JS_PARSE_FUNC_CLASS_CONSTRUCTOR,
  JS_PARSE_FUNC_DERIVED_CLASS_CONSTRUCTOR,
}
