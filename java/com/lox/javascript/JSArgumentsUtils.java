package com.lox.javascript;

import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSClassID.*;
import static com.lox.javascript.JSContext.*;
import static com.lox.javascript.JSProperty.*;
import static com.lox.javascript.JSPropertyUtils.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;

/**
 * @author benpeng.jiang
 * @title: JSArgumentsUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/5/182:17 PM
 */
public class JSArgumentsUtils {
  static JSValue js_build_arguments(JSContext ctx, int argc, JSValue[] argv)
  {
    JSValue val;
    JSValue[] tab;
    JSProperty pr;
    JSObject p;
    int i;

    val = JS_NewObjectProtoClass(ctx, ctx.class_proto[JS_CLASS_OBJECT.ordinal()],
      JS_CLASS_ARGUMENTS);
    if (JS_IsException(val))
      return val;
    p = JS_VALUE_GET_OBJ(val);

    /* add the length field (cannot fail) */
    pr = add_property(ctx, p, JS_ATOM_length.toJSAtom(),
      JS_PROP_WRITABLE | JS_PROP_CONFIGURABLE);
    pr.u.value = JS_NewInt32(ctx, argc);

    /* initialize the fast array part */
    tab = null;
    if (argc > 0) {
      tab = new JSValue[argc];
      if (tab == null) {
        JS_FreeValue(ctx, val);
        return JS_EXCEPTION;
      }
      for(i = 0; i < argc; i++) {
        tab[i] = JS_DupValue(ctx, argv[i]);
      }
    }
    p.u.array.u.values = tab;
    p.u.array.count = argc;

    JS_DefinePropertyValue(ctx, val, JS_ATOM_Symbol_iterator,
      JS_DupValue(ctx, ctx.array_proto_values),
      JS_PROP_CONFIGURABLE | JS_PROP_WRITABLE);
    /* add callee property to throw a TypeError in strict mode */
    JS_DefineProperty(ctx, val, JS_ATOM_callee, JS_UNDEFINED,
      ctx.throw_type_error, ctx.throw_type_error,
      JS_PROP_HAS_GET | JS_PROP_HAS_SET);
    return val;
  }

}
