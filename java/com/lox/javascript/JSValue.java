package com.lox.javascript;

import static com.lox.javascript.JSAtom.*;
import static com.lox.javascript.JSClassID.JS_CLASS_PROXY;
import static com.lox.javascript.JSContext.add_property;
import static com.lox.javascript.JSObject.find_own_property;
import static com.lox.javascript.JSProxy.js_proxy_isExtensible;
import static com.lox.javascript.JSString.js_get_atom_index;
import static com.lox.javascript.JSStringUtils.JS_ToStringInternal;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.JS_ThrowRangeError;
import static com.lox.javascript.JSThrower.JS_ThrowTypeErrorAtom;
import static com.lox.javascript.JSToNumber.JS_ToNumberFree;
import static com.lox.javascript.JS_PROP.*;
import static com.lox.clibrary.stdio_h.printf;
import static com.lox.javascript.JUtils.print_atom;
import static com.lox.javascript.math.isnan;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * @author benpeng.jiang
 * @title: JSValue
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/222:09 PM
 */
public class JSValue {
  public JSTag tag;
  public Object value;
  public static final JSValue JS_NULL = new JSValue(JSTag.JS_TAG_NULL, 0);
  public static final JSValue JS_UNDEFINED = new JSValue(JSTag.JS_TAG_UNDEFINED, 0);
  public static final JSValue JS_FALSE = new JSValue(JSTag.JS_TAG_BOOL, 0);
  public static final JSValue JS_TRUE = new JSValue(JSTag.JS_TAG_BOOL, 1);
  public static final JSValue JS_EXCEPTION = new JSValue(JS_TAG_EXCEPTION, 0);
  public static final JSValue JS_UNINITIALIZED = new JSValue(JSTag.JS_TAG_UNINITIALIZED, 0);
  public static final JSValue JS_NAN = new JSValue(JS_TAG_FLOAT64, 0);

  public JSValue(JSTag tag, Object value) {
    this.tag = tag;
    this.value = value;
  }

  public static JSTag JS_VALUE_GET_NORM_TAG(JSValue v) {
    return JS_VALUE_GET_TAG(v);
  }

  public static JSTag JS_VALUE_GET_TAG(JSValue v) {
    return v.tag;
  }

  public static boolean JS_VALUE_IS_BOTH_INT(JSValue v1, JSValue v2) {
    return v1.tag == JS_TAG_INT && v2.tag == JS_TAG_INT;
  }

  public JSObject JS_VALUE_GET_OBJ() {
    if (value instanceof JSObject) {
      return (JSObject) value;
    }
    return null;
  }

  public static JSObject JS_VALUE_GET_OBJ(JSValue v) {
    return v.JS_VALUE_GET_OBJ();
  }

  public static boolean JS_VALUE_HAS_REF_COUNT(JSValue v) {
   return JS_VALUE_GET_TAG(v).ordinal() >= JS_TAG_FIRST.ordinal();
  }

  public static int JS_VALUE_GET_BOOL(JSValue v) {
    return (int)v.value;
  }

  public static float JS_VALUE_GET_FLOAT64(JSValue v) {
    return (float)v.value;
  }

  public static int JS_VALUE_GET_INT(JSValue v) {
    return v.JS_VALUE_GET_INT();
  }

  public int JS_VALUE_GET_INT() {
    if (value instanceof Integer) {
      return (Integer) value;
    } else if (value instanceof Boolean) {
      return (Boolean)value?1:0;
    }
    return 0;
  }

  public static Object JS_VALUE_GET_PTR(JSValue v) {
    return v.value;
  }

  public JSTag JS_VALUE_GET_TAG() {
    return tag;
  }

  public JSString JS_VALUE_GET_String() {
    if (value instanceof JSString) {
      return (JSString) value;
    }else {
      return null;
    }
  }

  public static JSString JS_VALUE_GET_STRING(JSValue v) {
    return v.JS_VALUE_GET_String();
  }

  static JSObject get_proto_obj(JSValue v) {
    if (!JS_IsObject(v)) {
      return null;
    } else {
      return v.JS_VALUE_GET_OBJ();
    }
  }
  static boolean JS_IsNumber(JSValue v)
  {
    JSTag tag = JS_VALUE_GET_TAG(v);
    return tag == JS_TAG_INT || JS_TAG_IS_FLOAT64(tag);
  }

  public static boolean JS_IsObject(JSValue v) {
    return JS_TAG_OBJECT == v.tag;
  }

