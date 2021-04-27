package com.lox.javascript;


import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSContext.JS_AtomToString;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.JS_ThrowTypeError;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.VM.JS_CallFree;

/**
 * @author benpeng.jiang
 * @title: JSToPrimitive
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/201:54 PM
 */
public class JSToPrimitive {
  public static final int HINT_STRING = 0;
  public static final int HINT_NUMBER = 1;
  public static final int HINT_NONE = 2;
  public static final int HINT_FORCE_ORDINARY = 1 << 4;

  static JSValue JS_ToPrimitiveFree(JSContext ctx, JSValue val, int hint)
  {
    int i;
    boolean force_ordinary;

    JSAtom method_name;
    JSValue method, ret;
    if (JS_VALUE_GET_TAG(val) != JS_TAG_OBJECT)
      return val;
    force_ordinary = (hint & HINT_FORCE_ORDINARY) != 0;
    hint &= ~HINT_FORCE_ORDINARY;
    if (!force_ordinary) {
      method = JS_GetProperty(ctx, val, JS_ATOM_Symbol_toPrimitive.toJSAtom());
      if (JS_IsException(method))
        return on_exception(ctx, val);
        /* ECMA says *If exoticToPrim is not undefined* but tests in
           test262 use null as a non callable converter */
      if (!JS_IsUndefined(method) && !JS_IsNull(method)) {
        JSAtom atom;
        JSValue arg;
        switch(hint) {
          case HINT_STRING:
            atom = JS_ATOM_string.toJSAtom();
            break;
          case HINT_NUMBER:
            atom = JS_ATOM_number.toJSAtom();
            break;
          default:
          case HINT_NONE:
            atom = JS_ATOM_default.toJSAtom();
            break;
        }
        arg = JS_AtomToString(ctx, atom);
        ret = JS_CallFree(ctx, method, val, 1, new JSValue[]{arg});
        JS_FreeValue(ctx, arg);
        if (JS_IsException(ret))
          return on_exception(ctx, val);
        JS_FreeValue(ctx, val);
        if (JS_VALUE_GET_TAG(ret) != JS_TAG_OBJECT)
          return ret;
        JS_FreeValue(ctx, ret);
        return JS_ThrowTypeError(ctx, "toPrimitive");
      }
    }
    if (hint != HINT_STRING)
      hint = HINT_NUMBER;
    for(i = 0; i < 2; i++) {
      if ((i ^ hint) == 0) {
        method_name = JS_ATOM_toString.toJSAtom();
      } else {
        method_name = JS_ATOM_valueOf.toJSAtom();
      }
      method = JS_GetProperty(ctx, val, method_name);
      if (JS_IsException(method))
        return on_exception(ctx, val);
      if (JS_IsFunction(ctx, method)) {
        ret = JS_CallFree(ctx, method, val, 0, null);
        if (JS_IsException(ret))
          return on_exception(ctx, val);
        if (JS_VALUE_GET_TAG(ret) != JS_TAG_OBJECT) {
          JS_FreeValue(ctx, val);
          return ret;
        }
        JS_FreeValue(ctx, ret);
      } else {
        JS_FreeValue(ctx, method);
      }
    }
    JS_ThrowTypeError(ctx, "toPrimitive");
    return on_exception(ctx, val);
  }

  private static JSValue on_exception(JSContext ctx, JSValue val) {
    JS_FreeValue(ctx, val);
    return JS_EXCEPTION;
  }

  static JSValue JS_ToPrimitive(JSContext ctx, final JSValue val, int hint)
  {
    return JS_ToPrimitiveFree(ctx, JS_DupValue(ctx, val), hint);
  }

}
