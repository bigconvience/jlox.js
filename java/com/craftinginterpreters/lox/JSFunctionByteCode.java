package com.craftinginterpreters.lox;

import java.util.List;

/**
 * @author benpeng.jiang
 * @title: JSFunctionByteCode
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/31:42 PM
 */
public class JSFunctionByteCode {
  ClassID class_Id;
  byte js_mode;


  byte[] byte_code_buf;
  int byte_code_len;
  JSAtom func_name;
  List<JSVarDef> vardefs;

  short arg_count;
  short var_count;
  short defined_arg_count;
  short stack_size;


  JSContext realm;
  List<JSValue> cpool;
  int closure_var_count;
}
