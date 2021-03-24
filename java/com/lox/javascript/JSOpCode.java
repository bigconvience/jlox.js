package com.lox.javascript;

public class JSOpCode {
  String name;
  int size;
  int n_pop;
  int n_push;
  OPCodeFormat fmt;

  public JSOpCode(String name, int size, int n_pop, int n_push, OPCodeFormat fmt) {
    this.name = name;
    this.size = size;
    this.n_pop = n_pop;
    this.n_push = n_push;
    this.fmt = fmt;
  }
}