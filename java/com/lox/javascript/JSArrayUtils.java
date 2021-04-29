package com.lox.javascript;

import static com.lox.javascript.JSAtom.*;
import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSClassID.*;
import static com.lox.javascript.JSContext.*;
import static com.lox.javascript.JSPropertyUtils.delete_property;
import static com.lox.javascript.JSRuntime.*;
import static com.lox.javascript.JSShape.*;
import static com.lox.javascript.JSShapeProperty.*;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JSToNumber.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;

/**
 * @author benpeng.jiang
 * @title: JSArrayUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/217:51 PM
 */
public class JSArrayUtils {
  public static void on_convert_to_array(JSContext ctx, JSObject p, long idx, int flags) {
    convert_fast_array_to_array(ctx, p);

    on_generic_array(ctx, p, idx, flags);
  }

  private static void on_generic_array(JSContext ctx, JSObject p, long idx, int flags) {
    Pointer<Integer> pplen = new Pointer<>();
    long len;
    JSProperty plen;
    JSShapeProperty pslen;
    plen = p.prop.get(0);
    JS_ToUint32(ctx, pplen, plen.u.value);
    len = pplen.val;
    if ((idx + 1) > len) {
      pslen = get_shape_prop(p.shape)[0];
      if ((pslen.flags & JS_PROP_WRITABLE) == 0)
        JS_ThrowTypeErrorReadOnly(ctx, flags, JS_ATOM_length.toJSAtom());
                    /* XXX: should update the length after defining
                       the property */
      len = idx + 1;
      set_value(ctx, plen.u.value, JS_NewUint32(ctx, len));
      pplen.val = (int)len;
    }
  }
  
  static  int convert_fast_array_to_array(JSContext ctx,
                                          JSObject p)
  {
    JSProperty pr;
    JSShape sh;
    JSValue[] tab;
    int i, len, new_count;

    if (js_shape_prepare_update(ctx, p, null) != 0)
      return -1;
    len = p.u.array.count;
    /* resize the properties once to simplify the error handling */
    sh = p.shape;
    new_count = sh.prop_count + len;
    if (new_count > sh.prop_size) {
      if (resize_properties(ctx, p.shape, p, new_count) != 0)
        return -1;
    }

    tab = p.u.array.u.values;
    for(i = 0; i < len; i++) {
        /* add_property cannot fail here but
           __JS_AtomFromUInt32(i) fails for i > INT32_MAX */
      pr = add_property(ctx, p, __JS_AtomFromUInt32(i), JS_PROP_C_W_E);
      pr.u.value = tab[i];
    }
    js_free(ctx, p.u.array.u.values);
    p.u.array.count = 0;
    p.u.array.u.values = null; /* fail safe */
    p.u.array.u1.size = new uint32_t(0);
    p.fast_array = false;
    return 0;
  }

