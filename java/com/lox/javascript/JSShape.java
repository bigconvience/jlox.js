package com.lox.javascript;

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
  final JSRefCountHeader header = new JSRefCountHeader();
  JSObject proto;
  final List<JSShapeProperty> prop;

  public JSShape() {
    prop = new ArrayList<>();
  }

  List<JSShapeProperty> get_shape_property() {
    return prop;
  }

  static JSShape js_dup_shape(JSShape sh)
  {
    sh.header.ref_count++;
    return sh;
  }
}
