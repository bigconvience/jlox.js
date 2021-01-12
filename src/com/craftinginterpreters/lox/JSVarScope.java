package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * @author benpeng.jiang
 * @title: JSVarScope
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/69:53 AM
 */
public class JSVarScope {
  int parent;
  final Map<String, JSVarDef> vars;

  public JSVarScope() {
    parent = -1;
    vars = new HashMap<>();
  }
}