  public static boolean JS_IsException(JSValue v)
  {
    return v.tag == JS_TAG_EXCEPTION;
  }


  boolean JS_IsUninitialized() {
    return tag == JSTag.JS_TAG_UNINITIALIZED;
  }

  boolean JS_ISString() {
    return tag == JSTag.JS_TAG_STRING;
  }

  public static JSValue JS_NewBool(JSContext ctx, boolean val) {
    return new JSValue(JS_TAG_BOOL, val);
  }

  public static JSValue JS_NewBool(JSContext ctx, int val) {
    return new JSValue(JS_TAG_BOOL, val == 0 ? false : true);
  }

  public static JSValue JS_NewInt32(JSContext ctx, int val) {
    return new JSValue(JSTag.JS_TAG_INT, val);
  }

  public static JSValue JS_NewFloat64(JSContext ctx, double d)
  {
    JSValue v;
    int val = (int) d;

    /* -0 cannot be represented as integer, so we compare the bit
        representation */
    if (val == d) {
      v = JS_MKVAL(JS_TAG_INT, val);
    } else {
      v = __JS_NewFloat64(ctx, d);
    }
    return v;
  }

  static  boolean JS_IsBool(final JSValue v)
  {
    return JS_VALUE_GET_TAG(v) == JS_TAG_BOOL;
  }

  static  boolean JS_IsNull(final JSValue v)
  {
    return JS_VALUE_GET_TAG(v) == JS_TAG_NULL;
  }

  static boolean JS_IsUndefined(final JSValue v)
  {
    return JS_VALUE_GET_TAG(v) == JS_TAG_UNDEFINED;
  }


  static JSValue JS_MKVAL(JSTag tag, int val) {
    JSValue v = new JSValue(tag, val);
    return v;
  }

  static JSValue JS_MKPTR(JSTag tag, Object p) {
    JSValue v = new JSValue(tag, p);
    return v;
  }

  static JSValue __JS_NewFloat64(JSContext ctx, double d)
  {
    JSValue v = new JSValue(JS_TAG_FLOAT64, d);
    return v;
  }

  public static JSValue JS_NewString(JSContext ctx, String str) {
    return JS_NewString(ctx, str.getBytes());
  }

  public static JSValue JS_NewString(JSContext ctx, byte[] buf) {
    return js_new_string8(ctx, buf);
  }

  static JSValue js_new_string8(JSContext ctx, byte[] buf) {
    String value = new String(buf);
    JSString str = new JSString(value);
    return new JSValue(JSTag.JS_TAG_STRING, str);
  }

  void print() {
    System.out.println(value);
  }

