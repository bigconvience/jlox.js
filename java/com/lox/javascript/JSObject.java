package com.lox.javascript;

import java.util.ArrayList;
import java.util.List;

/**
 * @author benpeng.jiang
 * @title: JSObject
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/151:47 PM
 */
public class JSObject {
  boolean extensible = true;
  boolean is_exotic;
  JSShape shape;
  final List<JSProperty> prop;
  JSClassID class_id;

  final Func func ;
  static class Func {
    JSFunctionBytecode function_bytecode;
    List<JSVarRef> var_refs;
    JSObject homeObject;
  }

  public JSObject() {
    prop = new ArrayList<>();
    func = new Func();
  }

  JSShapeProperty find_own_property(JSProperty.Ptr ppr, JSAtom atom) {
    JSShape sh;
    JSShapeProperty pr;
    List<JSShapeProperty> prop;
    sh = shape;

    prop = sh.get_shape_property();
    for (int i = 0; i < prop.size(); i++) {
      pr = prop.get(i);
      if (pr.atom.equals(atom)) {
        ppr.setPtr(this.prop.get(i));
        return pr;
      }
    }
    ppr.setPtr(null);
    return null;
  }

  JSShapeProperty find_own_property1(JSAtom atom) {
    JSShape sh;
    List<JSShapeProperty> prop;
    sh = shape;
    prop = sh.get_shape_property();

    for (JSShapeProperty pr: prop) {
      if (pr.atom == atom) {
        return pr;
      }
    }
    return null;
  }

  public void defineProperty(String key, Object value) {
  }

  public Object getValue(String key) {
    return null;
  }

  public void setProp(String key, Object value) {

  }

  @Override
  public String toString() {
    return "JSObject{" +
      "prop=" + prop +
      '}';
  }
}
