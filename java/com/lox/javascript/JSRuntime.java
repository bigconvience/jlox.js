package com.lox.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lox.clibrary.string_h.strlen;
import static com.lox.javascript.JSAtom.*;
import static com.lox.clibrary.stdio_h.printf;
import static com.lox.javascript.JSValue.JS_VALUE_GET_STRING;
import static com.lox.javascript.LoxJS.is_digit;

/**
 * @author benpeng.jiang
 * @title: JSRuntime
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/268:15 PM
 */
public class JSRuntime {
  final List<JSString> atom_array;
  final Map<JSString, Integer> atom_hash;
  final List<JSClass> class_array;

  int stack_size;
  int stack_top;
  JSStackFrame current_stack_frame;
  JSValue current_exception;

  static boolean interrupt_handler(JSRuntime rt, Object opaque) {

    return false;
  }

  Object interrupt_opaque;


  public JSRuntime() {
    this.atom_array = new ArrayList<>();
    this.atom_hash = new HashMap<>();
    this.class_array = new ArrayList<>();

    JS_InitAtoms(this);
  }

  public JSContext JS_NewCustomContext() {
    JSContext ctx = JS_NewContext();


    return ctx;
  }

  JSContext JS_NewContext() {
    JSContext ctx = JS_NewContextRaw();

    ctx.JS_AddIntrinsicBaseObjects(ctx);
    return ctx;
  }

  JSContext JS_NewContextRaw() {
    JSContext ctx = new JSContext(this);


    return ctx;
  }


  public static JSAtom __JS_NewAtom(JSRuntime rt, JSString str, int atom_type) {
    if (false) {
      printf("__JS_NewAtom: "  + str + "\n");
    }
    JSString p = str;
    if (rt.atom_hash.containsKey(p)) {
      Integer atom = rt.atom_hash.get(p);
      return new JSAtom(atom);
    }
    int atomCount = rt.atom_array.size();
    rt.atom_hash.put(p, atomCount);
    rt.atom_array.add(p);
    return new JSAtom(atomCount);
  }


  private static void JS_InitAtoms(JSRuntime rt) {
    rt.atom_array.clear();
    rt.atom_hash.clear();
    __JS_NewAtomInit(rt,null, 0, JS_ATOM_TYPE_STRING);
    int atom_type;
    String str;
    for (int i = JSAtomEnum.JS_ATOM_null.ordinal(); i < JSAtomEnum.JS_ATOM_END.ordinal(); i++) {
      if (i == JSAtomEnum.JS_ATOM_Private_brand.ordinal()) {
        atom_type = JS_ATOM_TYPE_PRIVATE;
      } else if (i >= JSAtomEnum.JS_ATOM_Symbol_toPrimitive.ordinal()) {
        atom_type = JS_ATOM_TYPE_SYMBOL;
      } else {
        atom_type = JS_ATOM_TYPE_STRING;
      }
      str = JSAtomInit.js_atom_init.get(i);
      __JS_NewAtomInit(rt, str.toCharArray(), str.length(), atom_type);
    }
    rt.atom_hash.size();
  }

  static JSAtom __JS_NewAtomInit(JSRuntime rt, final char[] str, int len,
                                 int atom_type)
  {
    JSString p = new JSString(str, len);
    return __JS_NewAtom(rt, p, atom_type);
  }

  int init_class_range(JSClassShortDef[] tab, int start, int count) {
    JSClassDef cm;
    int class_id;
    for (int i = 0; i < count; i++) {
      class_id = i+ start;
      cm = new JSClassDef();
      cm.finalizer = tab[i].finalizer;
      cm.gc_mark = tab[i].gc_mark;
      if (JS_NewClass1(JSClassID.values()[class_id], cm, tab[i].class_name) < 0) {
        return -1;
      }
    }

    return 0;
  }

  int JS_NewClass1(JSClassID class_id, final JSClassDef class_def, JSAtom name) {
    return 0;
  }
  static void js_free_string(JSRuntime rt, JSString str)
  {

  }
}
