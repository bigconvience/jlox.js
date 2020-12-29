package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: Utils
 * @projectName LoxScript
 * @description: TODO
 * @date 2020/12/299:37 PM
 */
public class Utils {
  public static int toInt(Object value) {
    if (value instanceof Double) {
      return ((Double) value).intValue();
    }
    throw new RuntimeException("need number");
  }
}
