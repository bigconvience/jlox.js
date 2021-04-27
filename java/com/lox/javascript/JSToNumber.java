package com.lox.javascript;

import static com.lox.javascript.JSStringUtils.*;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JSToPrimitive.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JSToNumberHintEnum.*;
import static com.lox.javascript.JUtils.js_atof;
import static com.lox.javascript.JUtils.skip_spaces;

/**
 * @author benpeng.jiang
 * @title: NumberUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/2010:30 AM
 */
public class JSToNumber {
  static final int ATOD_INT_ONLY = 1 << 0;
  static final int ATOD_ACCEPT_BIN_OCT = 1 << 2;

  static int JS_ToUint32(JSContext ctx, Pointer<Integer> pres, final JSValue val)
  {
    return JS_ToInt32(ctx, pres, val);
  }

  static int JS_ToInt32(JSContext ctx, Pointer<Integer> pres, final JSValue val)
  {
    return JS_ToInt32Free(ctx, pres, JS_DupValue(ctx, val));
  }

  static long JS_ToUint32Free(JSContext ctx, Pointer<Integer> pres, JSValue val)
  {
    return JS_ToInt32Free(ctx, pres, val);
  }

  /* return (<0, 0) in case of exception */
  static int JS_ToInt32Free(JSContext ctx, Pointer<Integer> pres, JSValue val)
  {
    JSTag tag;
    int ret;

      tag = JS_VALUE_GET_NORM_TAG(val);
      switch (tag) {
        case JS_TAG_INT:
        case JS_TAG_BOOL:
        case JS_TAG_NULL:
        case JS_TAG_UNDEFINED:
          ret = JS_VALUE_GET_INT(val);
          break;
        case JS_TAG_FLOAT64: {
          JSFloat64Union u = new JSFloat64Union();
          double d;
          int e;
          d = JS_VALUE_GET_FLOAT64(val);
          u.d = d;
          /* we avoid doing fmod(x, 2^32) */
          e = (int)(u.u64 >> 52) & 0x7ff;
          if ((e <= (1023 + 30))) {
            /* fast case */
            ret = (int) d;
          } else if (e <= (1023 + 30 + 53)) {
            long v;
            /* remainder modulo 2^32 */
            v = (u.u64 & (((long) 1 << 52) - 1)) | ((long) 1 << 52);
            v = v << ((e - 1023) - 52 + 32);
            ret = (int) (v >> 32);
            /* take the sign into account */
            if (u.u64 >> 63 != 0)
              ret = -ret;
          } else {
            ret = 0; /* also handles NaN and +inf */
          }
        }
        break;
        default:
          val = JS_ToNumberFree(ctx, val);
          if (JS_IsException(val)) {
            pres.val = 0;
            return -1;
          }
          return JS_ToInt32Free(ctx, pres, val);
      }

    pres.val = ret;
    return 0;
  }

  static JSValue JS_ToNumberHintFree(JSContext ctx, JSValue val,
                                     JSToNumberHintEnum flag)
  {
    JSTag tag;
    JSValue ret;

    tag = JS_VALUE_GET_NORM_TAG(val);
    switch(tag) {
      case JS_TAG_FLOAT64:
      case JS_TAG_INT:
      case JS_TAG_EXCEPTION:
        ret = val;
        break;
      case JS_TAG_BOOL:
      case JS_TAG_NULL:
        ret = JS_NewInt32(ctx, JS_VALUE_GET_INT(val));
        break;
      case JS_TAG_UNDEFINED:
        ret = JS_NAN;
        break;
      case JS_TAG_OBJECT:
        val = JS_ToPrimitiveFree(ctx, val, HINT_NUMBER);
        if (JS_IsException(val))
          return JS_EXCEPTION;
        return JS_ToNumberHintFree(ctx, val, flag);
      case JS_TAG_STRING:
      {
            final char[] str;
         char[] p;
        int len;
        Pointer<Integer> plen = new Pointer<>();

        str = JS_ToCStringLen(ctx, plen, val);
        len = plen.val;
        JS_FreeValue(ctx, val);
        if (str == null)
          return JS_EXCEPTION;
        p = str;
        p = skip_spaces(p);
        if ((p.length == str.length)) {
          ret = JS_NewInt32(ctx, 0);
        } else {
          int flags = ATOD_ACCEPT_BIN_OCT;
          ret = js_atof(ctx, p, p, 0, flags);
          if (!JS_IsException(ret)) {
            p = skip_spaces(p);
            if (p.length != str.length) {
              JS_FreeValue(ctx, ret);
              ret = JS_NAN;
            }
          }
        }
        JS_FreeCString(ctx, str);
      }
      break;
      case JS_TAG_SYMBOL:
        JS_FreeValue(ctx, val);
        return JS_ThrowTypeError(ctx, "cannot convert symbol to number");
      default:
        JS_FreeValue(ctx, val);
        ret = JS_NAN;
        break;
    }
    return ret;
  }
  static JSValue JS_ToNumber(JSContext ctx, JSValue val)
  {
    return JS_ToNumberFree(ctx, JS_DupValue(ctx, val));
  }

  static JSValue JS_ToNumberFree(JSContext ctx, JSValue val)
  {
    return JS_ToNumberHintFree(ctx, val, TON_FLAG_NUMBER);
  }

  static JSValue JS_ToNumericFree(JSContext ctx, JSValue val)
  {
    return JS_ToNumberHintFree(ctx, val, TON_FLAG_NUMERIC);
  }

  static JSValue JS_ToNumeric(JSContext ctx, final JSValue val)
  {
    return JS_ToNumericFree(ctx, JS_DupValue(ctx, val));
  }

}
