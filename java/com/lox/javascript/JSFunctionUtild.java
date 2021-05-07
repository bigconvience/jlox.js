package com.lox.javascript;

import static com.lox.javascript.JSContext.*;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;
import static com.lox.javascript.VM.JS_CallFree;

/**
 * @author benpeng.jiang
 * @title: JSFunctionUtild
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/5/710:12 AM
 */
public class JSFunctionUtild {

  static int call_setter(JSContext ctx, JSObject setter,
                         final JSValue this_obj, JSValue val, int flags)
  {
    JSValue ret, func;
    if (setter != null) {
      func = JS_MKPTR(JS_TAG_OBJECT, setter);
      /* Note: the field could be removed in the setter */
      func = JS_DupValue(ctx, func);
      ret = JS_CallFree(ctx, func, this_obj, 1, new JSValue[]{val});
      JS_FreeValue(ctx, val);
      if (JS_IsException(ret))
        return -1;
      JS_FreeValue(ctx, ret);
      return 1;
    } else {
      JS_FreeValue(ctx, val);
      if ((flags & JS_PROP_THROW) != 0 ||
        (((flags & JS_PROP_THROW_STRICT) != 0) && is_strict_mode(ctx))) {
        JS_ThrowTypeError(ctx, "no setter for property");
        return -1;
      }
      return 0;
    }
  }
}
