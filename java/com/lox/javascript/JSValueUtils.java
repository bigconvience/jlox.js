package com.lox.javascript;

import static com.lox.clibrary.stdio_h.printf;
import static com.lox.javascript.JSArrayUtils.add_fast_array_element;
import static com.lox.javascript.JSArrayUtils.set_array_length;
import static com.lox.javascript.JSAtom.*;
import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSClassID.*;
import static com.lox.javascript.JSContext.add_property;
import static com.lox.javascript.JSContext.set_value;
import static com.lox.javascript.JSFunctionUtild.call_setter;
import static com.lox.javascript.JSObject.find_own_property;
import static com.lox.javascript.JSProperty.JS_CreateProperty;
import static com.lox.javascript.JSPropertyUtils.JS_AutoInitProperty;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;
import static com.lox.javascript.JUtils.print_atom;

/**
 * @author benpeng.jiang
 * @title: JSValueUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/5/49:28 AM
 */
public class JSValueUtils {
  /* Only works for primitive types, otherwise return JS_NULL. */
  static JSValue JS_GetPrototypePrimitive(JSContext ctx, JSValue val)
  {
    switch(JS_VALUE_GET_NORM_TAG(val)) {
      case JS_TAG_INT:
      case JS_TAG_FLOAT64:
        val = ctx.class_proto[JS_CLASS_NUMBER.ordinal()];
        break;
      case JS_TAG_BOOL:
        val = ctx.class_proto[JS_CLASS_BOOLEAN.ordinal()];
        break;
      case JS_TAG_STRING:
        val = ctx.class_proto[JS_CLASS_STRING.ordinal()];
        break;
      case JS_TAG_SYMBOL:
        val = ctx.class_proto[JS_CLASS_SYMBOL.ordinal()];
        break;
      case JS_TAG_OBJECT:
      case JS_TAG_NULL:
      case JS_TAG_UNDEFINED:
      default:
        val = JS_NULL;
        break;
    }
    return val;
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
          p1 = JS_VALUE_GET_OBJ(JS_GetPrototypePrimitive(ctx, this_obj));
          return prototype_lookup(ctx, this_obj, p1, prop, val, flags);
      }
    }
    p = this_obj.JS_VALUE_GET_OBJ();

    prs = find_own_property(ppr, p, prop);
    if (prs != null) {
      return set_own_property(ctx, this_obj, prop, val, flags);
    }

    p1 = p;
    for (;;) {

      p1 = p1.shape.proto;
      if (p1 == null) {
        break;
      }
      prs = find_own_property(ppr, p1, prop);
      if (prs != null) {
        ret = retry2(ctx, this_obj, p1, prop, val, flags);
        if (ret == -2) {
          break;
        }
        return ret;
      }
    }
    return check_and_add_property(ctx, p, prop, val, flags);
  }

  static int read_only_prop(JSContext ctx, JSAtom prop, JSValue val, int flags) {
    JS_FreeValue(ctx, val);
    return JS_ThrowTypeErrorReadOnly(ctx, flags, prop);
  }

  static int retry2(JSContext ctx, JSValue this_obj, JSObject p1, JSAtom prop, JSValue val, int flags) {
    Pointer<JSProperty> pr = new Pointer<>();
    JSShapeProperty prs = find_own_property(pr, p1, prop);

    if ((prs.flags & JS_PROP_TMASK) == JS_PROP_GETSET) {
      return call_setter(ctx, pr.val.u.getset.setter, this_obj, val, flags);
    } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
      /* Instantiate property and retry (potentially useless) */
      if (JS_AutoInitProperty(ctx, p1, prop, pr.val) != 0)
        return -1;
      return retry2(ctx, this_obj, p1, prop, val, flags);
    } else if ((prs.flags & JS_PROP_WRITABLE) == 0) {
      return read_only_prop(ctx, prop, val, flags);
    } else {
      return -2;
    }
  }

  static int prototype_lookup(JSContext ctx, JSValue this_obj,
                              JSObject p1, JSAtom prop, JSValue val, int flags) {
    if (p1 == null) {
      return check_and_add_property(ctx, this_obj.JS_VALUE_GET_OBJ(), prop, val, flags);
    }
    return retry2(ctx, this_obj, p1, prop, val, flags);
  }

  static int check_and_add_property(JSContext ctx, JSObject p,
                                    JSAtom prop,
                                    JSValue val, int flags) {
    int ret;
    if ((flags & JS_PROP_NO_ADD) != 0) {
      JS_FreeValue(ctx, val);
      JS_ThrowReferenceErrorNotDefined(ctx, prop);
      return -1;
    }
    if (p == null) {
      JS_FreeValue(ctx, val);
      return JS_ThrowTypeErrorOrFalse(ctx, flags, "not an object");
    }

    if ((!p.extensible)) {
      JS_FreeValue(ctx, val);
      return JS_ThrowTypeErrorOrFalse(ctx, flags, "object is not extensible");
    }

    if (p.is_exotic) {
      if (p.class_id == JS_CLASS_ARRAY && p.fast_array &&
        __JS_AtomIsTaggedInt(prop)) {
        int idx = __JS_AtomToUInt32(prop);
        if (idx == p.u.array.count) {
          /* fast case */
          return add_fast_array_element(ctx, p, val, flags);
        } else {
          return generic_create_prop(ctx, p, prop, val, flags);
        }
      } else {
        return generic_create_prop(ctx, p, prop, val, flags);
      }
    }

    JSProperty pr = add_property(ctx, p, prop, JS_PROP_C_W_E);
    if (pr == null) {
      JS_FreeValue(ctx, val);
      return -1;
    }
    pr.u.value = val;
    return 1;
  }

  static int generic_create_prop(JSContext ctx, JSObject p,
                                 JSAtom prop, JSValue val,  int flags) {
    int ret = JS_CreateProperty(ctx, p, prop, val, JS_UNDEFINED, JS_UNDEFINED,
      flags |
        JS_PROP_HAS_VALUE |
        JS_PROP_HAS_ENUMERABLE |
        JS_PROP_HAS_WRITABLE |
        JS_PROP_HAS_CONFIGURABLE |
        JS_PROP_C_W_E);
    JS_FreeValue(ctx, val);
    return ret;
  }

  static int set_own_property(JSContext ctx,
                              JSValue this_obj,
                              JSAtom prop, JSValue val,  int flags) {
    JSObject p = this_obj.JS_VALUE_GET_OBJ();
    Pointer<JSProperty> ppr = new Pointer();
    JSShapeProperty prs = find_own_property(ppr, p, prop);
    JSProperty pr = ppr.val;
      if (((prs.flags & (JS_PROP_TMASK | JS_PROP_WRITABLE |
        JS_PROP_LENGTH)) == JS_PROP_WRITABLE)) {
        /* fast case */
        set_value(ctx, pr.u.value, val);
        return 1;
      } else if ((prs.flags & (JS_PROP_LENGTH | JS_PROP_WRITABLE)) ==
        (JS_PROP_LENGTH | JS_PROP_WRITABLE)) {

        return set_array_length(ctx, p, val, flags);
      } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_GETSET) {
        return call_setter(ctx, pr.u.getset.setter, this_obj, val, flags);
      } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_VARREF) {
            /* JS_PROP_WRITABLE is always true for variable
               references, but they are write protected in module name
               spaces. */
        if (p.class_id == JS_CLASS_MODULE_NS)
          return read_only_prop(ctx, prop, val, flags);
        set_value(ctx, pr.u.var_ref.val.pvalue, val);
        return 1;
      } else if ((prs.flags & JS_PROP_TMASK) == JS_PROP_AUTOINIT) {
        /* Instantiate property and retry (potentially useless) */
        if (JS_AutoInitProperty(ctx, p, prop, pr) != 0) {
          JS_FreeValue(ctx, val);
          return -1;
        }
        return set_own_property(ctx, this_obj, prop, val, flags);
      } else {
        return read_only_prop(ctx, prop, val, flags);
      }
  }

}
