package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: Uint32
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/227:52 PM
 */
public class uint32_t {
  long val;

  public uint32_t(long val) {
    this.val = val;
  }

  public uint32_t() {
  }

  public long toUnit32() {
    return val;
  }

  public int toInt() {
    return (int) val;
  }
}
