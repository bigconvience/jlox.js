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


  static class GetSet {
    JSObject getter;
    JSObject setter;
  }

  public static class Ptr {
    JSProperty ptr;
  }
}
