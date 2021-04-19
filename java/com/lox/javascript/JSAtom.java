package com.lox.javascript;

import java.util.Objects;

import static com.lox.clibrary.stdio_h.printf;
import static com.lox.clibrary.string_h.strlen;
import static com.lox.javascript.JSAtomEnum.JS_ATOM_END;
import static com.lox.javascript.JSRuntime.__JS_NewAtom;
import static com.lox.javascript.JSRuntime.js_free_string;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.LoxJS.is_digit;
import static com.lox.javascript.LoxJS.is_num_string;

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

  int __JS_AtomToUInt32() {
    return getVal() & ~JS_ATOM_TAG_INT;
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
      if (atom != null)
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
    PInteger n = new PInteger();
    if (is_num_string(n, p)) {
    if (n.value <= JS_ATOM_MAX_INT) {
      js_free_string(rt, p);
      return __JS_AtomFromUInt32(n.value);
    }
  }
    /* XXX: should generate an exception */
    return __JS_NewAtom(rt, p, JS_ATOM_TYPE_STRING);
  }

  static  JSAtom __JS_AtomFromUInt32(int v)
  {
    return new JSAtom(v | JS_ATOM_TAG_INT);
  }

  static void JS_FreeAtom(JSContext ctx, JSAtom v)
  {

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
}