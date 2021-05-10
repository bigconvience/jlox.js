package com.lox.javascript;

import java.util.ArrayList;
import java.util.List;

import static com.lox.javascript.JSRuntime.free_var_ref;
import static com.lox.javascript.JSRuntime.js_autoinit_free;
import static com.lox.javascript.JSShape.*;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;

/**
 * @author benpeng.jiang
 * @title: JSObject
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/151:47 PM
 */
public class JSObject extends JSRefCountHeader{
  boolean is_HTMLDDA;
  boolean is_uncatchable_error;
  boolean extensible = true;
  boolean is_exotic;
  boolean fast_array;
  boolean is_constructor;
  JSProxyData proxy_data;
  JSShape shape;
  JSProperty[] prop;
  JSClassID class_id;


  final U u;

  static class U {
    final Array array;
    final CFunc cfunc;
    final Func func;
    JSTypedArray[] typed_array;

    public U() {
      array = new Array();
      cfunc = new CFunc();
      func = new Func();
    }
  }

  static class Func {
    JSFunctionBytecode function_bytecode;
    List<JSVarRef> var_refs;
    JSObject homeObject;
  }


  static class CFunc {
    JSContext realm;
    JSCFunctionType c_function;
    int length;
    JSCFunctionEnum cproto;
    int magic;

    public CFunc() {
      c_function = new JSCFunctionType();
    }
  }

  static class Array {
    public u1 u1;
    public u u;
    static class u1 {
      uint32_t size;          /* JS_CLASS_ARRAY, JS_CLASS_ARGUMENTS */
      JSTypedArray typed_array; /* JS_CLASS_UINT8C_ARRAY..JS_CLASS_FLOAT64_ARRAY */
    }

    static class u {
      JSValue[] values;        /* JS_CLASS_ARRAY, JS_CLASS_ARGUMENTS */
      Object ptr;              /* JS_CLASS_UINT8C_ARRAY..JS_CLASS_FLOAT64_ARRAY */
      byte int8_ptr;       /* JS_CLASS_INT8_ARRAY */
      byte uint8_ptr;     /* JS_CLASS_UINT8_ARRAY, JS_CLASS_UINT8C_ARRAY */
      char int16_ptr;     /* JS_CLASS_INT16_ARRAY */
      char uint16_ptr;   /* JS_CLASS_UINT16_ARRAY */
      int int32_ptr;     /* JS_CLASS_INT32_ARRAY */
      int uint32_ptr;   /* JS_CLASS_UINT32_ARRAY */
      long int64_ptr;     /* JS_CLASS_INT64_ARRAY */
      long uint64_ptr;   /* JS_CLASS_UINT64_ARRAY */
      float float_ptr;       /* JS_CLASS_FLOAT32_ARRAY */
      double double_ptr;     /* JS_CLASS_FLOAT64_ARRAY */
    }

    int count;

    public Array() {
       u1 = new u1();
       u = new u();
    }
  }

  public JSObject() {
    proxy_data = new JSProxyData();
    u = new U();
  }

  static void free_property(JSRuntime rt, JSProperty pr, int prop_flags)
  {
    if ((prop_flags & JS_PROP_TMASK) != 0) {
      if ((prop_flags & JS_PROP_TMASK) == JS_PROP_GETSET) {
        if (pr.u.getset.getter != null)
          JS_FreeValueRT(rt, JS_MKPTR(JS_TAG_OBJECT, pr.u.getset.getter));
        if (pr.u.getset.setter != null)
          JS_FreeValueRT(rt, JS_MKPTR(JS_TAG_OBJECT, pr.u.getset.setter));
      } else if ((prop_flags & JS_PROP_TMASK) == JS_PROP_VARREF) {
        free_var_ref(rt, pr.u.var_ref.val);
      } else if ((prop_flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
        js_autoinit_free(rt, pr);
      }
    } else {
      JS_FreeValueRT(rt, pr.u.value);
    }
  }

  static  JSShapeProperty find_own_property1(JSObject p,
                                             JSAtom atom)
  {
    JSShape sh;
    JSShapeProperty pr;
    JSShapeProperty[] prop;
    int h;
    sh = p.shape;
    h = atom.getVal() & sh.prop_hash_mask;
    h = prop_hash_end(sh)[h];
    prop = get_shape_prop(sh);
    while (h != 0) {
      pr = prop[h - 1];
      if ((pr.atom == atom)) {
        return pr;
      }
      h = pr.hash_next;
    }
    return null;
  }

  static JSShapeProperty find_own_property(Pointer<JSProperty> ppr,
                                                         JSObject p,
                                                         JSAtom atom)
  {
    JSShape sh;
    JSShapeProperty pr;
    JSShapeProperty[] prop;
    int h;
    sh = p.shape;
    h = atom.getVal() & sh.prop_hash_mask;
    h = prop_hash_end(sh)[h];
    prop = get_shape_prop(sh);
    while (h != 0) {
      pr = prop[h - 1];
      if (pr.atom == atom) {
            ppr.val = p.prop[h - 1];
        /* the compiler should be able to assume that pr != NULL here */
        return pr;
      }
      h = pr.hash_next;
    }
    ppr.val = null;
    return null;
  }


  static JSShapeProperty find_own_property(PJSProperty ppr, JSObject p, JSAtom atom) {
    JSShape sh;
    JSShapeProperty pr;
    JSShapeProperty[] prop;
    int h;
    sh = p.shape;
    h = atom.getVal() & sh.prop_hash_mask;
    h = prop_hash_end(sh)[h];
    prop = get_shape_prop(sh);
    while (h != 0) {
      pr = prop[h - 1];
      if (pr.atom.getVal() == atom.getVal()) {
        ppr.val = p.prop[h - 1];
        /* the compiler should be able to assume that pr != NULL here */
        return pr;
      }
      h = pr.hash_next;
    }
    ppr.val = null;
    return null;
  }


  public void defineProperty(String key, Object value) {
  }

  public Object getValue(String key) {
    return null;
  }

  public void setProp(String key, Object value) {

  }

  @Override
  public String toString() {
    return "JSObject{" +
      "prop=" + prop +
      '}';
  }
}
