package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSValue
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/222:09 PM
 */
public class JSValue {
  public JSTag tag;
  public Object value;

  public JSValue(JSTag tag, Object value) {
    this.tag = tag;
    this.value = value;
  }

  public boolean JS_IsObject() {
    return JSTag.JS_TAG_OBJECT == tag;
  }

  public static JSValue JS_NewString(JSContext ctx, String str) {
    return JS_NewString(ctx, str.getBytes());
  }

  public static JSValue JS_NewString(JSContext ctx, byte[] buf) {
    return js_new_string8(ctx, buf);
  }

  static JSValue js_new_string8(JSContext ctx, byte[] buf) {
    String value = new String(buf);
    JSString str = new JSString(value);
    return new JSValue(JSTag.JS_TAG_STRING, str);
  }


}
