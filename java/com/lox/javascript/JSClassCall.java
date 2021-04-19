package com.lox.javascript;

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

}
