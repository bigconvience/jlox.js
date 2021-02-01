package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * @author benpeng.jiang
 * @title: JSObject
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/151:47 PM
 */
public class JSObject {
  Map<String, Object> prop;

  public JSObject() {
    prop = new HashMap<>();
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
