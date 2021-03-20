package com.craftinginterpreters.lox;

import java.util.List;

/**
 * @author benpeng.jiang
 * @title: JSStackFrame
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/31:54 PM
 */
public class JSStackFrame {
  JSStackFrame prev_frame;
  JSValue cur_func;
  JSValue[] arg_buf;
  JSValue[] var_buf;
  List<JSVarRef> var_ref_list;
  byte cur_pc;
  int js_mode;
  JSValue cur_sp;
}
