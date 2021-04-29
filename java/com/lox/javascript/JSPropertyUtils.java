package com.lox.javascript;

import java.util.Arrays;

import static com.lox.clibrary.stdlib_h.abort;
import static com.lox.javascript.JSArrayUtils.*;
import static com.lox.javascript.JSAtom.*;
import static com.lox.javascript.JSClassID.*;
import static com.lox.javascript.JSClassID.JS_CLASS_FLOAT64_ARRAY;
import static com.lox.javascript.JSCompare.js_same_value;
import static com.lox.javascript.JSContext.set_value;
import static com.lox.javascript.JSObject.find_own_property;
import static com.lox.javascript.JSObject.free_property;
import static com.lox.javascript.JSProperty.JS_CreateProperty;
import static com.lox.javascript.JSProperty.check_define_prop_flags;
import static com.lox.javascript.JSRuntime.free_var_ref;
import static com.lox.javascript.JSRuntime.js_autoinit_free;
import static com.lox.javascript.JSShape.get_shape_prop;
import static com.lox.javascript.JSShape.prop_hash_end;
import static com.lox.javascript.JSShapeProperty.js_shape_prepare_update;
import static com.lox.javascript.JSTag.JS_TAG_OBJECT;
import static com.lox.javascript.JSThrower.JS_ThrowTypeErrorNotAnObject;
import static com.lox.javascript.JSThrower.JS_ThrowTypeErrorOrFalse;
import static com.lox.javascript.JSToNumber.JS_NumberIsInteger;
import static com.lox.javascript.JSToNumber.JS_NumberIsNegativeOrMinusZero;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;
import static java.lang.Boolean.TRUE;

/**
 * @author benpeng.jiang
 * @title: JSPropertyUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/241:37 AM
 */
public class JSPropertyUtils {
  /* allowed flags:
   JS_PROP_CONFIGURABLE, JS_PROP_WRITABLE, JS_PROP_ENUMERABLE
   JS_PROP_HAS_GET, JS_PROP_HAS_SET, JS_PROP_HAS_VALUE,
   JS_PROP_HAS_CONFIGURABLE, JS_PROP_HAS_WRITABLE, JS_PROP_HAS_ENUMERABLE,
   JS_PROP_THROW, JS_PROP_NO_EXOTIC.
   If JS_PROP_THROW is set, return an exception instead of FALSE.
   if JS_PROP_NO_EXOTIC is set, do not call the exotic
   define_own_property callback.
   return -1 (exception), FALSE or TRUE.
*/
  public static int JS_DefineProperty(JSContext ctx, final JSValue this_obj,
                               JSAtom prop, final JSValue val,
                               final JSValue getter, final JSValue setter, int flags)
  {
    JSObject p;
    JSShapeProperty prs;
    JSProperty pr;
    PJSProperty ppr = new PJSProperty();
    int mask, res;

    if (JS_VALUE_GET_TAG(this_obj) != JS_TAG_OBJECT) {
      JS_ThrowTypeErrorNotAnObject(ctx);
      return -1;
    }
    p = JS_VALUE_GET_OBJ(this_obj);

    redo_prop_update:
    prs = find_own_property(ppr, p, prop);
    pr = ppr.val;
    if (prs != null) {
      /* property already exists */
      if (!check_define_prop_flags(prs.flags, flags)) {
        return JS_ThrowTypeErrorOrFalse(ctx, flags, "property is not configurable");
      }

      int ret_retry = JS_DefineProperty_handle_retry(ctx, this_obj, prop, val, getter, setter, flags);
      if (ret_retry != 0) {
        return ret_retry;
      }

      /* handle modification of fast array elements */
      int ret_fast_array = JS_DefineProperty_handle_fast_array(ctx, this_obj, prop, val, getter, setter, flags);
      if (ret_fast_array != 0) {
        return ret_fast_array;
      }
    }
    return JS_CreateProperty(ctx, p, prop, val, getter, setter, flags);
  }