  /* return -1 in case of exception or TRUE or FALSE. Warning: 'val' is
   freed by the function. 'flags' is a bitmask of JS_PROP_NO_ADD,
   JS_PROP_THROW or JS_PROP_THROW_STRICT. If JS_PROP_NO_ADD is set,
   the new property is not added and an error is raised. */
  public static int JS_SetPropertyInternal(JSContext ctx, final JSValue this_obj,
                             JSAtom prop, JSValue val, int flags) {
    JSObject p, p1;
    JSShapeProperty prs;
    Pointer<JSProperty> ppr = new Pointer();
    JSTag tag;
    JSPropertyDescriptor desc;
    int ret = 0;
    if (false) {
      printf("JS_SetPropertyInternal: ");
      print_atom(ctx, prop);
      printf("\n");
    }

    tag = this_obj.tag;
    if (tag != JS_TAG_OBJECT) {
      switch (tag) {
        case JS_TAG_NULL:
          JS_ThrowTypeErrorAtom(ctx, "cannot set property '%s' of null", prop);
          return -1;
        case JS_TAG_UNDEFINED:
          JS_ThrowTypeErrorAtom(ctx, "cannot set property '%s' of undefined", prop);
          return -1;
        default:
          /* even on a primitive type we can have setters on the prototype */
          p = null;
//          p1 = JS_VALUE_GET_OBJ(JS_GetPrototypePrimitive(ctx, this_obj));
//            goto prototype_lookup;
      }
    }
    p = this_obj.JS_VALUE_GET_OBJ();

    prs = find_own_property(ppr, p, prop);
    if (prs != null) {
      if ((prs.flags & (JS_PROP_TMASK | JS_PROP_WRITABLE |
        JS_PROP_LENGTH)) == JS_PROP_WRITABLE) {
        /* fast case */
        ctx.set_value(ppr.val.u.value, val);
        return 1;
      }
    }

    p1 = p;
    while (true) {
     p1 = p1.shape.proto;
     if (p1 == null) {
       break;
     }
    }

    JSProperty jsprop = add_property(ctx, p, prop, JS_PROP_C_W_E);
    jsprop.u.value = val;

//    for(;;) {
//      if (p1->is_exotic) {
//        if (p1->fast_array) {
//          if (__JS_AtomIsTaggedInt(prop)) {
//            uint32_t idx = __JS_AtomToUInt32(prop);
//            if (idx < p1->u.array.count) {
//              if (unlikely(p == p1))
//                return JS_SetPropertyValue(ctx, this_obj, JS_NewInt32(ctx, idx), val, flags);
//              else
//                break;
//            } else if (p1->class_id >= JS_CLASS_UINT8C_ARRAY &&
//              p1->class_id <= JS_CLASS_FLOAT64_ARRAY) {
//                        goto typed_array_oob;
//            }
//          } else if (p1->class_id >= JS_CLASS_UINT8C_ARRAY &&
//            p1->class_id <= JS_CLASS_FLOAT64_ARRAY) {
//            ret = JS_AtomIsNumericIndex(ctx, prop);
//            if (ret != 0) {
//              if (ret < 0) {
//                JS_FreeValue(ctx, val);
//                return -1;
//              }
//              typed_array_oob:
//              val = JS_ToNumberFree(ctx, val);
//              JS_FreeValue(ctx, val);
//              if (JS_IsException(val))
//                return -1;
//              if (typed_array_is_detached(ctx, p1)) {
//                JS_ThrowTypeErrorDetachedArrayBuffer(ctx);
//                return -1;
//              }
//              return JS_ThrowTypeErrorOrFalse(ctx, flags, "out-of-bound numeric index");
//            }
//          }
//        } else {
//                const JSClassExoticMethods *em = ctx->rt->class_array[p1->class_id].exotic;
//          if (em) {
//            JSValue obj1;
//            if (em->set_property) {
//              /* set_property can free the prototype */
//              obj1 = JS_DupValue(ctx, JS_MKPTR(JS_TAG_OBJECT, p1));
//              ret = em->set_property(ctx, obj1, prop,
//                val, this_obj, flags);
//              JS_FreeValue(ctx, obj1);
//              JS_FreeValue(ctx, val);
//              return ret;
//            }
//            if (em->get_own_property) {
//              /* get_own_property can free the prototype */
//              obj1 = JS_DupValue(ctx, JS_MKPTR(JS_TAG_OBJECT, p1));
//              ret = em->get_own_property(ctx, &desc,
//                obj1, prop);
//              JS_FreeValue(ctx, obj1);
//              if (ret < 0) {
//                JS_FreeValue(ctx, val);
//                return ret;
//              }
//              if (ret) {
//                if (desc.flags & JS_PROP_GETSET) {
//                  JSObject *setter;
//                  if (JS_IsUndefined(desc.setter))
//                    setter = NULL;
//                  else
//                    setter = JS_VALUE_GET_OBJ(desc.setter);
//                  ret = call_setter(ctx, setter, this_obj, val, flags);
//                  JS_FreeValue(ctx, desc.getter);
//                  JS_FreeValue(ctx, desc.setter);
//                  return ret;
//                } else {
//                  JS_FreeValue(ctx, desc.value);
//                  if (!(desc.flags & JS_PROP_WRITABLE))
//                                    goto read_only_prop;
//                  if (likely(p == p1)) {
//                    ret = JS_DefineProperty(ctx, this_obj, prop, val,
//                      JS_UNDEFINED, JS_UNDEFINED,
//                      JS_PROP_HAS_VALUE);
//                    JS_FreeValue(ctx, val);
//                    return ret;
//                  } else {
//                    break;
//                  }
//                }
//              }
//            }
//          }
//        }
//      }
//      p1 = p1.shape.proto;
//      prototype_lookup:
//      if (p1 == null)
//        break;
//
//      retry2:
//      prs = find_own_property(&pr, p1, prop);
//      if (prs) {
//        if ((prs->flags & JS_PROP_TMASK) == JS_PROP_GETSET) {
//          return call_setter(ctx, pr->u.getset.setter, this_obj, val, flags);
//        } else if ((prs->flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
//          /* Instantiate property and retry (potentially useless) */
//          if (JS_AutoInitProperty(ctx, p1, prop, pr))
//            return -1;
//                goto retry2;
//        } else if (!(prs->flags & JS_PROP_WRITABLE)) {
//          read_only_prop:
//          JS_FreeValue(ctx, val);
//          return JS_ThrowTypeErrorReadOnly(ctx, flags, prop);
//        }
//      }
//    }
//
//    if (unlikely(flags & JS_PROP_NO_ADD)) {
//      JS_FreeValue(ctx, val);
//      JS_ThrowReferenceErrorNotDefined(ctx, prop);
//      return -1;
//    }
//
//    if (unlikely(!p)) {
//      JS_FreeValue(ctx, val);
//      return JS_ThrowTypeErrorOrFalse(ctx, flags, "not an object");
//    }
//
//    if (unlikely(!p->extensible)) {
//      JS_FreeValue(ctx, val);
//      return JS_ThrowTypeErrorOrFalse(ctx, flags, "object is not extensible");
//    }
//
//    if (p->is_exotic) {
//      if (p->class_id == JS_CLASS_ARRAY && p->fast_array &&
//        __JS_AtomIsTaggedInt(prop)) {
//        uint32_t idx = __JS_AtomToUInt32(prop);
//        if (idx == p->u.array.count) {
//          /* fast case */
//          return add_fast_array_element(ctx, p, val, flags);
//        } else {
//                goto generic_create_prop;
//        }
//      } else {
//        generic_create_prop:
//        ret = JS_CreateProperty(ctx, p, prop, val, JS_UNDEFINED, JS_UNDEFINED,
//          flags |
//            JS_PROP_HAS_VALUE |
//            JS_PROP_HAS_ENUMERABLE |
//            JS_PROP_HAS_WRITABLE |
//            JS_PROP_HAS_CONFIGURABLE |
//            JS_PROP_C_W_E);
//        JS_FreeValue(ctx, val);
//        return ret;
//      }
//    }
//
//    pr = add_property(ctx, p, prop, JS_PROP_C_W_E);
//    if (unlikely(!pr)) {
//      JS_FreeValue(ctx, val);
//      return -1;
//    }
//    pr->u.value = val;
//    return TRUE;
    return ret;
  }

