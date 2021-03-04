package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSConstants
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/11:52 PM
 */
public class JConstants {
  static final int DEFINE_GLOBAL_LEX_VAR = (1 << 7);
  static final int DEFINE_GLOBAL_FUNC_VAR = (1 << 6);

  static final int JS_MAX_LOCAL_VARS = 65536;
  static final int JS_STACK_SIZE_MAX = 65536;
  static final int JS_STRING_LEN_MAX = ((1 << 30) - 1);

  static final int JS_CALL_FLAG_COPY_ARGV = (1 << 1);
  static final int JS_CALL_FLAG_GENERATOR = (1 << 2);
}
