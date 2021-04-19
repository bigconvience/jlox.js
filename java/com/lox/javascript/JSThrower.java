package com.lox.javascript;

import com.lox.clibrary.stdio_h;
import com.lox.clibrary.stdlib_h;

import java.util.Arrays;
import java.util.List;

import static com.lox.javascript.JSAtom.JS_ATOM_NULL;
import static com.lox.javascript.JSClassID.JS_CLASS_ERROR;
import static com.lox.javascript.JSContext.JS_NewObjectClass;
import static com.lox.javascript.JSContext.is_strict_mode;
import static com.lox.javascript.JSErrorEnum.*;
import static com.lox.javascript.JSTag.JS_TAG_OBJECT;
import static com.lox.javascript.JSValue.JS_VALUE_GET_OBJ;
import static com.lox.javascript.JSValue.JS_VALUE_GET_TAG;
import static com.lox.javascript.JS_PROP.JS_PROP_THROW;
import static com.lox.javascript.JS_PROP.JS_PROP_THROW_STRICT;

/**
 * @author benpeng.jiang
 * @title: JS_Throw
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/108:58 PM
 */
public class JSThrower {
  public static final int JS_THROW_VAR_RO = 0;
  public static final int JS_THROW_VAR_REDECL = 1;
  public static final int JS_THROW_VAR_UNINITIALIZED = 2;
  public static final int JS_THROW_VAR_DELETE_SUPER = 3;

  public static int js_parse_error(Resolver s, String fmt, Object... args) {
    JSContext ctx = s.ctx;

    if (Config.loxTest) {
      stdio_h.perror("[line %d] Error ", s.last_line_num);
    }
    JS_ThrowError2(ctx, JS_SYNTAX_ERROR, fmt, Arrays.asList(args), false);
    return -1;
  }

  public static int JS_ThrowTypeErrorReadOnly(JSContext ctx, int flags, JSAtom atom) {
    if ((flags & JS_PROP_THROW) != 0 ||
      ((flags & JS_PROP_THROW_STRICT) != 0 && is_strict_mode(ctx))) {
      JS_ThrowTypeErrorAtom(ctx, "'%s' is read-only", atom);
      return -1;
    } else {
      return 0;
    }
  }

  public static JSValue JS_ThrowTypeErrorNotAnObject(JSContext ctx)
  {
    return JS_ThrowTypeError(ctx, "not an object");
  }

  public static JSValue JS_ThrowTypeErrorAtom(JSContext ctx, String fmt, JSAtom atom) {
    return __JS_ThrowTypeErrorAtom(ctx, atom, fmt, "");
  }

  private static JSValue __JS_ThrowTypeErrorAtom(JSContext ctx, JSAtom atom, String fmt, Object... args) {
    return JS_ThrowTypeError(ctx, fmt, ctx.JS_AtomGetStr(atom));
  }

  public static JSValue JS_ThrowTypeError(JSContext ctx, String fmt, Object... args) {
    JSValue val;

    val = JS_ThrowError(ctx, JS_TYPE_ERROR, fmt, Arrays.asList(args));
    return val;
  }

  public static JSValue JS_ThrowReferenceErrorUninitialized(JSContext ctx, JSAtom name) {
    return JS_ThrowReferenceError(ctx, "%s is not initialized",
      name == JS_ATOM_NULL ? "lexical variable" : ctx.JS_AtomGetStr(name));
  }

  public static JSValue JS_ThrowReferenceError(JSContext ctx, String fmt, Object... args) {
    JSValue val;
    val = JS_ThrowError(ctx, JSErrorEnum.JS_REFERENCE_ERROR, fmt, Arrays.asList(args));
    return val;
  }

  public static JSValue JS_ThrowError(JSContext ctx, JSErrorEnum error_num,
                                      String fmt, List<Object> ap) {

    JSStackFrame sf = ctx.rt.current_stack_frame;

    boolean add_backtrace = false;
    return JS_ThrowError2(ctx, error_num, fmt, ap, add_backtrace);
  }

  static int JS_ThrowTypeErrorOrFalse(JSContext ctx, int flags, String fmt, Object... args)
  {
    if ((flags & JS_PROP_THROW) != 0 ||
      ((flags & JS_PROP_THROW_STRICT) != 0 && is_strict_mode(ctx))) {
      JS_ThrowError(ctx, JS_TYPE_ERROR, fmt, Arrays.asList(args));

      return -1;
    } else {
      return 0;
    }
  }

  public static JSValue JS_ThrowError2(JSContext ctx, JSErrorEnum error_num,
                                       String fmt, List<Object> ap, boolean add_backtrace) {
    JSValue obj, ret;

    obj = JS_NewObjectClass(ctx, JSClassID.JS_CLASS_OBJECT);
    if (obj != null) {

      if (error_num != null) {
        stdio_h.perror("%s: ", error_num.toString());
      }
      stdio_h.perror(fmt + "\n", ap.toArray());

      stdlib_h.abort();
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


  public static JSValue JS_Throw(JSContext ctx, JSValue obj) {
    return JSValue.JS_EXCEPTION;
  }

  public static JSValue JS_ThrowInternalError(JSContext ctx, String fmt, Object... args) {
    return JS_ThrowError(ctx, JS_INTERNAL_ERROR, fmt, Arrays.asList(args));
  }

  public static JSValue JS_ThrowSyntaxErrorVarRedeclaration(JSContext ctx, JSAtom prop) {
    return JS_ThrowSyntaxErrorAtom(ctx, "redeclaration of '%s'", prop);
  }

  public static JSValue JS_ThrowSyntaxErrorAtom(JSContext ctx, String fmt, JSAtom atom) {
    return __JS_ThrowSyntaxErrorAtom(ctx, atom, fmt, "");
  }

  private static JSValue __JS_ThrowSyntaxErrorAtom(JSContext ctx, JSAtom atom, String fmt, Object... args) {
    return JS_ThrowSyntaxError(ctx, fmt, ctx.JS_AtomGetStr(atom));
  }

  public static JSValue JS_ThrowSyntaxError(JSContext ctx, String fmt, Object... args) {
    JSValue val;

    val = JS_ThrowError(ctx, JS_SYNTAX_ERROR, fmt, Arrays.asList(args));
    return val;
  }

  public static void JS_SetUncatchableError(JSContext ctx, final JSValue val, boolean flag)
  {
    JSObject p;
    if (JS_VALUE_GET_TAG(val) != JS_TAG_OBJECT)
      return;
    p = JS_VALUE_GET_OBJ(val);
    if (p.class_id == JS_CLASS_ERROR)
      p.is_uncatchable_error = flag;
  }
}