  static JSValue JS_GetProperty(JSContext ctx, final JSValue this_obj,
                                                JSAtom prop)
  {
    return JS_GetPropertyInternal(ctx, this_obj, prop, this_obj, 0);
  }

  static JSValue JS_GetPropertyInternal(JSContext ctx,
                                 final JSValue obj,
                                 JSAtom prop, final JSValue this_obj,
                                 int throw_ref_error) {
    JSObject  p;
    PJSProperty  ppr = new PJSProperty();
    JSShapeProperty  prs;
    JSTag tag;

    tag = obj.JS_VALUE_GET_TAG();
    if (tag != JS_TAG_OBJECT) {
      switch (tag) {
        case JS_TAG_NULL:
          return JS_ThrowTypeErrorAtom(ctx, "cannot read property '%s' of null", prop);
        case JS_TAG_UNDEFINED:
          return JS_ThrowTypeErrorAtom(ctx, "cannot read property '%s' of undefined", prop);
        case JS_TAG_EXCEPTION:
          return JS_EXCEPTION;
        case JS_TAG_STRING: {
          JSString p1 = obj.JS_VALUE_GET_String();
        }
        default:
          break;
      }
      return JS_UNDEFINED;
    } else {
      p = get_proto_obj(obj);
    }

    while (true) {
      prs = find_own_property(ppr, p, prop);
      if (prs != null) {
        return ppr.val.u.value;
      }
      p = p.shape.proto;
      if (p == null) {
        break;
      }
    }
    return JS_UNDEFINED;
  }

  static boolean JS_IsHTMLDDA(JSContext ctx, JSValue obj)
  {
    JSObject p;
    if (JS_VALUE_GET_TAG(obj) != JS_TAG_OBJECT)
      return false;
    p = JS_VALUE_GET_OBJ(obj);
    return p.is_HTMLDDA;
  }

