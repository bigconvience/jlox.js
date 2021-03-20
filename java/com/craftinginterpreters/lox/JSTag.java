package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSTag
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/39:58 AM
 */
public enum JSTag {
  /* all tags withT a reference count are negative */
  JS_TAG_FIRST, /* first negative tag */
  JS_TAG_BIG_DECIMAL,
  JS_TAG_BIG_INT,
  JS_TAG_BIG_FLOAT,
  JS_TAG_SYMBOL,
  JS_TAG_STRING,
  JS_TAG_MODULE, /* used internally */
  JS_TAG_FUNCTION_BYTECODE, /* used internally */
  JS_TAG_OBJECT,

  JS_TAG_INT,
  JS_TAG_BOOL,
  JS_TAG_NULL,
  JS_TAG_UNDEFINED,
  JS_TAG_UNINITIALIZED,
  JS_TAG_CATCH_OFFSET,
  JS_TAG_EXCEPTION,
  JS_TAG_FLOAT64;
  /* any larger tag is FLOAT64 if JS_NAN_BOXING */

  static boolean tag_is_number(JSTag tag)
  {
    return (tag == JS_TAG_INT || tag == JS_TAG_BIG_INT ||
      tag == JS_TAG_FLOAT64 || tag == JS_TAG_BIG_FLOAT ||
      tag == JS_TAG_BIG_DECIMAL);
  }
}
