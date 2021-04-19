package com.lox.javascript;

import static com.lox.javascript.JSClassID.JS_CLASS_ARRAY;
import static com.lox.javascript.JSClassID.JS_CLASS_MODULE_NS;
import static com.lox.javascript.JSCompare.js_same_value;
import static com.lox.javascript.JSContext.add_property;
import static com.lox.javascript.JSObject.find_own_property;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.JS_ThrowTypeErrorNotAnObject;
import static com.lox.javascript.JSThrower.JS_ThrowTypeErrorOrFalse;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;
import static java.lang.Boolean.*;

/**
 * @author benpeng.jiang
 * @title: JSProperty
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/212:11 AM
 */
public class JSProperty {
  JSValue value;
  GetSet getset;
  JSVarRefWrapper var_ref;

  public JSProperty() {
    this.value = new JSValue(JSTag.JS_TAG_UNDEFINED, null);
  }

  static class GetSet {
    JSObject getter;
    JSObject setter;
  }

  public static class Ptr {
    private JSProperty ptr;

    public Ptr() {
    }

    JSValue value() {
      if (ptr != null) {
        return ptr.value;
      }
      return null;
    }

    public void setPtr(JSProperty ptr) {
      this.ptr = ptr;
    }
  }


  /* shortcut to add or redefine a new property value */
  static int JS_DefinePropertyValue(JSContext ctx, final JSValue this_obj,
                                    JSAtom prop, JSValue val, int flags)
  {
    int ret;
    ret = JS_DefineProperty(ctx, this_obj, prop, val, JS_UNDEFINED, JS_UNDEFINED,
      flags | JS_PROP_HAS_VALUE | JS_PROP_HAS_CONFIGURABLE | JS_PROP_HAS_WRITABLE | JS_PROP_HAS_ENUMERABLE);
    JS_FreeValue(ctx, val);
    return ret;
  }


  static int JS_DefineProperty(JSContext ctx, final JSValue this_obj,
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
        not_configurable:
        return JS_ThrowTypeErrorOrFalse(ctx, flags, "property is not configurable");
      }

