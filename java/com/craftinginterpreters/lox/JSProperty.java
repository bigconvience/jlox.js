package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSProperty
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/212:11 AM
 */
public class JSProperty {
  JSValue value;
  GetSet getset;
  JSVarRefWrapper var_ref;

  public JSProperty() {
    this.value = new JSValue(JSTag.JS_TAG_UNDEFINED, null);
  }

  static class GetSet {
    JSObject getter;
    JSObject setter;
  }

  public static class Ptr {
    private JSProperty ptr;

    public Ptr() {
    }

    JSValue value() {
      if (ptr != null) {
        return ptr.value;
      }
      return null;
    }

    public void setPtr(JSProperty ptr) {
      this.ptr = ptr;
    }
  }
}
