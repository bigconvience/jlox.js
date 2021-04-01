package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSFunctionKindEnum
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/42:04 PM
 */
public enum JSFunctionKindEnum {
   JS_FUNC_NORMAL,
   JS_FUNC_GENERATOR ,
   JS_FUNC_ASYNC ,
   JS_FUNC_ASYNC_GENERATOR;

  static JSClassID func_kind_to_class_id[] = {
    JSClassID.JS_CLASS_BYTECODE_FUNCTION,
    JSClassID.JS_CLASS_GENERATOR_FUNCTION,
    JSClassID.JS_CLASS_ASYNC_FUNCTION,
    JSClassID.JS_CLASS_ASYNC_GENERATOR_FUNCTION,
  };
}
