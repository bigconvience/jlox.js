package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSClassExoticMethods
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/241:21 AM
 */
public abstract class JSClassExoticMethods {
  boolean define_own_property;

  abstract int define_own_property(JSContext ctx, final JSValue this_obj,
  JSAtom prop, final JSValue val,
  final JSValue getter, final JSValue setter,
  int flags);
}
