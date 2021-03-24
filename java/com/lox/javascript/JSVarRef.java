package com.lox.javascript;

import java.util.List;

/**
 * @author benpeng.jiang
 * @title: JSVarRef
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/31:56 PM
 */
public class JSVarRef {
  JSValue pValue;
  JSValue value;


  public static class PPtr {
    List<JSVarRef> var_refs;
  }

}
