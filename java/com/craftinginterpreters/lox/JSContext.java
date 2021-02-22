package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSContext
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/222:00 PM
 */
public class JSContext {
  public JSValue globalObj;

  JSValue evalInternal(JSContext ctx, JSValue thisObject, String input, String filename, int flats, int scope_idx) {
    JSValue retVal = null;

    return retVal;
  }
}
