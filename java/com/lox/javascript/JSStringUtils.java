package com.lox.javascript;

import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSContext.JS_AtomToString;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JSToPrimitive.*;
import static com.lox.javascript.JSValue.*;
import static java.lang.Boolean.*;

/**
 * @author benpeng.jiang
 * @title: JSStringUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/211:52 PM
 */
public class JSStringUtils {
  /* radix != 10 is only supported with flags = JS_DTOA_VAR_FORMAT */
  /* use as many digits as necessary */
  static final int JS_DTOA_VAR_FORMAT = (0 << 0);
  /* use n_digits significant digits (1 <= n_digits <= 101) */
  static final int JS_DTOA_FIXED_FORMAT = (1 << 0);
  /* force fractional format: [-]dd.dd with n_digits fractional digits */
  static final int JS_DTOA_FRAC_FORMAT = (2 << 0);
  /* force exponential notation either in fixed or variable format */
  static final int JS_DTOA_FORCE_EXP = (1 << 2);


  static JSValue JS_ToStringInternal(JSContext ctx, final JSValue val, boolean is_ToPropertyKey) {
    JSTag tag;
    String str;

    tag = JS_VALUE_GET_NORM_TAG(val);
    switch (tag) {
      case JS_TAG_STRING:
        return JS_DupValue(ctx, val);
      case JS_TAG_INT:
        str = String.valueOf(JS_VALUE_GET_INT(val));
        return JS_NewString(ctx, str);
      case JS_TAG_BOOL:
        return JS_AtomToString(ctx, JS_VALUE_GET_BOOL(val) != 0 ?
          JS_ATOM_true.toJSAtom() : JS_ATOM_false.toJSAtom());
      case JS_TAG_NULL:
        return JS_AtomToString(ctx, JS_ATOM_null.toJSAtom());
      case JS_TAG_UNDEFINED:
        return JS_AtomToString(ctx, JS_ATOM_undefined.toJSAtom());
      case JS_TAG_EXCEPTION:
        return JS_EXCEPTION;
      case JS_TAG_OBJECT:
        JSValue val1, ret;
        val1 = JS_ToPrimitive(ctx, val, HINT_STRING);
        if (JS_IsException(val1))
          return val1;
        ret = JS_ToStringInternal(ctx, val1, is_ToPropertyKey);
        JS_FreeValue(ctx, val1);
        return ret;
      case JS_TAG_FUNCTION_BYTECODE:
        str = "[function bytecode]";
        return JS_NewString(ctx, str);
      case JS_TAG_SYMBOL:
        if (is_ToPropertyKey) {
          return JS_DupValue(ctx, val);
        } else {
          return JS_ThrowTypeError(ctx, "cannot convert symbol to string");
        }
      case JS_TAG_FLOAT64:
        return js_dtoa(ctx, JS_VALUE_GET_FLOAT64(val), 10, 0,
          JS_DTOA_VAR_FORMAT);
      default:
        str = "[unsupported type]";
        return JS_NewString(ctx, str);
    }
  }

  static JSValue JS_ToString(JSContext ctx, final JSValue val) {
    return JS_ToStringInternal(ctx, val, FALSE);
  }

  static char[] JS_ToCStringLen(JSContext ctx, Pointer<Integer> plen, final JSValue val1) {
    return JS_ToCStringLen2(ctx, plen, val1, false);
  }

  static char[] JS_ToCString(JSContext ctx, final JSValue val1) {
    return JS_ToCStringLen2(ctx, null, val1, false);
  }

  /* return (NULL, 0) if exception. */
  /* return pointer into a JSString with a live ref_count */
  /* cesu8 determines if non-BMP1 codepoints are encoded as 1 or 2 utf-8 sequences */
  static char[] JS_ToCStringLen2(JSContext ctx, Pointer<Integer> plen, final JSValue val1, boolean cesu8) {
    JSValue val;
    JSString str, str_new;
    int pos, len, c, c1;
    int q;

    if (JS_VALUE_GET_TAG(val1) != JS_TAG_STRING) {
      val = JS_ToString(ctx, val1);
      if (JS_IsException(val))
        return on_fail(plen);
    } else {
      val = JS_DupValue(ctx, val1);
    }

    str = JS_VALUE_GET_STRING(val);
    return str.getChars();
  }

  private static char[] on_fail(Pointer<Integer> plen) {
    if (plen != null)
      plen.val = 0;
    return null;
  }

  static void JS_FreeCString(JSContext ctx, final char[] ptr) {
    JSString p;
    if (ptr == null)
      return;
    /* purposely removing constness */
    p = new JSString(new String(ptr));
    JS_FreeValue(ctx, JS_MKPTR(JS_TAG_STRING, p));
  }

  static final int JS_DTOA_BUF_SIZE = 126;

  static void js_dtoa1(char[] buf, double d, int radix, int n_digits, int flags) {

  }


  static JSValue js_dtoa(JSContext ctx,
                         double d, int radix, int n_digits, int flags) {
    char[] buf = new char[JS_DTOA_BUF_SIZE];
    js_dtoa1(buf, d, radix, n_digits, flags);
    return JS_NewString(ctx, buf);
  }
}
