package com.lox.javascript;

import static com.lox.javascript.JSArrayUtils.JS_CreateProperty_exotic;
import static com.lox.javascript.JSArrayUtils.convert_fast_array_to_array;
import static com.lox.javascript.JSAtom.*;
import static com.lox.javascript.JSAtomEnum.JS_ATOM_length;
import static com.lox.javascript.JSClassID.*;
import static com.lox.javascript.JSCompare.*;
import static com.lox.javascript.JSContext.*;
import static com.lox.javascript.JSObject.*;
import static com.lox.javascript.JSPropertyUtils.JS_DefineProperty;
import static com.lox.javascript.JSProxy.js_proxy_isExtensible;
import static com.lox.javascript.JSRuntime.js_free;
import static com.lox.javascript.JSShape.get_shape_prop;
import static com.lox.javascript.JSShapeProperty.js_shape_prepare_update;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JSToNumber.JS_ToUint32;
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
  U u;

  static class U {
    JSValue value;
    GetSet getset;
    Pointer<JSVarRef> var_ref;
    Init init;

    public U() {
      this.value = new JSValue(JSTag.JS_TAG_UNDEFINED, null);
      init = new Init();
    }

    static class  Init {            /* JS_PROP_AUTOINIT */
            /* in order to use only 2 pointers, we compress the realm
               and the init function pointer */
      int realm_and_id; /* realm and init_id (JS_AUTOINIT_ID_x)
                                       in the 2 low bits */
      Object opaque;
    } ;
  }

  public JSProperty() {
    u = new U();
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
        return ptr.u.value;
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
    JS_CreateProperty_exotic(ctx, p, prop, val, getter, setter, flags);
    if (!p.extensible) {
      return JS_ThrowTypeErrorOrFalse(ctx, flags, "object is not extensible");
    }

    if ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
      prop_flags = (flags & (JS_PROP_CONFIGURABLE | JS_PROP_ENUMERABLE)) |
        JS_PROP_GETSET;
    } else {
      prop_flags = flags & JS_PROP_C_W_E;
    }
    pr = add_property(ctx, p, prop, prop_flags);
    if (pr == null)
      return -1;
    if ((flags & (JS_PROP_HAS_GET | JS_PROP_HAS_SET)) != 0) {
      pr.u.getset.getter = null;
      if ((flags & JS_PROP_HAS_GET) != 0 && JS_IsFunction(ctx, getter)) {
        pr.u.getset.getter =
          JS_VALUE_GET_OBJ(JS_DupValue(ctx, getter));
      }
      pr.u.getset.setter = null;
      if ((flags & JS_PROP_HAS_SET) != 0 && JS_IsFunction(ctx, setter)) {
        pr.u.getset.setter =
          JS_VALUE_GET_OBJ(JS_DupValue(ctx, setter));
      }
    } else {
      if ((flags & JS_PROP_HAS_VALUE) != 0) {
        pr.u.value = JS_DupValue(ctx, val);
      } else {
        pr.u.value = JS_UNDEFINED;
      }
    }
    return 1;
  }



}