  static int JS_ToBoolFree(JSContext ctx, JSValue val)
  {
    JSTag tag = JS_VALUE_GET_TAG(val);
    switch(tag) {
      case JS_TAG_INT:
        return JS_VALUE_GET_INT(val) != 0 ? 1 : 0;
      case JS_TAG_BOOL:
      case JS_TAG_NULL:
      case JS_TAG_UNDEFINED:
        return JS_VALUE_GET_INT(val);
      case JS_TAG_EXCEPTION:
        return -1;
      case JS_TAG_STRING:
      {
        int ret = JS_VALUE_GET_STRING(val).str.length() != 0 ? 1 : 0;
        JS_FreeValue(ctx, val);
        return ret;
      }
      case JS_TAG_OBJECT:
      {
        JSObject p = JS_VALUE_GET_OBJ(val);
        int ret;
        ret = !p.is_HTMLDDA ? 1 : 0;
        JS_FreeValue(ctx, val);
        return ret;
      }

      default:
        if (JS_TAG_IS_FLOAT64(tag)) {
          double d = JS_VALUE_GET_FLOAT64(val);
          return !isnan(d) && d != 0 ? 1 : 0;
        } else {
          JS_FreeValue(ctx, val);
          return 1;
        }
    }
  }

  static void JS_FreeValue(JSContext ctx, JSValue v)
  {
    if (JS_VALUE_HAS_REF_COUNT(v)) {
      JSRefCountHeader p = (JSRefCountHeader)JS_VALUE_GET_PTR(v);
      if (--p.ref_count <= 0) {
        __JS_FreeValue(ctx, v);
      }
    }
  }

  static void JS_FreeValueRT(JSRuntime rt, JSValue v)
  {
    if (JS_VALUE_HAS_REF_COUNT(v)) {
      JSRefCountHeader p = (JSRefCountHeader )JS_VALUE_GET_PTR(v);
      if (--p.ref_count <= 0) {
        __JS_FreeValueRT(rt, v);
      }
    }
  }

  static void __JS_FreeValue(JSContext ctx, JSValue v)
  {
    __JS_FreeValueRT(ctx.rt, v);
  }

  static void __JS_FreeValueRT(JSRuntime rt, JSValue v)
  {

  }

  /* flags can be JS_PROP_THROW or JS_PROP_THROW_STRICT */
  static int JS_SetPropertyValue(JSContext ctx, final JSValue this_obj,
                                 JSValue prop, JSValue val, int flags) {
    if (JS_VALUE_GET_TAG(this_obj) == JS_TAG_OBJECT &&
      JS_VALUE_GET_TAG(prop) == JS_TAG_INT) {
      return 1;
    } else {
      return on_slow_path(ctx, this_obj, prop, val, flags);
    }
  }

  static JSAtom js_symbol_to_atom(JSContext ctx, JSValue val)
  {
    JSAtomStruct p = (JSAtomStruct) JS_VALUE_GET_PTR(val);
    return js_get_atom_index(ctx.rt, p);
  }

  static JSAtom JS_ValueToAtom(JSContext ctx, final JSValue val)
  {
    JSAtom atom;
    JSTag tag;
    tag = JS_VALUE_GET_TAG(val);
    if (tag == JS_TAG_INT &&
      JS_VALUE_GET_INT(val) <= JS_ATOM_MAX_INT) {
      /* fast path for integer values */
      atom = __JS_AtomFromUInt32(JS_VALUE_GET_INT(val));
    } else if (tag == JS_TAG_SYMBOL) {
      JSAtomStruct p = (JSAtomStruct) JS_VALUE_GET_PTR(val);
      atom = JS_DupAtom(ctx, js_get_atom_index(ctx.rt, p));
    } else {
      JSValue str;
      str = JS_ToPropertyKey(ctx, val);
      if (JS_IsException(str))
        return JS_ATOM_NULL;
      if (JS_VALUE_GET_TAG(str) == JS_TAG_SYMBOL) {
        atom = js_symbol_to_atom(ctx, str);
      } else {
        atom = JS_NewAtomStr(ctx, JS_VALUE_GET_STRING(str));
      }
    }
    return atom;
  }



  static JSValue JS_ToPropertyKey(JSContext ctx, final JSValue val)
  {
    return JS_ToStringInternal(ctx, val, TRUE);
  }

  private static int on_slow_path(JSContext ctx, final JSValue this_obj,
                                  JSValue prop, JSValue val, int flags) {
    JSAtom atom;
    int ret;
    slow_path:
    atom = JS_ValueToAtom(ctx, prop);
    JS_FreeValue(ctx, prop);
    if (atom == JS_ATOM_NULL) {
      JS_FreeValue(ctx, val);
      return -1;
    }
    ret = JS_SetPropertyInternal(ctx, this_obj, atom, val, flags);
    JS_FreeAtom(ctx, atom);
    return ret;
  }

