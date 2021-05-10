package com.lox.javascript;

import static com.lox.clibrary.stdlib_h.abort;
import static com.lox.javascript.JSCFunctionEnum.*;
import static com.lox.javascript.JSCFunctionEnum.*;
import static com.lox.javascript.JSThrower.JS_ThrowTypeError;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.LoxJS.*;

/**
 * @author benpeng.jiang
 * @title: JSClassCall
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/199:19 AM
 */
public abstract class JSClassCall {
  abstract JSValue JSClassCall(JSContext ctx, final JSValue func_obj,
                      final JSValue this_val, int argc, final JSValue[] argv,
                      int flags);
  
  public static JSClassCall js_call_c_function = new JSClassCall() {
    @Override
    JSValue JSClassCall(JSContext ctx, JSValue func_obj, JSValue this_val, int argc, JSValue[] argv, int flags) {
      return js_call_c_function(ctx, func_obj, this_val, argc, argv, flags);
    }
  };

  static JSValue js_call_c_function(JSContext ctx, JSValue func_obj,
                                    JSValue this_obj,
                                    int argc, JSValue[] argv, int flags)
  {
    JSRuntime rt = ctx.rt;
    JSCFunctionType func;
    JSObject p;
    JSStackFrame sf_s = new JSStackFrame(), sf = sf_s, prev_sf;
    JSValue ret_val = JS_NULL;
    JSValue[] arg_buf;
    int arg_count, i;
    JSCFunctionEnum cproto;

    p = JS_VALUE_GET_OBJ(func_obj);
    cproto = p.u.cfunc.cproto;
    arg_count = p.u.cfunc.length;

    /* better to always check stack overflow */
//    if (js_check_stack_overflow(rt, sizeof(arg_buf[0]) * arg_count))
//      return JS_ThrowStackOverflow(ctx);

    prev_sf = rt.current_stack_frame;
    sf.prev_frame = prev_sf;
    rt.current_stack_frame = sf;
    ctx = p.u.cfunc.realm; /* change the current realm */
    

    sf.js_mode = 0;
    sf.cur_func = (JSValue)func_obj;
    sf.arg_count = argc;
    arg_buf = argv;

    if ((argc < arg_count)) {
      /* ensure that at least argc_count arguments are readable */
      arg_buf = new JSValue[arg_count];
      for(i = 0; i < argc; i++)
        arg_buf[i] = argv[i];
      for(i = argc; i < arg_count; i++)
        arg_buf[i] = JS_UNDEFINED;
      sf.arg_count = arg_count;
    }
    sf.arg_buf = arg_buf;

    func = p.u.cfunc.c_function;
    switch(cproto) {
      case JS_CFUNC_constructor:
      case JS_CFUNC_constructor_or_func:
        if ((flags & JS_CALL_FLAG_CONSTRUCTOR) == 0) {
          if (cproto == JS_CFUNC_constructor) {
            ret_val = JS_ThrowTypeError(ctx, "must be called with new");
            break;
          } else {
            this_obj = JS_UNDEFINED;
          }
        }
        /* here this_obj is new_target */
        /* fall thru */
      case JS_CFUNC_generic:
        ret_val = func.generic.call(ctx, this_obj, argc, arg_buf);
        break;
      case JS_CFUNC_constructor_magic:
      case JS_CFUNC_constructor_or_func_magic:
        if ((flags & JS_CALL_FLAG_CONSTRUCTOR) == 0) {
          if (cproto == JS_CFUNC_constructor_magic) {
            ret_val = JS_ThrowTypeError(ctx, "must be called with new");
            break;
          } else {
            this_obj = JS_UNDEFINED;
          }
        }
        /* fall thru */
      case JS_CFUNC_generic_magic:
        ret_val = func.generic_magic.call(ctx, this_obj, argc, arg_buf,
          p.u.cfunc.magic);
        break;
//      case JS_CFUNC_getter:
//        ret_val = func.getter(ctx, this_obj);
//        break;
//      case JS_CFUNC_setter:
//        ret_val = func.setter(ctx, this_obj, arg_buf[0]);
//        break;
//      case JS_CFUNC_getter_magic:
//        ret_val = func.getter_magic(ctx, this_obj, p.u.cfunc.magic);
//        break;
//      case JS_CFUNC_setter_magic:
//        ret_val = func.setter_magic(ctx, this_obj, arg_buf[0], p.u.cfunc.magic);
//        break;
//      case JS_CFUNC_f_f:
//      {
//        double d1;
//
//        if ((JS_ToFloat64(ctx, d1, arg_buf[0]))) {
//        ret_val = JS_EXCEPTION;
//        break;
//      }
//        ret_val = JS_NewFloat64(ctx, func.f_f(d1));
//      }
//      break;
//      case JS_CFUNC_f_f_f:
//      {
//        double d1, d2;
//
//        if (unlikely(JS_ToFloat64(ctx, &d1, arg_buf[0]))) {
//        ret_val = JS_EXCEPTION;
//        break;
//      }
//        if (unlikely(JS_ToFloat64(ctx, &d2, arg_buf[1]))) {
//        ret_val = JS_EXCEPTION;
//        break;
//      }
//        ret_val = JS_NewFloat64(ctx, func.f_f_f(d1, d2));
//      }
//      break;
//      case JS_CFUNC_iterator_next:
//      {
//        int done;
//        ret_val = func.iterator_next(ctx, this_obj, argc, arg_buf,
//          &done, p.u.cfunc.magic);
//        if (!JS_IsException(ret_val) && done != 2) {
//          ret_val = js_create_iterator_result(ctx, ret_val, done);
//        }
//      }
//      break;
      default:
        abort();
    }

    rt.current_stack_frame = sf.prev_frame;
    return ret_val;
  }

}
