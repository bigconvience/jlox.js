package com.craftinginterpreters.lox;

import java.util.Objects;

public class JSAtom {
  static final int JS_ATOM_TYPE_STRING = 0;
  static final int JS_ATOM_TYPE_GLOBAL_SYMBOL = 1;
  static final int JS_ATOM_TYPE_SYMBOL = 2;
  static final int JS_ATOM_TYPE_PRIVATE = 3;

  static final int JS_ATOM_TAG_INT = (1 << 31);
  static final int JS_ATOM_MAX_INT = (JS_ATOM_TAG_INT - 1);
  static final int JS_ATOM_MAX = ((1 << 30) - 1);

  boolean __JS_AtomIsTaggedInt() {
    return (getVal() & JS_ATOM_TAG_INT) != 0;
  }

  int __JS_AtomToUInt32() {
    return getVal() & ~JS_ATOM_TAG_INT;
  }

  static final JSAtom JS_ATOM_NULL = new JSAtom(0);
  static final JSAtom JS_ATOM_empty_string = new JSAtom(JSAtomEnum.JS_ATOM_empty_string.ordinal());

  private final int val;

  public JSAtom(int val) {
    this.val = val;
  }

  public int getVal() {
    return val;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JSAtom atom = (JSAtom) o;
    return val == atom.val;
  }

  @Override
  public String toString() {
    return "JSAtom{" +
      "val=" + val +
      '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(val);
  }
}