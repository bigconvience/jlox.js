package com.lox.javascript;

import java.util.Objects;

/**
 * @author benpeng.jiang
 * @title: JSString
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/277:33 PM
 */
public class JSString {
  final JSRefCountHeader header = new JSRefCountHeader();
  final int atom_type;
  final String str;

  public JSString(String str) {
    atom_type = JSAtom.JS_ATOM_TYPE_STRING;
    this.str = str;
  }

  public JSString(String str, int atom_type) {
    this.atom_type = atom_type;
    this.str = str;
  }

  public JSString(char[] str, int atom_type) {
    this.atom_type = atom_type;
    this.str = new String(str);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atom_type, str);
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
        }
        if (this.str == null && anotherJSString.str == null) {
          return true;
        }
        return false;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return str;
  }


  static int js_string_compare(JSContext ctx,
                             final JSString p1, final JSString p2)
  {
    return p1.equals(p2) ? 1 : 0;
  }

  public char[] getChars() {
    if (str != null) {
      return str.toCharArray();
    }
    return null;
  }

  static JSAtom js_get_atom_index(JSRuntime rt, JSAtomStruct p)
  {
    return null;
  }
}
