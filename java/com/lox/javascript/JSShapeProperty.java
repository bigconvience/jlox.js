package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSShapeProperty
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/91:51 PM
 */
public class JSShapeProperty {
  int flags;
  JSAtom atom;
  int hash_next;

  static int js_shape_prepare_update(JSContext ctx, JSObject p,
                                     JSShapeProperty pprs)
  {
    return 0;
  }
}
