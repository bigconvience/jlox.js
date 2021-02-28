package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSString
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/277:33 PM
 */
public class JSString {
  final  int atom_type;
  final  String str;

  public JSString(String str, int atom_type) {
    this.atom_type = atom_type;
    this.str = str;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof JSString) {
      JSString anotherJSString = (JSString) obj;
      if (this.atom_type == anotherJSString.atom_type) {
        if (this.str != null && anotherJSString.str != null) {
          return this.str.equals(anotherJSString.str);
        } if (this.str == null && anotherJSString.str == null) {
          return true;
        }
        return false;
      }
    }
    return false;
  }
}
