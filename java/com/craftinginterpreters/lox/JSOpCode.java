package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import  static com.craftinginterpreters.lox.OPCodeEnum.*;
import  static com.craftinginterpreters.lox.OPCodeFormat.*;
public class JSOpCode {
  OPCodeEnum id;
  int size;
  int n_pop;
  int n_push;
  OPCodeFormat f;

  public JSOpCode(OPCodeEnum id, int size, int n_pop, int n_push, OPCodeFormat f) {
    this.id = id;
    this.size = size;
    this.n_pop = n_pop;
    this.n_push = n_push;
    this.f = f;
  }

  public static List<JSOpCode> opcode_info;

  {
    opcode_info = new ArrayList<>();
    opcode_info.add(new JSOpCode(OP_neg, 1, 1, 3, atom));
  }
}