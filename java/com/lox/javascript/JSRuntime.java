package com.lox.javascript;

import com.sun.org.apache.bcel.internal.generic.JSR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lox.clibrary.string_h.strlen;
import static com.lox.javascript.JSAtom.*;
import static com.lox.clibrary.stdio_h.printf;
import static com.lox.javascript.JSClassID.JS_CLASS_INIT_COUNT;
import static com.lox.javascript.JSContext.JS_AddIntrinsicBaseObjects;
import static com.lox.javascript.JSContext.JS_AddIntrinsicBasicObjects;
import static com.lox.javascript.JSRuntimeUtils.js_realloc_rt;
import static com.lox.javascript.JSString.js_alloc_string_rt;
import static com.lox.javascript.JSValue.JS_NULL;
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
  int class_count;
  JSClass[] class_array;

  int stack_size;
  int stack_top;
  JSStackFrame current_stack_frame;
  JSValue current_exception;
  JSContext context_list;

  static boolean interrupt_handler(JSRuntime rt, Object opaque) {

    return false;
  }

  Object interrupt_opaque;


  public JSRuntime() {
    this.atom_array = new ArrayList<>();
    this.atom_hash = new HashMap<>();

    JS_InitAtoms(this);
  }

  public static JSContext JS_NewCustomContext(JSRuntime rt) {
    JSContext ctx = JS_NewContext(rt);


    return ctx;
  }

  static JSContext JS_NewContext(JSRuntime rt) {
    JSContext ctx = JS_NewContextRaw(rt);

    JS_AddIntrinsicBaseObjects(ctx);
    return ctx;
  }

  static JSContext JS_NewContextRaw(JSRuntime rt) {
    JSContext ctx = new JSContext(rt);
    ctx.class_proto = new JSValue[rt.class_count];
    JS_AddIntrinsicBasicObjects(ctx);
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
    __JS_NewAtom(rt, new JSString(null), JS_ATOM_TYPE_STRING);
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
    JSString p = js_alloc_string_rt(rt, len, 0);
    p.str = new String(str);
    return __JS_NewAtom(rt, p, atom_type);
  }

  static int init_class_range(JSRuntime rt, JSClassShortDef[] tab, int start, int count) {
    JSClassDef cm;
    int class_id;
    for (int i = 0; i < count; i++) {
      class_id = i+ start;
      cm = new JSClassDef();
      cm.finalizer = tab[i].finalizer;
      cm.gc_mark = tab[i].gc_mark;
      if (JS_NewClass1(rt, JSClassID.values()[class_id], cm, tab[i].class_name) < 0) {
        return -1;
      }
    }

    return 0;
  }

  static int JS_NewClass1(JSRuntime rt, JSClassID classId, final JSClassDef class_def, JSAtom name) {
    int new_size, i;
    JSClass cl;
    JSClass[] new_class_array;

    int class_id = classId.ordinal();
    if (class_id < rt.class_count &&
      rt.class_array[class_id].class_id != 0)
      return -1;

    if (class_id >= rt.class_count) {
      new_size = Math.max(JS_CLASS_INIT_COUNT.ordinal(),
        Math.min(class_id + 1, rt.class_count * 3 / 2));

      /* reallocate the context class prototype array, if any */
      JSContext ctx = rt.context_list;
      while (ctx != null) {

        JSValue[] new_tab;
        new_tab = js_realloc_rt(rt, JSValue.class, ctx.class_proto,
          new_size);
        if (new_tab == null)
          return -1;
        for(i = rt.class_count; i < new_size; i++)
          new_tab[i] = JS_NULL;
        ctx.class_proto = new_tab;
        ctx = ctx.link;
      }
      /* reallocate the class array */
      new_class_array = js_realloc_rt(rt, JSClass.class, rt.class_array,
         new_size);
      if (new_class_array == null)
        return -1;
      rt.class_array = new_class_array;
      rt.class_count = new_size;
    }
    cl = rt.class_array[class_id];
    cl.class_id = class_id;
    cl.class_name = JS_DupAtomRT(rt, name);
    cl.call = class_def.call;
    cl.exotic = class_def.exotic;
    return 0;
  }
  static void js_free_string(JSRuntime rt, JSString str)
  {

  }

  static void js_free(JSContext ctx, Object ptr)
  {
    js_free_rt(ctx.rt, ptr);
  }

  static void js_free_rt(JSRuntime rt, Object ptr)
  {

  }

  /* Throw out of memory in case of error */
  static <T> T[]  js_realloc(JSContext ctx, T[] ptr, int size)
  {
    Object[] dst = new Object[size];
    System.arraycopy(ptr, 0, dst, 0, size);
    return (T[])dst;
  }

  /* store extra allocated size in *pslack if successful */
  static <T> T[] js_realloc2(JSContext ctx, T[] ptr, int size)
  {
    Object[] dst = new Object[size];
    System.arraycopy(ptr, 0, dst, 0, size);
    return (T[])dst;
  }

  /* indicate that the object may be part of a function prototype cycle */
  static void set_cycle_flag(JSContext ctx, JSValue obj)
  {
  }

  static void free_var_ref(JSRuntime rt, JSVarRef var_ref)
  {
    if (var_ref != null) {

      if (--var_ref.header.ref_count == 0) {
        if (var_ref.is_detached) {

        } else {
           /* still on the stack */
        }
        js_free_rt(rt, var_ref);
      }
    }
  }

  static void js_autoinit_free(JSRuntime rt, JSProperty pr)
  {
    JS_FreeContext(js_autoinit_get_realm(pr));
  }

  static JSContext js_autoinit_get_realm(JSProperty pr)
  {
    return null;
  }

  static void JS_FreeContext(JSContext ctx) {

  }

}