  static int JS_SetPropertyUint32(JSContext ctx, final JSValue this_obj,
                           int idx, JSValue val)
  {
    return JS_SetPropertyValue(ctx, this_obj, JS_NewUint32(ctx, idx), val,
      JS_PROP_THROW);
  }

  static JSValue JS_NewUint32(JSContext ctx, long val)
  {
    JSValue v;
    if (val <= 0x7fffffff) {
      v = JS_NewInt32(ctx, (int) val);
    } else {
      v = __JS_NewFloat64(ctx, val);
    }
    return v;
  }

  static int JS_ToBool(JSContext ctx, final JSValue val)
  {
    return JS_ToBoolFree(ctx, val);
  }

  static JSValue JS_GetGlobalObject(JSContext ctx)
  {
    return JS_DupValue(ctx, ctx.global_obj);
  }

  static JSValue JS_DupValue(JSContext ctx, JSValue v)
  {
    if (JS_VALUE_HAS_REF_COUNT(v)) {
      JSRefCountHeader p = (JSRefCountHeader)JS_VALUE_GET_PTR(v);
      p.ref_count++;
    }
    return (JSValue)v;
  }

  static int JS_SetPropertyStr(JSContext ctx, final JSValue this_obj,
                               final String prop, JSValue val)
  {
    return JS_SetPropertyStr(ctx, this_obj, prop.toCharArray(), val);
  }

  static int JS_SetPropertyStr(JSContext ctx, final JSValue this_obj,
                      final char[] prop, JSValue val)
  {
    JSAtom atom;
    int ret;
    atom = JS_NewAtom(ctx, prop);
    ret = JS_SetPropertyInternal(ctx, this_obj, atom, val, JS_PROP_THROW);
    JS_FreeAtom(ctx, atom);
    return ret;
  }

 static JSValue JS_NewStringLen(JSContext ctx, final char[] buf, int buf_len)
  {
    StringBuffer b = new StringBuffer();
    b.ctx = ctx;
    b.str = new JSString(buf, buf_len);
    b.size = buf_len;
    b.len = buf_len;
    return string_buffer_end(b);
  }

  static JSValue string_buffer_end(StringBuffer s)
  {
    JSString str = s.str;
    return JS_MKPTR(JS_TAG_STRING, str);
  }

  static boolean JS_IsFunction(JSContext ctx, final JSValue val)
  {
    JSObject p;
    if (JS_VALUE_GET_TAG(val) != JS_TAG_OBJECT)
      return FALSE;
    p = JS_VALUE_GET_OBJ(val);
    switch(p.class_id) {
      case JS_CLASS_BYTECODE_FUNCTION:
        return TRUE;
      case JS_CLASS_PROXY:
        return p.proxy_data.is_func;
      default:
        return (ctx.rt.class_array.get(p.class_id.ordinal()).call != null);
    }
  }

  static int JS_IsExtensible(JSContext ctx, final JSValue obj)
  {
    JSObject p;

    if ((JS_VALUE_GET_TAG(obj) != JS_TAG_OBJECT))
      return 0;
    p = JS_VALUE_GET_OBJ(obj);
    if ((p.class_id == JS_CLASS_PROXY))
      return js_proxy_isExtensible(ctx, obj);
    else
      return p.extensible?1:0;
  }

  static int JS_ToArrayLengthFree(JSContext ctx, uint32_t plen,
                                              JSValue val)
  {
    long len;
    JSTag tag;

    redo:
    tag = JS_VALUE_GET_TAG(val);
    switch(tag) {
      case JS_TAG_INT:
      case JS_TAG_BOOL:
      case JS_TAG_NULL:
      {
        int v;
        v = JS_VALUE_GET_INT(val);
        if (v < 0)
               return JS_ToArrayLengthFree_fail(ctx);
        len = v;
      }
      break;

      default:
        if (JS_TAG_IS_FLOAT64(tag)) {
          double d;
          d = JS_VALUE_GET_FLOAT64(val);
          len = (long) d;
          if (len != d) {
            return JS_ToArrayLengthFree_fail(ctx);
          }
        } else {
          val = JS_ToNumberFree(ctx, val);
          if (JS_IsException(val))
            return -1;
          return JS_ToArrayLengthFree(ctx, plen, val);
        }
        break;
    }
    plen.val = len;
    return 0;
  }

  static int JS_ToArrayLengthFree_fail(JSContext ctx) {
    JS_ThrowRangeError(ctx, "invalid array length");
    return -1;
  }
}
