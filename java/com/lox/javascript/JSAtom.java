package com.lox.javascript;

import java.util.Objects;

import static com.lox.clibrary.stdio_h.printf;
import static com.lox.clibrary.string_h.strlen;
import static com.lox.javascript.JSAtomEnum.JS_ATOM_END;
import static com.lox.javascript.JSRuntime.__JS_NewAtom;
import static com.lox.javascript.JSRuntime.js_free_string;
import static com.lox.javascript.JSString.js_string_compare;
import static com.lox.javascript.JSStringUtils.JS_ToString;
import static com.lox.javascript.JSTag.JS_TAG_STRING;
import static com.lox.javascript.JSToNumber.JS_ToNumber;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.LoxJS.is_digit;
import static com.lox.javascript.LoxJS.is_num_string;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class JSAtom {
  static final int JS_ATOM_TYPE_STRING = 0;
  static final int JS_ATOM_TYPE_GLOBAL_SYMBOL = 1;
  static final int JS_ATOM_TYPE_SYMBOL = 2;
  static final int JS_ATOM_TYPE_PRIVATE = 3;

  static final int JS_ATOM_TAG_INT = (1 << 31);
  static final int JS_ATOM_MAX_INT = (JS_ATOM_TAG_INT - 1);
  static final int JS_ATOM_MAX = ((1 << 30) - 1);

  boolean __JS_AtomIsTaggedInt() {
    return (getVal() & JS_ATOM_TAG_INT) != 0;
  }

  static boolean __JS_AtomIsTaggedInt(JSAtom v) {
    return (v.getVal() & JS_ATOM_TAG_INT) != 0;
  }

  int __JS_AtomToUInt32() {
    return getVal() & ~JS_ATOM_TAG_INT;
  }

  static int __JS_AtomToUInt32(JSAtom v) {
    return v.getVal() & ~JS_ATOM_TAG_INT;
  }

  static final JSAtom JS_ATOM_NULL = new JSAtom(0);
  static final JSAtom JS_ATOM_empty_string = new JSAtom(JSAtomEnum.JS_ATOM_empty_string.ordinal());

  private final int val;

  public JSAtom(int val) {
    this.val = val;
  }

  public int getVal() {
    return val;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JSAtom atom = (JSAtom) o;
    return val == atom.val;
  }

  @Override
  public int hashCode() {
    return Objects.hash(val);
  }

  @Override
  public String toString() {
    return "JSAtom{" +
      "val=" + val +
      '}';
  }

  static JSAtom JS_NewAtom(JSContext ctx, final char[] str)
  {
    return JS_NewAtomLen(ctx, str, strlen(str));
  }

  static JSAtom JS_NewAtomLen(JSContext ctx, final char[] str, int len)
  {
    JSValue val;

    if (len == 0 || !is_digit(str[0])) {
      JSAtom atom = __JS_FindAtom(ctx.rt, str, len, JS_ATOM_TYPE_STRING);
      if (atom != null && atom.val != 0)
        return atom;
    }
    val = JS_NewStringLen(ctx, str, len);
    if (JS_IsException(val))
      return JS_ATOM_NULL;
    return JS_NewAtomStr(ctx, JS_VALUE_GET_STRING(val));
  }

  public static JSAtom __JS_FindAtom(JSRuntime rt, char[] str, int len, int atom_type) {
    JSString p = new JSString(str, atom_type);
    if (rt.atom_hash.containsKey(p)) {
      Integer atom = rt.atom_hash.get(p);
      return new JSAtom(atom);
    }

    return JS_ATOM_NULL;
  }

  static JSAtom JS_NewAtomStr(JSContext ctx, JSString p)
  {
    JSRuntime rt = ctx.rt;
    uint32_t n = new uint32_t(0);
    if (is_num_string(n, p)) {
    if (n.val <= JS_ATOM_MAX_INT) {
      js_free_string(rt, p);
      return __JS_AtomFromUInt32(n.toInt());
    }
  }
    /* XXX: should generate an exception */
    return __JS_NewAtom(rt, p, JS_ATOM_TYPE_STRING);
  }

  static  JSAtom __JS_AtomFromUInt32(int v)
  {
    return new JSAtom(v | JS_ATOM_TAG_INT);
  }

  static int JS_AtomIsNumericIndex(JSContext ctx, JSAtom atom)
  {
    JSValue num;
    num = JS_AtomIsNumericIndex1(ctx, atom);
    if (JS_IsUndefined(num))
      return 0;
    if (JS_IsException(num))
      return -1;
    JS_FreeValue(ctx, num);
    return 1;
  }

  static void JS_FreeAtom(JSContext ctx, JSAtom v)
  {

  }

  static JSAtom JS_DupAtomRT(JSRuntime rt, JSAtom v)
  {
    JSString p;

    if (!__JS_AtomIsConst(v)) {
      p = rt.atom_array.get(v.getVal());
      p.header.ref_count++;
    }
    return v;
  }

  static JSAtom JS_DupAtom(JSContext ctx, JSAtom v)
  {
    JSRuntime rt;
    JSString p;

    if (!__JS_AtomIsConst(v)) {
      rt = ctx.rt;
      p = rt.atom_array.get(v.val);
      p.header.ref_count++;
    }
    return v;
  }

  static boolean __JS_AtomIsConst(JSAtom v)
  {
    return v.val < JS_ATOM_END.ordinal();
  }



  /* return TRUE if the atom is an array index (i.e. 0 <= index <=
   2^32-2 and return its value */
  static boolean JS_AtomIsArrayIndex(JSContext ctx, uint32_t pval, JSAtom atom)
  {
    if (__JS_AtomIsTaggedInt(atom)) {
        pval.val = __JS_AtomToUInt32(atom);
      return TRUE;
    } else {
      JSRuntime rt = ctx.rt;
      JSString p;
      uint32_t val = new uint32_t(0);

      p = rt.atom_array.get(atom.val);
      if (p.atom_type == JS_ATOM_TYPE_STRING &&
        is_num_string(val, p) && val.val != -1) {
            pval.val = val.toInt();
        return TRUE;
      } else {
            pval.val = 0;
        return FALSE;
      }
    }
  }

  /* This test must be fast if atom is not a numeric index (e.g. a
     method name). Return JS_UNDEFINED if not a numeric
     index. JS_EXCEPTION can also be returned. */
  static JSValue JS_AtomIsNumericIndex1(JSContext ctx, JSAtom atom)
  {
    JSRuntime rt = ctx.rt;
    JSString p1;
    JSString p;
    int c, len, ret;
    JSValue num, str;

    if (__JS_AtomIsTaggedInt(atom))
      return JS_NewInt32(ctx, __JS_AtomToUInt32(atom));

    p1 = rt.atom_array.get(atom.getVal());
    if (p1.atom_type != JS_ATOM_TYPE_STRING)
      return JS_UNDEFINED;
    p = p1;
    len = p.str.length();
    try {
      String str1 = p.str;
      if ("-0".equals(str1)) {
        return __JS_NewFloat64(ctx, -0.0);
      }
      Long.parseLong(str1);
    } catch (Exception ex) {
      return JS_UNDEFINED;
    }
    /* XXX: bignum: would be better to only accept integer to avoid
       relying on current floating point precision */
    /* this is ECMA CanonicalNumericIndexString primitive */
    num = JS_ToNumber(ctx, JS_MKPTR(JS_TAG_STRING, p));
    if (JS_IsException(num))
      return num;
    str = JS_ToString(ctx, num);
    if (JS_IsException(str)) {
      JS_FreeValue(ctx, num);
      return str;
    }
    ret = js_string_compare(ctx, p, JS_VALUE_GET_STRING(str));
    JS_FreeValue(ctx, str);
    if (ret == 0) {
      return num;
    } else {
      JS_FreeValue(ctx, num);
      return JS_UNDEFINED;
    }
  }


  static JSAtom JS_NewAtomUInt32(JSContext ctx, long n)
  {
    if (n <= JS_ATOM_MAX_INT) {
      return __JS_AtomFromUInt32((int) n);
    } else {
      JSValue val;
      val = JS_NewString(ctx, String.valueOf(n));
      if (JS_IsException(val))
        return JS_ATOM_NULL;
      return __JS_NewAtom(ctx.rt, JS_VALUE_GET_STRING(val),
        JS_ATOM_TYPE_STRING);
    }
  }
}