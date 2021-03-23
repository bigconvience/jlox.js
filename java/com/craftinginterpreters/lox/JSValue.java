package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.JSTag.*;
import static com.craftinginterpreters.lox.JSThrower.JS_ThrowTypeErrorAtom;
import static com.craftinginterpreters.lox.JS_PROP.*;
import static com.craftinginterpreters.lox.stdio_h.print_atom;
import static com.craftinginterpreters.lox.stdio_h.printf;

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

  JSObject get_proto_obj() {
    if (!JS_IsObject()) {
      return null;
    } else {
      return JS_VALUE_GET_OBJ();
    }
  }

  public boolean JS_IsObject() {
    return JS_TAG_OBJECT == tag;
  }

  boolean JS_IsException()
  {
    return tag == JS_TAG_EXCEPTION;
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

  static JSValue JS_MKVAL(JSTag tag, int val) {
    JSValue v = new JSValue(tag, val);
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
  int JS_SetPropertyInternal(JSContext ctx,
                             JSAtom prop, JSValue val, int flags) {
    JSObject p, p1;
    JSShapeProperty prs;
    JSProperty.Ptr pr = new JSProperty.Ptr();
    JSTag tag;
    JSPropertyDescriptor desc;
    int ret = 0;
    if (false) {
      printf("JS_SetPropertyInternal: ");
      print_atom(ctx, prop);
      printf("\n");
    }

    JSValue this_obj = this;
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

    prs = p.find_own_property(pr, prop);
    if (prs != null) {
      if ((prs.flags & (JS_PROP_TMASK | JS_PROP_WRITABLE |
        JS_PROP_LENGTH)) == JS_PROP_WRITABLE) {
        /* fast case */
        ctx.set_value(pr.value(), val);
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

    JSProperty jsprop = ctx.add_property(p, prop, JS_PROP_C_W_E);
    jsprop.value = val;

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

  JSValue JS_GetPropertyInternal(JSContext ctx,
                                 JSAtom prop, final JSValue this_obj,
                                 int throw_ref_error) {
    JSValue obj = this;
    JSObject  p;
    JSProperty.Ptr  pr = new JSProperty.Ptr();
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
      p = obj.get_proto_obj();
    }

    while (true) {
      prs = p.find_own_property(pr, prop);
      if (prs != null) {
        return pr.value();
      }
      p = p.shape.proto;
      if (p == null) {
        break;
      }
    }
    return JS_UNDEFINED;
  }
}
