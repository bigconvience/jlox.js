package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSAtomStruct
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/188:50 PM
 */
public class JSAtomStruct extends JSString{
  public JSAtomStruct(String str) {
    super(str);
  }

  public JSAtomStruct(String str, int atom_type) {
    super(str, atom_type);
  }

  public JSAtomStruct(char[] str, int atom_type) {
    super(str, atom_type);
  }
}