  public static int JS_DefineProperty_handle_retry(JSContext ctx, final JSValue this_obj,
                                                        JSAtom prop, final JSValue val,
                                                        final JSValue getter, final JSValue setter, int flags) {
    JSObject p;
    JSShapeProperty prs;
    JSProperty pr;
    PJSProperty ppr = new PJSProperty();
    int mask, res;
    if (JS_VALUE_GET_TAG(this_obj) != JS_TAG_OBJECT) {
      JS_ThrowTypeErrorNotAnObject(ctx);
      return -1;
    }
    p = JS_VALUE_GET_OBJ(this_obj);

    prs = find_own_property(ppr, p, prop);
    pr = ppr.val;

    if ((flags & (JS_PROP_HAS_VALUE | JS_PROP_HAS_WRITABLE |
      JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
      if ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
        JSObject new_getter, new_setter;

        if (JS_IsFunction(ctx, getter)) {
          new_getter = JS_VALUE_GET_OBJ(getter);
        } else {
          new_getter = null;
        }
        if (JS_IsFunction(ctx, setter)) {
          new_setter = JS_VALUE_GET_OBJ(setter);
        } else {
          new_setter = null;
        }

        if ((prs.flags & JS_PROP_TMASK) != JS_PROP_GETSET) {
          if (js_shape_prepare_update(ctx, p, prs) != 0)
            return -1;
          /* convert to getset */
          if ((prs.flags & JS_PROP_TMASK) == JS_PROP_VARREF) {
            free_var_ref(ctx.rt, pr.u.var_ref.val);
          } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
            /* clear property and update */
            if (js_shape_prepare_update(ctx, p, prs) != 0)
              return -1;
            js_autoinit_free(ctx.rt, pr);
            prs.flags &= ~JS_PROP_TMASK;
            pr.u.value = JS_UNDEFINED;
            int ret_retry = JS_DefineProperty_handle_retry(ctx, this_obj, prop, val, getter, setter, flags);
            return ret_retry;
          } else {
            JS_FreeValue(ctx, pr.u.value);
          }
          prs.flags = (prs.flags &
            (JS_PROP_CONFIGURABLE | JS_PROP_ENUMERABLE)) |
            JS_PROP_GETSET;
          pr.u.getset.getter = null;
          pr.u.getset.setter = null;
        } else {
          if ((prs.flags & JS_PROP_CONFIGURABLE) == 0) {
            if ((flags & JS_PROP_HAS_GET) != 0 &&
              new_getter != pr.u.getset.getter) {
              return on_not_configurable(ctx, flags);
            }
            if ((flags & JS_PROP_HAS_SET) != 0 &&
              new_setter != pr.u.getset.setter) {
              return on_not_configurable(ctx, flags);
            }
          }
        }
        if ((flags & JS_PROP_HAS_GET) != 0) {
          if (pr.u.getset.getter != null)
            JS_FreeValue(ctx, JS_MKPTR(JS_TAG_OBJECT, pr.u.getset.getter));
          if (new_getter != null)
            JS_DupValue(ctx, getter);
          pr.u.getset.getter = new_getter;
        }
        if ((flags & JS_PROP_HAS_SET) != 0) {
          if (pr.u.getset.setter != null)
            JS_FreeValue(ctx, JS_MKPTR(JS_TAG_OBJECT, pr.u.getset.setter));
          if (new_setter != null)
            JS_DupValue(ctx, setter);
          pr.u.getset.setter = new_setter;
        }
      } else {
        if ((prs.flags & JS_PROP_TMASK) == JS_PROP_GETSET) {
          /* convert to data descriptor */
          if (js_shape_prepare_update(ctx, p, prs) != 0)
            return -1;
          if (pr.u.getset.getter != null)
            JS_FreeValue(ctx, JS_MKPTR(JS_TAG_OBJECT, pr.u.getset.getter));
          if (pr.u.getset.setter != null)
            JS_FreeValue(ctx, JS_MKPTR(JS_TAG_OBJECT, pr.u.getset.setter));
          prs.flags &= ~(JS_PROP_TMASK | JS_PROP_WRITABLE);
          pr.u.value = JS_UNDEFINED;
        } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_VARREF) {
          /* Note: JS_PROP_VARREF is always writable */
        } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
          /* clear property and update */
          if (js_shape_prepare_update(ctx, p, prs) != 0)
            return -1;
          js_autoinit_free(ctx.rt, pr);
          prs.flags &= ~JS_PROP_TMASK;
          pr.u.value = JS_UNDEFINED;
        } else {
          if ((prs.flags & (JS_PROP_CONFIGURABLE | JS_PROP_WRITABLE)) == 0 &&
            (flags & JS_PROP_HAS_VALUE) != 0 &&
            !js_same_value(ctx, val, pr.u.value)) {
            return on_not_configurable(ctx, flags);
          }
        }
        if ((prs.flags & JS_PROP_LENGTH) != 0) {
          if ((flags & JS_PROP_HAS_VALUE) != 0) {
            res = set_array_length(ctx, p, JS_DupValue(ctx, val),
              flags);
          } else {
            res = 1;
          }
                    /* still need to reset the writable flag if needed.
                       The JS_PROP_LENGTH is reset to have the correct
                       read-only behavior in JS_SetProperty(). */
          if ((flags & (JS_PROP_HAS_WRITABLE | JS_PROP_WRITABLE)) ==
            JS_PROP_HAS_WRITABLE) {
            prs = get_shape_prop(p.shape)[0];
            if (js_update_property_flags(ctx, p, prs,
              prs.flags & ~(JS_PROP_WRITABLE | JS_PROP_LENGTH)) != 0)
              return -1;
          }
          return res;
        } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_VARREF) {
          if ((flags & JS_PROP_HAS_VALUE) != 0) {
            if (p.class_id == JS_CLASS_MODULE_NS) {
                            /* JS_PROP_WRITABLE is always true for variable
                               references, but they are write protected in module name
                               spaces. */
              if (!js_same_value(ctx, val, pr.u.var_ref.val.pvalue))
                return on_not_configurable(ctx, flags);
            }
            /* update the reference */
            set_value(ctx, pr.u.var_ref.val.pvalue,
              JS_DupValue(ctx, val));
          }
                    /* if writable is set to false, no longer a
                       reference (for mapped arguments) */
          if ((flags & (JS_PROP_HAS_WRITABLE | JS_PROP_WRITABLE)) == JS_PROP_HAS_WRITABLE) {
            JSValue val1;
            if (js_shape_prepare_update(ctx, p, prs) != 0)
              return -1;
            val1 = JS_DupValue(ctx, pr.u.var_ref.val.pvalue);
            free_var_ref(ctx.rt, pr.u.var_ref.val);
            pr.u.value = val1;
            prs.flags &= ~(JS_PROP_TMASK | JS_PROP_WRITABLE);
          }
        } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
          /* XXX: should never happen, type was reset above */
          abort();
        } else {
          if ((flags & JS_PROP_HAS_VALUE) != 0) {
            JS_FreeValue(ctx, pr.u.value);
            pr.u.value = JS_DupValue(ctx, val);
          }
          if ((flags & JS_PROP_HAS_WRITABLE) != 0) {
            if (js_update_property_flags(ctx, p, prs,
              (prs.flags & ~JS_PROP_WRITABLE) |
                (flags & JS_PROP_WRITABLE)) != 0)
              return -1;
          }
        }
      }
    }
    mask = 0;
    if ((flags & JS_PROP_HAS_CONFIGURABLE) != 0)
      mask |= JS_PROP_CONFIGURABLE;
    if ((flags & JS_PROP_HAS_ENUMERABLE) != 0)
      mask |= JS_PROP_ENUMERABLE;
    if (js_update_property_flags(ctx, p, prs,
      (prs.flags & ~mask) | (flags & mask)) != 0)
      return -1;
    return 1;
  }

  public static int JS_DefineProperty_handle_fast_array(JSContext ctx, final JSValue this_obj,
                                      JSAtom prop, final JSValue val,
                                      final JSValue getter, final JSValue setter, int flags) {
    JSObject p;
    JSShapeProperty prs;
    JSProperty pr;
    PJSProperty ppr = new PJSProperty();
    int mask, res;

    if (JS_VALUE_GET_TAG(this_obj) != JS_TAG_OBJECT) {
      JS_ThrowTypeErrorNotAnObject(ctx);
      return -1;
    }
    p = JS_VALUE_GET_OBJ(this_obj);


    /* handle modification of fast array elements */
    if (p.fast_array) {
      int idx;
      int prop_flags;
      if (p.class_id == JS_CLASS_ARRAY) {
        if (__JS_AtomIsTaggedInt(prop)) {
          idx = __JS_AtomToUInt32(prop);
          if (idx < p.u.array.count) {
            prop_flags = get_prop_flags(flags, JS_PROP_C_W_E);
            if (prop_flags != JS_PROP_C_W_E) {
              if (convert_fast_array_to_array(ctx, p) != 0)
                return -1;
            }
            if ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
              if (convert_fast_array_to_array(ctx, p) != 0)
                return -1;
              else
                return JS_DefineProperty_handle_retry(ctx, this_obj, prop, val, getter, setter, flags);
            }
            if ((flags & JS_PROP_HAS_VALUE) != 0) {
              set_value(ctx, p.u.array.u.values[idx], JS_DupValue(ctx, val));
            }
            return 1;
          }
        }
      } else if (p.class_id.ordinal() >= JS_CLASS_UINT8C_ARRAY.ordinal() &&
        p.class_id.ordinal() <= JS_CLASS_FLOAT64_ARRAY.ordinal()) {
        JSValue num;
        int ret;

        if (!__JS_AtomIsTaggedInt(prop)) {
          /* slow path with to handle all numeric indexes */
          num = JS_AtomIsNumericIndex1(ctx, prop);
          if (JS_IsUndefined(num))
            return 0;
          if (JS_IsException(num))
            return -1;
          ret = JS_NumberIsInteger(ctx, num);
          if (ret < 0) {
            JS_FreeValue(ctx, num);
            return -1;
          }
          if (ret != 0) {
            JS_FreeValue(ctx, num);
            return JS_ThrowTypeErrorOrFalse(ctx, flags, "non integer index in typed array");
          }
          ret = JS_NumberIsNegativeOrMinusZero(ctx, num) ? 1 : 0;
          JS_FreeValue(ctx, num);
          if (ret != 0) {
            return JS_ThrowTypeErrorOrFalse(ctx, flags, "negative index in typed array");
          }
          if (!__JS_AtomIsTaggedInt(prop))
            return JS_ThrowTypeErrorOrFalse(ctx, flags, "out-of-bound index in typed array");
        }
        idx = __JS_AtomToUInt32(prop);
        /* if the typed array is detached, p.u.array.count = 0 */
        if (idx >= typed_array_get_length(ctx, p)) {
          return JS_ThrowTypeErrorOrFalse(ctx, flags, "out-of-bound index in typed array");
        }
        prop_flags = get_prop_flags(flags, JS_PROP_ENUMERABLE | JS_PROP_WRITABLE);
        if ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0 ||
          prop_flags != (JS_PROP_ENUMERABLE | JS_PROP_WRITABLE)) {
          return JS_ThrowTypeErrorOrFalse(ctx, flags, "invalid descriptor flags");
        }
        if ((flags & JS_PROP_HAS_VALUE) != 0) {
          return JS_SetPropertyValue(ctx, this_obj, JS_NewInt32(ctx, idx), JS_DupValue(ctx, val), flags);
        }
        return 1;
      }
    }
    return 0;
  }


  private static int on_not_configurable(JSContext ctx, int flags) {
    return JS_ThrowTypeErrorOrFalse(ctx, flags, "property is not configurable");
  }

  static int delete_property(JSContext ctx, JSObject p, JSAtom atom)
  {
    JSShape sh;
    JSShapeProperty pr, lpr;
    JSShapeProperty[] prop;
    JSProperty pr1;
    int lpr_idx;
    int h, h1;

    sh = p.shape;
    h1 = atom.getVal() & sh.prop_hash_mask;
    h = prop_hash_end(sh)[h1];
    prop = get_shape_prop(sh);
    lpr = null;
    lpr_idx = 0;   /* prevent warning */
    while (h != 0) {
      pr = prop[h - 1];
      if ((pr.atom == atom)) {
        /* found ! */
        if ((pr.flags & JS_PROP_CONFIGURABLE) == 0)
          return 0;
        /* realloc the shape if needed */
        if (lpr != null)
          lpr_idx = Arrays.asList(get_shape_prop(sh)).indexOf(lpr_idx);
        if (js_shape_prepare_update(ctx, p, pr) != 0)
          return -1;
        sh = p.shape;
        /* remove property */
        if (lpr != null) {
          lpr = get_shape_prop(sh)[lpr_idx];
          lpr.hash_next = pr.hash_next;
        } else {
          prop_hash_end(sh)[h1 - 1] = pr.hash_next;
        }
        sh.deleted_prop_count++;
        /* free the entry */
        pr1 = p.prop.get(h - 1);
        free_property(ctx.rt, pr1, pr.flags);
        JS_FreeAtom(ctx, pr.atom);
        /* put default values */
        pr.flags = 0;
        pr.atom = JS_ATOM_NULL;
        pr1.u.value = JS_UNDEFINED;


        return 1;
      }
      lpr = pr;
      h = pr.hash_next;
    }

    if (p.is_exotic) {
      if (p.fast_array) {
        uint32_t idx = new uint32_t();
        if (JS_AtomIsArrayIndex(ctx, idx, atom) &&
        idx.toInt() < p.u.array.count) {
          if (p.class_id == JS_CLASS_ARRAY ||
            p.class_id == JS_CLASS_ARGUMENTS) {
            /* Special case deleting the last element of a fast Array */
            if (idx.toInt() == p.u.array.count - 1) {
              JS_FreeValue(ctx, p.u.array.u.values[idx.toInt()]);
              p.u.array.count = idx.toInt();
              return 1;
            }
            if (convert_fast_array_to_array(ctx, p) != 0)
              return -1;
            return delete_property(ctx, p, atom);
          } else {
            return 0; /* not configurable */
          }
        }
      } else {
             JSClassExoticMethods em = ctx.rt.class_array.get(p.class_id.ordinal()).exotic;
        if (em != null && em.delete_property) {
          return em.delete_property(ctx, JS_MKPTR(JS_TAG_OBJECT, p), atom);
        }
      }
    }
    /* not found */
    return 1;
  }

  static int js_update_property_flags(JSContext ctx, JSObject p,
                                      JSShapeProperty pprs, int flags)
  {
    if (flags != pprs.flags) {
    if (js_shape_prepare_update(ctx, p, pprs) != 0)
      return -1;
    pprs.flags = flags;
  }
    return 0;
  }

}
