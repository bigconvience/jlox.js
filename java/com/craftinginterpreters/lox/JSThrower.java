package com.craftinginterpreters.lox;

import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.JSAtom.JS_ATOM_NULL;
import static com.craftinginterpreters.lox.JSClassID.JS_CLASS_OBJECT;
import static com.craftinginterpreters.lox.JSErrorEnum.*;
import static com.craftinginterpreters.lox.JSValue.JS_EXCEPTION;
import static com.craftinginterpreters.lox.JS_PROP.JS_PROP_THROW;
import static com.craftinginterpreters.lox.JS_PROP.JS_PROP_THROW_STRICT;

/**
 * @author benpeng.jiang
 * @title: JS_Throw
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/108:58 PM
 */
public class JSThrower {

  static int JS_ThrowTypeErrorReadOnly(JSContext ctx, int flags, JSAtom atom) {
    if ((flags & JS_PROP_THROW) != 0 ||
      ((flags & JS_PROP_THROW_STRICT) != 0 && ctx.is_strict_mode())) {
      JS_ThrowTypeErrorAtom(ctx, "'%s' is read-only", atom);
      return -1;
    } else {
      return 0;
    }
  }

  static JSValue JS_ThrowTypeErrorAtom(JSContext ctx, String fmt, JSAtom atom) {
    return __JS_ThrowTypeErrorAtom(ctx, atom, fmt, "");
  }

  private static JSValue __JS_ThrowTypeErrorAtom(JSContext ctx, JSAtom atom, String fmt, Object... args) {
    return JS_ThrowTypeError(ctx, fmt, ctx.JS_AtomGetStr(atom));
  }

  static JSValue  JS_ThrowTypeError(JSContext ctx, String fmt, Object... args)
  {
    JSValue val;

    val = JS_ThrowError(ctx, JS_TYPE_ERROR, fmt, Arrays.asList(args));
    return val;
  }

  static JSValue JS_ThrowReferenceErrorUninitialized(JSContext ctx, JSAtom name) {
    return JS_ThrowReferenceError(ctx, "%s is not initialized",
      name == JS_ATOM_NULL ? "lexical variable" : ctx.JS_AtomGetStr(name));
  }

  static JSValue JS_ThrowReferenceError(JSContext ctx, String fmt, Object... args) {
    JSValue val;
    val = JS_ThrowError(ctx, JSErrorEnum.JS_REFERENCE_ERROR, fmt, Arrays.asList(args));
    return val;
  }

  static JSValue JS_ThrowError(JSContext ctx, JSErrorEnum error_num,
                               String fmt, List<Object> ap) {

    JSStackFrame sf = ctx.rt.current_stack_frame;

    boolean add_backtrace = false;
    return JS_ThrowError2(ctx, error_num, fmt, ap, add_backtrace);
  }

  static JSValue JS_ThrowError2(JSContext ctx, JSErrorEnum error_num,
                                String fmt, List<Object> ap, boolean add_backtrace) {
    JSValue obj, ret;

    obj = ctx.JS_NewObjectClass(JS_CLASS_OBJECT);
    if (obj != null) {
      throw new RuntimeException(String.format(fmt, ap.toArray()));
    }

//    if (unlikely(JS_IsException(obj))) {
//      /* out of memory: throw JS_NULL to avoid recursing */
//      obj = JS_NULL;
//    } else {
//      ctx.JS_DefinePropertyValue(ctx, obj, JS_ATOM_message,
//        JS_NewString(ctx, buf),
//        JS_PROP_WRITABLE | JS_PROP_CONFIGURABLE);
//    }
//    if (add_backtrace) {
//
//    }
    ret = JS_Throw(ctx, obj);
    return ret;
  }


  static JSValue JS_Throw(JSContext ctx, JSValue obj) {
    return JS_EXCEPTION;
  }

  static JSValue JS_ThrowInternalError(JSContext ctx, String fmt, Object... args) {
    return JS_ThrowError(ctx, JS_INTERNAL_ERROR, fmt, Arrays.asList(args));
  }

  static JSValue JS_ThrowSyntaxErrorVarRedeclaration(JSContext ctx, JSAtom prop) {
    return JS_ThrowSyntaxErrorAtom(ctx, "redeclaration of '%s'", prop);
  }

  static JSValue JS_ThrowSyntaxErrorAtom(JSContext ctx, String fmt, JSAtom atom) {
    return __JS_ThrowSyntaxErrorAtom(ctx, atom, fmt, "");
  }

  private static JSValue __JS_ThrowSyntaxErrorAtom(JSContext ctx, JSAtom atom, String fmt, Object... args) {
    return JS_ThrowSyntaxError(ctx, fmt, ctx.JS_AtomGetStr(atom));
  }

  static JSValue JS_ThrowSyntaxError(JSContext ctx, String fmt, Object... args) {
    JSValue val;

    val = JS_ThrowError(ctx, JS_SYNTAX_ERROR, fmt, Arrays.asList(args));
    return val;
  }
}
