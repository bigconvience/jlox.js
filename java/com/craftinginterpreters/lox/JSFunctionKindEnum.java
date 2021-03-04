package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.JSClassID.*;

/**
 * @author benpeng.jiang
 * @title: JSFunctionKindEnum
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/42:04 PM
 */
public interface JSFunctionKindEnum {
  int JS_FUNC_NORMAL = 0;
  int JS_FUNC_GENERATOR = (1 << 0);
  int JS_FUNC_ASYNC = (1 << 1);
  int JS_FUNC_ASYNC_GENERATOR = (JS_FUNC_GENERATOR | JS_FUNC_ASYNC);

  JSClassID func_kind_to_class_id[] = {
    JS_CLASS_BYTECODE_FUNCTION,
    JS_CLASS_GENERATOR_FUNCTION,
    JS_CLASS_ASYNC_FUNCTION,
    JS_CLASS_ASYNC_GENERATOR_FUNCTION,
  };
}
