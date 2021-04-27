package com.lox.javascript;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.lox.javascript.JSCFunction.*;
import static com.lox.javascript.JSClassID.JS_CLASS_OBJECT;
import static com.lox.javascript.JSContext.*;
import static com.lox.javascript.JSStdClassDef.js_std_class_def;
import static com.lox.javascript.JSValue.*;

/**
 * @author benpeng.jiang
 * @title: JSEval
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/512:41 AM
 */
public class LoxJS {
  public static final int JS_EVAL_TYPE_GLOBAL = 0;
  public static final int JS_EVAL_TYPE_MODULE = 1;
  public static final int JS_EVAL_TYPE_DIRECT = 2;
  public static final int JS_EVAL_TYPE_INDIRECT = 3;
  public static final int JS_EVAL_TYPE_MASK = 3;

  public static final int JS_MODE_STRICT = (1 << 0);
  public static final int JS_MODE_STRIP  = (1 << 1);
  public static final int JS_MODE_MATH   = (1 << 2);

  public static int evalFile(JSContext ctx, String filename, boolean module) throws IOException {
    int ret, evalFlags;
    byte[] bytes = Files.readAllBytes(Paths.get(filename));
    if (module) {
      evalFlags = JS_EVAL_TYPE_MODULE;
    } else {
      evalFlags = JS_EVAL_TYPE_GLOBAL;
    }
    ret = eval_buf(ctx, bytes, filename, evalFlags);
    return ret;
  }

  public static int eval_buf(JSContext ctx, byte[] bytes, String filename, int evalFlags) {
    JSValue val;
    int ret;
    String buf = new String(bytes, Charset.defaultCharset());
    val = JS_Eval(ctx, buf, filename, evalFlags);
    ret = 0;
    return ret;
  }

  public static JSValue JS_Eval(JSContext ctx, String input, String filename, int evalFlags) {
    JSValue ret;
    ret = __JS_evalInternal(ctx, ctx.global_obj, input, filename, evalFlags, -1);
    return ret;
  }

  public JSRuntime JS_NewRuntime() {
    return JS_NewRuntime2();
  }

  JSRuntime JS_NewRuntime2() {
    JSRuntime rt = new JSRuntime();
    rt.init_class_range(js_std_class_def, JS_CLASS_OBJECT.ordinal(), js_std_class_def.length);

    return rt;
  }

  public static void JS_DumpMemoryUsage(PrintStream stdout, final JSMemoryUsage s, JSRuntime rt)
  {

  }

  void js_std_add_helpers(JSContext ctx, int argc,String[] argv)
  {
    JSValue global_obj, console, args;
    int i;


    /* XXX: should these global definitions be enumerable? */
    global_obj = JS_GetGlobalObject(ctx);

    console = JS_NewObject(ctx);
    JS_SetPropertyStr(ctx, console, "log",
      JS_NewCFunction(ctx, js_print, "log", 1));
    JS_SetPropertyStr(ctx, global_obj, "console", console);

    /* same methods as the mozilla JS shell */
    if (argc >= 0) {
      args = JS_NewArray(ctx);
      for(i = 0; i < argc; i++) {
        JS_SetPropertyUint32(ctx, args, i, JS_NewString(ctx, argv[i]));
      }
      JS_SetPropertyStr(ctx, global_obj, "scriptArgs", args);
    }

    JS_SetPropertyStr(ctx, global_obj, "print",
      JS_NewCFunction(ctx, js_print, "print", 1));
    JS_SetPropertyStr(ctx, global_obj, "__loadScript",
      JS_NewCFunction(ctx, js_loadScript, "__loadScript", 1));

    JS_FreeValue(ctx, global_obj);
  }

  static boolean is_digit(int c) {
    return c >= '0' && c <= '9';
  }

  /* return TRUE if the string is a number n with 0 <= n <= 2^32-1 */
  static boolean is_num_string(uint32_t pval, final JSString p)
  {
    String str = p.str;
    try {
      pval.val = Long.parseLong(str);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  static boolean is_num_string(Pointer<Integer> pval, final JSString p)
  {
    return false;
  }
}
