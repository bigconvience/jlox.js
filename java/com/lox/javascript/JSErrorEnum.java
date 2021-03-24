package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSErrorEnum
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/109:08 PM
 */
public enum JSErrorEnum {
  JS_EVAL_ERROR,
  JS_RANGE_ERROR,
  JS_REFERENCE_ERROR,
  JS_SYNTAX_ERROR,
  JS_TYPE_ERROR,
  JS_URI_ERROR,
  JS_INTERNAL_ERROR,
  JS_AGGREGATE_ERROR,

  JS_NATIVE_ERROR_COUNT,
  ;


  @Override
  public String toString() {
    switch (this) {
      case JS_SYNTAX_ERROR:
        return "SyntaxError";
    }
    return super.toString();
  }
}
