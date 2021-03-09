package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author benpeng.jiang
 * @title: JSObject
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/151:47 PM
 */
public class JSObject {
  boolean extensible;
  boolean is_exotic;
  JSShape shape;
  Map<String, Object> prop;
  JSClassID JSClassID;

  Func func ;
  static class Func {
    JSFunctionBytecode function_bytecode;
    List<JSVarRef> var_refs;
    JSObject homeObject;
  }

  public JSObject() {
    prop = new HashMap<>();
    func = new Func();
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
    prop.put(key, value);
  }

  public Object getValue(String key) {
    return prop.get(key);
  }

  public void setProp(String key, Object value) {
    prop.put(key, value);
  }

  @Override
  public String toString() {
    return "JSObject{" +
      "prop=" + prop +
      '}';
  }
}
