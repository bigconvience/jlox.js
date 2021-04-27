package com.lox.javascript;

import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSValue.*;
import static java.lang.Boolean.FALSE;

/**
 * @author benpeng.jiang
 * @title: JSStringUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/211:52 PM
 */
public class JSStringUtils {
  static JSValue JS_ToStringInternal(JSContext ctx, final JSValue val, boolean is_ToPropertyKey)
  {
    return null;
  }

  static JSValue JS_ToString(JSContext ctx, final JSValue val)
  {
    return JS_ToStringInternal(ctx, val, FALSE);
  }

  static  char[] JS_ToCStringLen(JSContext ctx, Pointer<Integer> plen, final JSValue val1)
  {
    return JS_ToCStringLen2(ctx, plen, val1, false);
  }
  static   char[] JS_ToCString(JSContext ctx, final JSValue val1)
  {
    return JS_ToCStringLen2(ctx, null, val1, false);
  }

  /* return (NULL, 0) if exception. */
  /* return pointer into a JSString with a live ref_count */
  /* cesu8 determines if non-BMP1 codepoints are encoded as 1 or 2 utf-8 sequences */
static char[] JS_ToCStringLen2(JSContext ctx, Pointer<Integer> plen, final JSValue val1, boolean cesu8)
  {
    JSValue val;
    JSString str, str_new;
    int pos, len, c, c1;
    int  q;

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

  static void JS_FreeCString(JSContext ctx, final char[] ptr)
  {
    JSString p;
    if (ptr == null)
      return;
    /* purposely removing constness */
    p = new JSString(new String(ptr));
    JS_FreeValue(ctx, JS_MKPTR(JS_TAG_STRING, p));
  }
}
