package com.lox.javascript;


/**
 * @author benpeng.jiang
 * @title=JSFunctionByteCode
 * @projectName LoxScript
 * @description=TODO
 * @date 2021/3/31:42 PM
 */
public class JSFunctionBytecode {
  int js_mode;
  int has_prototype =1; /* true if a prototype field is necessary */
  int has_simple_parameter_list =1;
  int is_derived_class_constructor =1;
  /* true if home_object needs to be initialized */
  int need_home_object =1;
  int func_kind =2;
  int new_target_allowed =1;
  int super_call_allowed =1;
  int super_allowed =1;
  int arguments_allowed =1;
  boolean has_debug = true;
  int backtrace_barrier =1; /* stop backtrace on this function */
  int read_only_bytecode =1;
  /* XXX=4 bits available */
  byte[] byte_code_buf; /* (self pointer) */
  int byte_code_len;
  JSAtom func_name;
  JSVarDef[] vardefs; /* arguments + local variables (arg_count + var_count) (self pointer) */
  JSClosureVar[] closure_var; /* list of variables in the closure (self pointer) */
  int arg_count;
  int var_count;
  int defined_arg_count; /* for length function property */
  int stack_size; /* maximum stack size */
  JSContext realm; /* function realm */
  JSValue[] cpool; /* constant pool (self pointer) */
  int cpool_count;
  int closure_var_count;

  Debug debug = new Debug();

  class Debug {
    String filename;
    int line_num;
    int source_len;
    int pc2line_len;
    byte[] pc2line_buf;
    String source;

    static final int PC2LINE_BASE = (-1);
    static final int PC2LINE_RANGE = 5;
    static final int PC2LINE_OP_FIRST = 1;
    static final int PC2LINE_DIFF_PC_MAX = ((255 - PC2LINE_OP_FIRST) / PC2LINE_RANGE);

  }
}
