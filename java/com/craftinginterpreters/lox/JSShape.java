package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

/**
 * @author benpeng.jiang
 * @title: JSShape
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/42:22 PM
 */
public class JSShape {
  JSObject proto;
  final List<JSShapeProperty> prop;

  public JSShape() {
    prop = new ArrayList<>();
  }

  List<JSShapeProperty> get_shape_property() {
    return prop;
  }
}
