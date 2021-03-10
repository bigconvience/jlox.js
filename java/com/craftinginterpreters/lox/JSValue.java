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
  public static final JSValue JS_NULL = new JSValue(JSTag.JS_TAG_NULL, 0);
  public static final JSValue JS_UNDEFINED = new JSValue(JSTag.JS_TAG_UNDEFINED, 0);
  public static final JSValue JS_FALSE = new JSValue(JSTag.JS_TAG_BOOL, 0);
  public static final JSValue JS_TRUE = new JSValue(JSTag.JS_TAG_BOOL, 1);
  public static final JSValue JS_EXCEPTION = new JSValue(JSTag.JS_TAG_EXCEPTION, 0);
  public static final JSValue JS_UNINITIALIZED = new JSValue(JSTag.JS_TAG_UNINITIALIZED, 0);

  public JSValue(JSTag tag, Object value) {
    this.tag = tag;
    this.value = value;
  }

  public JSObject JS_VALUE_GET_OBJ() {
    if (value instanceof JSObject) {
      return (JSObject) value;
    }
    return null;
  }

  JSObject get_proto_obj() {
    if (!JS_IsObject()) {
      return null;
    } else {
      return JS_VALUE_GET_OBJ();
    }
  }

  public boolean JS_IsObject() {
    return JSTag.JS_TAG_OBJECT == tag;
  }


  boolean JS_IsUninitialized() {
    return tag == JSTag.JS_TAG_UNINITIALIZED;
  }

  boolean JS_ISString() {
    return tag == JSTag.JS_TAG_STRING;
  }

  public static JSValue JS_NewInt32(JSContext ctx, int val) {
    return new JSValue(JSTag.JS_TAG_INT, val);
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

  void print() {
    System.out.println(value);
  }

  /* return -1 in case of exception or TRUE or FALSE. Warning: 'val' is
   freed by the function. 'flags' is a bitmask of JS_PROP_NO_ADD,
   JS_PROP_THROW or JS_PROP_THROW_STRICT. If JS_PROP_NO_ADD is set,
   the new property is not added and an error is raised. */
  int JS_SetPropertyInternal(JSContext ctx,
                             JSAtom prop, JSValue val, int flags) {
    JSObject  p, p1;
    JSShapeProperty  prs;
    JSProperty  pr;
    int tag;
    JSPropertyDescriptor desc;
    int ret = 0;
    if (true) {
      PrintUtils.printf("JS_SetPropertyInternal: ");
      PrintUtils.print_atom(ctx, prop);
      PrintUtils.printf("\n");
    }

    return ret;
  }
}
