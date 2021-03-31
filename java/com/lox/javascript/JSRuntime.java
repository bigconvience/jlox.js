package com.lox.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lox.javascript.JSAtom.*;
import static com.lox.clibrary.stdio_h.printf;

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
  final Map<JSString, JSClass> class_array;

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
    this.class_array = new HashMap<>();

    JS_InitAtoms();
  }

  public JSContext JS_NewCustomContext() {
    JSContext ctx = JS_NewContext();


    return ctx;
  }

  JSContext JS_NewContext() {
    JSContext ctx = JS_NewContextRaw();

    ctx.JS_AddIntrinsicBaseObjects();
    return ctx;
  }

  JSContext JS_NewContextRaw() {
    JSContext ctx = new JSContext(this);


    return ctx;
  }

  public JSAtom JS_NewAtomStr(String str) {
    return __JS_NewAtom(str, JS_ATOM_TYPE_STRING);
  }

  private JSAtom __JS_NewAtom(String str, int atom_type) {
    if (false) {
      printf("__JS_NewAtom: "  + str + "\n");
    }
    JSString p = new JSString(str, atom_type);
    if (atom_hash.containsKey(p)) {
      Integer atom = atom_hash.get(p);
      return new JSAtom(atom);
    }
    int atomCount = atom_array.size();
    atom_hash.put(p, atomCount);
    atom_array.add(p);
    return new JSAtom(atomCount);
  }

  private JSAtom __JS_FindAtom(String str, int atom_type) {
    JSString p = new JSString(str, atom_type);
    if (atom_hash.containsKey(p)) {
      Integer atom = atom_hash.get(p);
      return new JSAtom(atom);
    }

    return JS_ATOM_NULL;
  }

  private void JS_InitAtoms() {
    atom_array.clear();
    atom_hash.clear();
    JS_NewAtomStr(null);
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
      __JS_NewAtom(str, atom_type);
    }
    atom_hash.size();
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

}
