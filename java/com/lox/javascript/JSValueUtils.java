package com.lox.javascript;

import static com.lox.javascript.JSClassID.*;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSValue.*;

/**
 * @author benpeng.jiang
 * @title: JSValueUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/5/49:28 AM
 */
public class JSValueUtils {
  /* Only works for primitive types, otherwise return JS_NULL. */
  static JSValue JS_GetPrototypePrimitive(JSContext ctx, JSValue val)
  {
    switch(JS_VALUE_GET_NORM_TAG(val)) {
      case JS_TAG_INT:
      case JS_TAG_FLOAT64:
        val = ctx.class_proto[JS_CLASS_NUMBER.ordinal()];
        break;
      case JS_TAG_BOOL:
        val = ctx.class_proto[JS_CLASS_BOOLEAN.ordinal()];
        break;
      case JS_TAG_STRING:
        val = ctx.class_proto[JS_CLASS_STRING.ordinal()];
        break;
      case JS_TAG_SYMBOL:
        val = ctx.class_proto[JS_CLASS_SYMBOL.ordinal()];
        break;
      case JS_TAG_OBJECT:
      case JS_TAG_NULL:
      case JS_TAG_UNDEFINED:
      default:
        val = JS_NULL;
        break;
    }
    return val;
  }

}
