package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSVarDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2020/12/313:47 PM
 */
public class JSVarDef {
  Token name;
  boolean isConst;
  boolean isLexical;
  JSVarScope scope;
  JSVarKindEnum varKind;
}
