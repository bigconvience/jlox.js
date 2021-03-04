package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static com.craftinginterpreters.lox.OPCodeEnum.OP_push_i32;

/*
  @author benpeng.jiang
  @title: JSVM
  @projectName LoxScript
  @description: TODO
  @date 2021/3/310:22 AM
 */
public class JSVM {

  static JSValue JS_CallInternal(JSContext callerCtx, final JSValue funcObj,
                                 final JSValue thisObject, final JSValue newTarget,
                                 int argc, JSValue[] argv, int flags) {
    JSRuntime rt = callerCtx.rt;
    JSContext ctx;
    List<JSValue> local_buf = new ArrayList<>();
    JSValue[] stack_buf;
    List<JSValue> var_buf = new ArrayList<>(), arg_buf = new ArrayList<>(), pva = new ArrayList<>();
    JSValue ret_val = null;
    int sp;
    JSObject p;
    JSFunctionByteCode b;
    JSStackFrame sf = new JSStackFrame();
    int pc;
    List<JSVarRef> var_refs;
    p = funcObj.JS_VALUE_GET_OBJ();

    if (p.classID != ClassID.JS_CLASS_BYTECODE_FUNCTION) {

    }

    b = p.func.functionByteCode;
    sf.js_mode = b.js_mode;
    sf.cur_func = funcObj;
    sf.var_ref_list = new LinkedList<>();
    var_refs = p.func.var_refs;

    stack_buf = new JSValue[b.stack_size];
    int n = Math.min(argc, b.arg_count);
    int i;
    for (i = 0; i < n; i++) {
      arg_buf.add(argv[i]);
    }
    for (; i < b.arg_count; i++) {
      arg_buf.add(JSValue.JS_UNDEFINED);
    }

    for (i = 0; i < b.var_count; i++) {
      var_buf.add(JSValue.JS_UNDEFINED);
    }
    sf.arg_buf = arg_buf;
    sf.var_buf = var_buf;

    sp = 0;
    pc = 0;
    JSValue top = null;
    sf.prev_frame = rt.current_stack_frame.prev_frame;
    rt.current_stack_frame = sf;
    ctx = b.realm;
    while (pc < b.byte_code_len) {
      int call_argc;
      int u32;
      JSValue[] call_argv;
      int code = b.byte_code_buf[pc++];
      OPCodeEnum opcode = JSOpCode.opcode_enum.get(code);
      switch (opcode) {
        case OP_push_i32:
          u32 = Utils.get_u32(b.byte_code_buf, sp);
          top = push(stack_buf, sp, JSValue.JS_NewInt32(ctx, u32));
          sp++;
          pc += 4;
          break;
      }
    }

    rt.current_stack_frame = sf.prev_frame;
    return ret_val;
  }

  private static JSValue push(JSValue[] stackBuf, int sp, JSValue value) {
    stackBuf[sp] = value;
    return value;
  }


}