  static int JS_CreateProperty_exotic(JSContext ctx, JSObject p,
                               JSAtom prop, final JSValue val,
                               final JSValue getter, final JSValue setter,
                               int flags) {
    JSProperty  pr;
    int ret, prop_flags;

    /* add a new property or modify an existing exotic one */
    if (p.is_exotic) {
      if (p.class_id == JS_CLASS_ARRAY) {
        long idx, len;
        uint32_t pidx = new uint32_t(0);

        if (p.fast_array) {
          if (__JS_AtomIsTaggedInt(prop)) {
            idx = __JS_AtomToUInt32(prop);
            if (idx == p.u.array.count) {
              if (!p.extensible)
                           return on_not_extensible(ctx, flags);
              if ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
                on_convert_to_array(ctx, p, idx,  flags);
                return 1;
              }
              prop_flags = get_prop_flags(flags, 0);
              if (prop_flags != JS_PROP_C_W_E){
                on_convert_to_array(ctx, p, idx,  flags);
                return 1;
              }
              return add_fast_array_element(ctx, p,
                JS_DupValue(ctx, val), flags);
            } else {
              on_convert_to_array(ctx, p, idx, flags);
              return 1;
            }
          } else if (JS_AtomIsArrayIndex(ctx,  pidx, prop)){
            idx = pidx.val;
            /* convert the fast array to normal array */
            on_convert_to_array(ctx, p, idx, flags);
          }
        } else if (JS_AtomIsArrayIndex(ctx, pidx, prop)){
          idx = pidx.val;
          on_generic_array(ctx, p, idx, flags);
        }
      } else if (p.class_id.ordinal() >= JS_CLASS_UINT8C_ARRAY.ordinal() &&
        p.class_id.ordinal() <= JS_CLASS_FLOAT64_ARRAY.ordinal()) {
        ret = JS_AtomIsNumericIndex(ctx, prop);
        if (ret != 0) {
          if (ret < 0)
            return -1;
          return JS_ThrowTypeErrorOrFalse(ctx, flags, "cannot create numeric index in typed array");
        }
      } else if ((flags & JS_PROP_NO_EXOTIC) == 0) {
            final JSClassExoticMethods em = ctx.rt.class_array.get(p.class_id.ordinal()).exotic;
        if (em != null) {
          if (!em.define_own_property) {
            return em.define_own_property(ctx, JS_MKPTR(JS_TAG_OBJECT, p),
              prop, val, getter, setter, flags);
          }
          ret = JS_IsExtensible(ctx, JS_MKPTR(JS_TAG_OBJECT, p));
          if (ret < 0)
            return -1;
          if (ret == 0)
            return on_not_extensible(ctx, flags);
        }
      }
    }
    return 0;
  }

  private static int on_not_extensible(JSContext ctx, int flags) {
    return JS_ThrowTypeErrorOrFalse(ctx, flags, "object is not extensible");
  }

  /* Preconditions: 'p' must be of class JS_CLASS_ARRAY, p.fast_array =
   TRUE and p.extensible = TRUE */
  static int add_fast_array_element(JSContext ctx, JSObject p,
                                    JSValue val, int flags)
  {
    uint32_t new_len, array_len;
    /* extend the array by one */
    /* XXX: convert to slow array if new_len > 2^31-1 elements */
    new_len = new uint32_t(p.u.array.count + 1);
    /* update the length if necessary. We assume that if the length is
       not an integer, then if it >= 2^31.  */
    if (JS_VALUE_GET_TAG(p.prop.get(0).u.value) == JS_TAG_INT) {
      array_len = new uint32_t(JS_VALUE_GET_INT(p.prop.get(0).u.value));
      if (new_len.toUnit32() > array_len.toUnit32()) {
        if ((get_shape_prop(p.shape)[0].flags & JS_PROP_WRITABLE) == 0) {
          JS_FreeValue(ctx, val);
          return JS_ThrowTypeErrorReadOnly(ctx, flags, JS_ATOM_length);
        }
        p.prop.get(0).u.value = JS_NewInt32(ctx, new_len.toInt());
      }
    }
    if (new_len.toUnit32() > p.u.array.u1.size.toUnit32()) {
      uint32_t new_size;
      int slack;
      JSValue[] new_array_prop;
      /* XXX: potential arithmetic overflow */
      new_size = new uint32_t(Math.max(new_len.toUnit32(), p.u.array.u1.size.toUnit32() * 3 / 2));
      new_array_prop = js_realloc2(ctx, p.u.array.u.values, new_size.toInt());
      if (new_array_prop == null) {
        JS_FreeValue(ctx, val);
        return -1;
      }
      
      p.u.array.u.values = new_array_prop;
      p.u.array.u1.size = new_size;
    }
    p.u.array.u.values[new_len.toInt() - 1] = val;
    p.u.array.count = new_len.toInt();
    return 1;
  }

  /* set the array length and remove the array elements if necessary. */
  static int set_array_length(JSContext ctx, JSObject p, JSValue val, int flags)
  {
    uint32_t len = new uint32_t(), idx = new uint32_t();
    long cur_len;
    int i, ret;

    /* Note: this call can reallocate the properties of 'p' */
    ret = JS_ToArrayLengthFree(ctx, len, val);
    if (ret != 0)
      return -1;

    if (p.fast_array) {
      uint32_t old_len = new uint32_t(p.u.array.count);
      if (len.val < old_len.val) {
        for(i = (int) len.val; i < old_len.val; i++) {
          JS_FreeValue(ctx, p.u.array.u.values[i]);
        }
        p.u.array.count = len.toInt();
      }
      p.prop.get(0).u.value = JS_NewUint32(ctx, len.toInt());
    } else {
        /* Note: length is always a uint32 because the object is an
           array */
      Pointer<Integer> p_cur_len = new Pointer<>();
      JS_ToUint32(ctx, p_cur_len, p.prop.get(0).u.value);
      cur_len = p_cur_len.val;
      if (len.val < cur_len) {
        uint32_t d;
        JSShape sh;
        JSShapeProperty pr;

        d = new uint32_t(cur_len - len.val);
        sh = p.shape;
        if (d.val <= sh.prop_count) {
          JSAtom atom;

          /* faster to iterate */
          while (cur_len > len.toUnit32()) {
            atom = JS_NewAtomUInt32(ctx, cur_len - 1);
            ret = delete_property(ctx, p, atom);
            JS_FreeAtom(ctx, atom);
            if (ret == 0) {
                        /* unlikely case: property is not
                           configurable */
              break;
            }
            cur_len--;
          }
        } else {
                /* faster to iterate thru all the properties. Need two
                   passes in case one of the property is not
                   configurable */
          cur_len = len.val;
          for(i = 0, pr = get_shape_prop(sh)[0]; i < sh.prop_count;
              i++, pr = sh.prop[i]) {
            if (pr.atom != JS_ATOM_NULL &&
              JS_AtomIsArrayIndex(ctx, idx, pr.atom)) {
              if (idx.toUnit32() >= cur_len &&
                (pr.flags & JS_PROP_CONFIGURABLE) == 0) {
                cur_len = idx.toUnit32() + 1;
              }
            }
          }

          for(i = 0, pr = get_shape_prop(sh)[0]; i < sh.prop_count;
              i++, pr = sh.prop[i]) {
            if (pr.atom != JS_ATOM_NULL &&
              JS_AtomIsArrayIndex(ctx, idx, pr.atom)) {
              if (idx.toUnit32() >= cur_len) {
                /* remove the property */
                delete_property(ctx, p, pr.atom);
                /* WARNING: the shape may have been modified */
                sh = p.shape;
              }
            }
          }
        }
      } else {
        cur_len = len.val;
      }
      set_value(ctx, p.prop.get(0).u.value, JS_NewUint32(ctx, cur_len));
      if ((cur_len > len.val)) {
        return JS_ThrowTypeErrorOrFalse(ctx, flags, "not configurable");
      }
    }
    return 1;
  }

  /* WARNING: 'p' must be a typed array. Works even if the array buffer
     is detached */
  static long typed_array_get_length(JSContext ctx, JSObject p)
  {
    JSTypedArray[] ta = p.u.typed_array;
    int size_log2 = typed_array_size_log2(p.class_id);
    return ta.length >> size_log2;
  }


  static final int JS_TYPED_ARRAY_COUNT = (JS_CLASS_FLOAT64_ARRAY.ordinal() - JS_CLASS_UINT8C_ARRAY.ordinal() + 1);

  static int[] typed_array_size_log2 =  {
    0, 0, 0, 1, 1, 2, 2,
    2, 3
};
  static  int  typed_array_size_log2(JSClassID classID) {
      return typed_array_size_log2[classID.ordinal() - JS_CLASS_UINT8C_ARRAY.ordinal()];
  }
}
