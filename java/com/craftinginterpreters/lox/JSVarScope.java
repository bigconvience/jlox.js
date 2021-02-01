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
  public JSVarScope prev;
  public JSVarScope next;
  public int level;
  final Map<String, JSVarDef> vars;

  public JSVarScope(JSVarScope prev) {
    vars = new HashMap<>();
    this.prev = prev;
    if (prev != null) {
      level = prev.level + 1;
    } else {
      level = 1;
    }
  }

  public void addVar(JSVarDef varDef) {
    vars.put(varDef.name.lexeme, varDef);
  }

  public JSVarDef get(String name) {
    return vars.get(name);
  }
}
