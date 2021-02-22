package com.craftinginterpreters.lox;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author benpeng.jiang
 * @title: JSEval
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/512:41 AM
 */
public class JSEvaluator {
  public static final int JS_EVAL_TYPE_GLOBAL = 0;
  public static final int JS_EVAL_TYPE_MODULE = 1;
  public static final int JS_EVAL_TYPE_DIRECT = 2;
  public static final int JS_EVAL_TYPE_INDIRECT = 3;
  public static final int JS_EVAL_TYPE_MASK = 3;

  public static int evalFile(JSContext ctx, String filename, boolean module) throws IOException {
    int ret, evalFlags;
    byte[] bytes = Files.readAllBytes(Paths.get(filename));
    if (module) {
      evalFlags = JS_EVAL_TYPE_MODULE;
    } else {
      evalFlags = JS_EVAL_TYPE_GLOBAL;
    }
    ret = evalBuf(ctx, bytes, filename, evalFlags);
    return ret;
  }

  public static int evalBuf(JSContext ctx, byte[] bytes, String filename, int evalFlags) {
    JSValue val;
    int ret;
    String buf = new String(bytes, Charset.defaultCharset());
    val = JSEval(ctx, buf, filename, evalFlags);
    ret = 0;
    return ret;
  }

  public static JSValue JSEval(JSContext ctx, String input, String filename, int evalFlags) {
    JSValue ret;
    ret = JSEvalInternal(ctx, ctx.globalObj, input, filename, evalFlags, -1);
    return ret;
  }

  public static JSValue JSEvalInternal(JSContext ctx, JSValue thisObject, String input, String filename, int flags, int scope_idx) {
    return ctx.evalInternal(ctx, thisObject, input, filename, flags, scope_idx);
  }

}