      retry:
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
            if (js_shape_prepare_update(ctx, p, prs))
            return -1;
            /* convert to getset */
            if ((prs.flags & JS_PROP_TMASK) == JS_PROP_VARREF) {
              free_var_ref(ctx.rt, pr.var_ref);
            } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
              /* clear property and update */
              if (js_shape_prepare_update(ctx, p, &prs))
              return -1;
              js_autoinit_free(ctx.rt, pr);
              prs.flags &= ~JS_PROP_TMASK;
              pr.value = JS_UNDEFINED;
                        goto retry;
            } else {
              JS_FreeValue(ctx, pr.value);
            }
            prs.flags = (prs.flags &
              (JS_PROP_CONFIGURABLE | JS_PROP_ENUMERABLE)) |
              JS_PROP_GETSET;
            pr.getset.getter = null;
            pr.getset.setter = null;
          } else {
            if ((prs.flags & JS_PROP_CONFIGURABLE) == 0) {
              if ((flags & JS_PROP_HAS_GET) != 0 &&
                new_getter != pr.getset.getter) {
                            goto not_configurable;
              }
              if ((flags & JS_PROP_HAS_SET) != 0 &&
                new_setter != pr.getset.setter) {
                            goto not_configurable;
              }
            }
          }
          if ((flags & JS_PROP_HAS_GET) != 0) {
            if (pr.getset.getter != null)
              JS_FreeValue(ctx, JS_MKPTR(JS_TAG_OBJECT, pr.getset.getter));
            if (new_getter != null)
              JS_DupValue(ctx, getter);
            pr.getset.getter = new_getter;
          }
          if ((flags & JS_PROP_HAS_SET) != 0) {
            if (pr.getset.setter != null)
              JS_FreeValue(ctx, JS_MKPTR(JS_TAG_OBJECT, pr.getset.setter));
            if (new_setter != null)
              JS_DupValue(ctx, setter);
            pr.getset.setter = new_setter;
          }
        } else {
          if ((prs.flags & JS_PROP_TMASK) == JS_PROP_GETSET) {
            /* convert to data descriptor */
            if (js_shape_prepare_update(ctx, p, &prs))
            return -1;
            if (pr.getset.getter != null)
              JS_FreeValue(ctx, JS_MKPTR(JS_TAG_OBJECT, pr.getset.getter));
            if (pr.getset.setter != null)
              JS_FreeValue(ctx, JS_MKPTR(JS_TAG_OBJECT, pr.getset.setter));
            prs.flags &= ~(JS_PROP_TMASK | JS_PROP_WRITABLE);
            pr.value = JS_UNDEFINED;
          } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_VARREF) {
            /* Note: JS_PROP_VARREF is always writable */
          } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
            /* clear property and update */
            if (js_shape_prepare_update(ctx, p, &prs))
            return -1;
            js_autoinit_free(ctx.rt, pr);
            prs.flags &= ~JS_PROP_TMASK;
            pr.value = JS_UNDEFINED;
          } else {
            if ((prs.flags & (JS_PROP_CONFIGURABLE | JS_PROP_WRITABLE)) == 0 &&
              (flags & JS_PROP_HAS_VALUE) != 0 &&
              !js_same_value(ctx, val, pr.value)) {
                        goto not_configurable;
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
              prs = get_shape_prop(p.shape);
              if (js_update_property_flags(ctx, p, &prs,
              prs.flags & ~(JS_PROP_WRITABLE | JS_PROP_LENGTH)))
              return -1;
            }
            return res;
          } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_VARREF) {
            if ((flags & JS_PROP_HAS_VALUE) != 0) {
              if (p.class_id == JS_CLASS_MODULE_NS) {
                            /* JS_PROP_WRITABLE is always true for variable
                               references, but they are write protected in module name
                               spaces. */
                if (!js_same_value(ctx, val, pr.var_ref.pvalue))
                                goto not_configurable;
              }
              /* update the reference */
              set_value(ctx, pr.var_ref->pvalue,
                JS_DupValue(ctx, val));
            }
                    /* if writable is set to false, no longer a
                       reference (for mapped arguments) */
            if ((flags & (JS_PROP_HAS_WRITABLE | JS_PROP_WRITABLE)) == JS_PROP_HAS_WRITABLE) {
              JSValue val1;
              if (js_shape_prepare_update(ctx, p, &prs))
              return -1;
              val1 = JS_DupValue(ctx, *pr.var_ref->pvalue);
              free_var_ref(ctx.rt, pr.var_ref);
              pr.value = val1;
              prs.flags &= ~(JS_PROP_TMASK | JS_PROP_WRITABLE);
            }
          } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
            /* XXX: should never happen, type was reset above */
            abort();
          } else {
            if ((flags & JS_PROP_HAS_VALUE) != 0) {
              JS_FreeValue(ctx, pr.value);
              pr.value = JS_DupValue(ctx, val);
            }
            if ((flags & JS_PROP_HAS_WRITABLE) != 0) {
              if (js_update_property_flags(ctx, p, &prs,
              (prs.flags & ~JS_PROP_WRITABLE) |
                (flags & JS_PROP_WRITABLE)))
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
      if (js_update_property_flags(ctx, p, &prs,
      (prs.flags & ~mask) | (flags & mask)))
      return -1;
      return TRUE;
    }

    /* handle modification of fast array elements */
    if (p.fast_array) {
      int idx;
      int prop_flags;
      if (p.class_id == JS_CLASS_ARRAY) {
        if (__JS_AtomIsTaggedInt(prop)) {
          idx = __JS_AtomToUInt32(prop);
          if (idx < p.u.array.count) {
            prop_flags = get_prop_flags(flags, JS_PROP_C_W_E);
            if (prop_flags != JS_PROP_C_W_E)
                        goto convert_to_slow_array;
            if (flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) {
              convert_to_slow_array:
              if (convert_fast_array_to_array(ctx, p))
                return -1;
              else
                            goto redo_prop_update;
            }
            if (flags & JS_PROP_HAS_VALUE) {
              set_value(ctx, &p.u.array.u.values[idx], JS_DupValue(ctx, val));
            }
            return TRUE;
          }
        }
      } else if (p.class_id >= JS_CLASS_UINT8C_ARRAY &&
        p.class_id <= JS_CLASS_FLOAT64_ARRAY) {
        JSValue num;
        int ret;

        if (!__JS_AtomIsTaggedInt(prop)) {
          /* slow path with to handle all numeric indexes */
          num = JS_AtomIsNumericIndex1(ctx, prop);
          if (JS_IsUndefined(num))
                    goto typed_array_done;
          if (JS_IsException(num))
            return -1;
          ret = JS_NumberIsInteger(ctx, num);
          if (ret < 0) {
            JS_FreeValue(ctx, num);
            return -1;
          }
          if (!ret) {
            JS_FreeValue(ctx, num);
            return JS_ThrowTypeErrorOrFalse(ctx, flags, "non integer index in typed array");
          }
          ret = JS_NumberIsNegativeOrMinusZero(ctx, num);
          JS_FreeValue(ctx, num);
          if (ret) {
            return JS_ThrowTypeErrorOrFalse(ctx, flags, "negative index in typed array");
          }
          if (!__JS_AtomIsTaggedInt(prop))
                    goto typed_array_oob;
        }
        idx = __JS_AtomToUInt32(prop);
        /* if the typed array is detached, p.u.array.count = 0 */
        if (idx >= typed_array_get_length(ctx, p)) {
          typed_array_oob:
          return JS_ThrowTypeErrorOrFalse(ctx, flags, "out-of-bound index in typed array");
        }
        prop_flags = get_prop_flags(flags, JS_PROP_ENUMERABLE | JS_PROP_WRITABLE);
        if (flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET) ||
          prop_flags != (JS_PROP_ENUMERABLE | JS_PROP_WRITABLE)) {
          return JS_ThrowTypeErrorOrFalse(ctx, flags, "invalid descriptor flags");
        }
        if (flags & JS_PROP_HAS_VALUE) {
          return JS_SetPropertyValue(ctx, this_obj, JS_NewInt32(ctx, idx), JS_DupValue(ctx, val), flags);
        }
        return TRUE;
        typed_array_done: ;
      }
    }

    return JS_CreateProperty(ctx, p, prop, val, getter, setter, flags);
  }

  static boolean check_define_prop_flags(int prop_flags, int flags)
  {
    boolean has_accessor, is_getset;

    if ((prop_flags & JS_PROP_CONFIGURABLE) == 0) {
      if ((flags & (JS_PROP_HAS_CONFIGURABLE | JS_PROP_CONFIGURABLE)) ==
        (JS_PROP_HAS_CONFIGURABLE | JS_PROP_CONFIGURABLE)) {
        return FALSE;
      }
      if ((flags & JS_PROP_HAS_ENUMERABLE) != 0&&
        (flags & JS_PROP_ENUMERABLE) != (prop_flags & JS_PROP_ENUMERABLE))
        return FALSE;
    }
    if ((flags & (JS_PROP_HAS_VALUE | JS_PROP_HAS_WRITABLE |
      JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
      if ((prop_flags & JS_PROP_CONFIGURABLE) == 0) {
        has_accessor = ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0);
        is_getset = ((prop_flags & JS_PROP_TMASK) == JS_PROP_GETSET);
        if (has_accessor != is_getset)
          return FALSE;
        if (!has_accessor && !is_getset && (prop_flags & JS_PROP_WRITABLE) == 0) {
          /* not writable: cannot set the writable bit */
          if ((flags & (JS_PROP_HAS_WRITABLE | JS_PROP_WRITABLE)) ==
            (JS_PROP_HAS_WRITABLE | JS_PROP_WRITABLE))
            return FALSE;
        }
      }
    }
    return TRUE;
  }

  static int JS_CreateProperty(JSContext ctx, JSObject p,
                               JSAtom prop, final JSValue val,
                               final JSValue  getter, final JSValue  setter,
                               int flags)
  {
    JSProperty pr;
    int ret, prop_flags;

    /* add a new property or modify an existing exotic one */
    if (p.is_exotic) {
      if (p.class_id == JS_CLASS_ARRAY) {
        int idx, len;

        if (p.fast_array) {
          if (__JS_AtomIsTaggedInt(prop)) {
            idx = __JS_AtomToUInt32(prop);
            if (idx == p.u.array.count) {
              if (!p.extensible)
                            goto not_extensible;
              if (flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET))
                            goto convert_to_array;
              prop_flags = get_prop_flags(flags, 0);
              if (prop_flags != JS_PROP_C_W_E)
                            goto convert_to_array;
              return add_fast_array_element(ctx, p,
                JS_DupValue(ctx, val), flags);
            } else {
                        goto convert_to_array;
            }
          } else if (JS_AtomIsArrayIndex(ctx, &idx, prop)) {
            /* convert the fast array to normal array */
            convert_to_array:
            if (convert_fast_array_to_array(ctx, p))
              return -1;
                    goto generic_array;
          }
        } else if (JS_AtomIsArrayIndex(ctx, &idx, prop)) {
          JSProperty *plen;
          JSShapeProperty *pslen;
          generic_array:
          /* update the length field */
          plen = &p.prop[0];
          JS_ToUint32(ctx, &len, plen->u.value);
          if ((idx + 1) > len) {
            pslen = get_shape_prop(p.shape);
            if (unlikely(!(pslen->flags & JS_PROP_WRITABLE)))
              return JS_ThrowTypeErrorReadOnly(ctx, flags, JS_ATOM_length);
                    /* XXX: should update the length after defining
                       the property */
            len = idx + 1;
            set_value(ctx, &plen->u.value, JS_NewUint32(ctx, len));
          }
        }
      } else if (p.class_id >= JS_CLASS_UINT8C_ARRAY &&
        p.class_id <= JS_CLASS_FLOAT64_ARRAY) {
        ret = JS_AtomIsNumericIndex(ctx, prop);
        if (ret != 0) {
          if (ret < 0)
            return -1;
          return JS_ThrowTypeErrorOrFalse(ctx, flags, "cannot create numeric index in typed array");
        }
      } else if (!(flags & JS_PROP_NO_EXOTIC)) {
            const JSClassExoticMethods *em = ctx->rt->class_array[p.class_id].exotic;
        if (em) {
          if (em->define_own_property) {
            return em->define_own_property(ctx, JS_MKPTR(JS_TAG_OBJECT, p),
              prop, val, getter, setter, flags);
          }
          ret = JS_IsExtensible(ctx, JS_MKPTR(JS_TAG_OBJECT, p));
          if (ret < 0)
            return -1;
          if (!ret)
                    goto not_extensible;
        }
      }
    }

    if (!p.extensible) {
      not_extensible:
      return JS_ThrowTypeErrorOrFalse(ctx, flags, "object is not extensible");
    }

    if ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
      prop_flags = (flags & (JS_PROP_CONFIGURABLE | JS_PROP_ENUMERABLE)) |
        JS_PROP_GETSET;
    } else {
      prop_flags = flags & JS_PROP_C_W_E;
    }
    pr = add_property(ctx, p, prop, prop_flags);
    if (pr != null)
      return -1;
    if ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
      pr->u.getset.getter = NULL;
      if ((flags & JS_PROP_HAS_GET) && JS_IsFunction(ctx, getter)) {
        pr->u.getset.getter =
          JS_VALUE_GET_OBJ(JS_DupValue(ctx, getter));
      }
      pr->u.getset.setter = null;
      if ((flags & JS_PROP_HAS_SET) && JS_IsFunction(ctx, setter)) {
        pr->u.getset.setter =
          JS_VALUE_GET_OBJ(JS_DupValue(ctx, setter));
      }
    } else {
      if (flags & JS_PROP_HAS_VALUE) {
        pr->u.value = JS_DupValue(ctx, val);
      } else {
        pr->u.value = JS_UNDEFINED;
      }
    }
    return 1;
  }
}
