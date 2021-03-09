package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.craftinginterpreters.lox.JSContext.JS_CALL_FLAG_COPY_ARGV;
import static com.craftinginterpreters.lox.JSValue.JS_UNDEFINED;

/*
  @author benpeng.jiang
  @title: JSVM
  @projectName LoxScript
  @description: TODO
  @date 2021/3/310:22 AM
 */
public class VM {

  static JSValue JS_EvalFunctionInternal(JSContext ctx, JSValue func_obj, final JSValue this_obj,
                                         JSVarRefWrapper var_refs, JSStackFrame sf) {
    JSValue ret_val = null;
    JSTag tag = func_obj.tag;
    if (tag == JSTag.JS_TAG_FUNCTION_BYTECODE) {
      func_obj = ctx.js_closure(func_obj, var_refs, sf);
      ret_val = JS_CallFree(ctx, func_obj, this_obj, 0, null);
    }

    return ret_val;
  }

  static JSValue JS_CallFree(JSContext ctx, JSValue func_obj, final JSValue this_obj,
                             int argc, final JSValue[] argv) {
    JSValue res = JS_CallInternal(ctx, func_obj, this_obj, JS_UNDEFINED,
      argc, argv, JS_CALL_FLAG_COPY_ARGV);
    return res;
  }


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
    JSFunctionBytecode b;
    JSStackFrame sf = new JSStackFrame();
    int pc;
    List<JSVarRef> var_refs;
    p = funcObj.JS_VALUE_GET_OBJ();

    if (p.JSClassID != JSClassID.JS_CLASS_BYTECODE_FUNCTION) {

    }

    b = p.func.function_bytecode;
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
      arg_buf.add(JS_UNDEFINED);
    }

    for (i = 0; i < b.var_count; i++) {
      var_buf.add(JS_UNDEFINED);
    }
    sf.arg_buf = arg_buf;
    sf.var_buf = var_buf;

    sp = 0;
    pc = 0;
    JSValue top = null;
    sf.prev_frame = rt.current_stack_frame;
    rt.current_stack_frame = sf;
    ctx = b.realm;
    while (pc < b.byte_code_len) {
      int call_argc;
      int u32;
      JSValue[] call_argv;
      int code = Byte.toUnsignedInt(b.byte_code_buf[pc++]);
      OPCodeEnum opcode = OPCodeInfo.opcode_enum.get(code);
      byte[] code_buf = b.byte_code_buf;
      switch (opcode) {
        case OP_print:
          top = peek(stack_buf, sp);
          top.print();
          sp--;
          pc++;
          break;
        case OP_push_i32:
          u32 = JUtils.get_u32(code_buf, pc);
          push(stack_buf, sp, JSValue.JS_NewInt32(ctx, u32));
          sp++;
          pc += 4;
          break;
        case OP_push_atom_value:
          u32 = JUtils.get_u32(code_buf, pc);
          push(stack_buf, sp, ctx.JS_AtomToValue(u32));
          pc += 4;
          break;
        case OP_check_define_var:
          JSAtom atom = new JSAtom(JUtils.get_u32(code_buf, pc));
          flags = code_buf[pc];
          pc += 5;
          break;
      }
    }

    rt.current_stack_frame = sf.prev_frame;
    return ret_val;
  }

  private static JSValue peek(JSValue[] stack_buf, int sp) {
    JSValue value = stack_buf[sp];
    return value;
  }

  private static JSValue push(JSValue[] stackBuf, int sp, JSValue value) {
    stackBuf[sp] = value;
    return value;
  }


}
