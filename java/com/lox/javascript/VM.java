package com.lox.javascript;

import com.lox.clibrary.stdlib_h;

import java.util.*;

import static com.lox.javascript.Config.*;
import static com.lox.javascript.JSCompare.*;
import static com.lox.javascript.JSContext.*;
import static com.lox.javascript.JSTag.*;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JUtils.*;

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
      func_obj = js_closure(ctx, func_obj, var_refs, sf);
      ret_val = JS_CallFree(ctx, func_obj, this_obj, 0, null);
    }

    return ret_val;
  }

  static JSValue JS_CallFree(JSContext ctx, JSValue func_obj, final JSValue this_obj,
                             int argc, final JSValue[] argv) {
    JSValue res = JS_CallInternal(ctx, func_obj, this_obj, JSValue.JS_UNDEFINED,
      argc, argv, JSContext.JS_CALL_FLAG_COPY_ARGV);
    return res;
  }


  static JSValue JS_CallInternal(JSContext callerCtx, final JSValue funcObj,
                                 final JSValue thisObject, final JSValue newTarget,
                                 int argc, JSValue[] argv, int flags) {
    JSRuntime rt = callerCtx.rt;
    JSContext ctx;
    List<JSValue> local_buf = new ArrayList<>();
    JSValue[] stack_buf;
    JSValue[] var_buf,arg_buf,pva;

    JSValue ret_val = null;
    int sp;
    JSObject p;
    JSFunctionBytecode b;
    JSStackFrame sf = new JSStackFrame();
    int pc;
    List<JSVarRef> var_refs;
    p = funcObj.JS_VALUE_GET_OBJ();

    if (p.class_id != JSClassID.JS_CLASS_BYTECODE_FUNCTION) {

    }

    b = p.u.func.function_bytecode;
    sf.js_mode = b.js_mode;
    sf.cur_func = funcObj;
    sf.var_ref_list = new LinkedList<>();
    var_refs = p.u.func.var_refs;

    stack_buf = new JSValue[b.stack_size];
    int n = Math.min(argc, b.arg_count);
    arg_buf = new JSValue[n + b.arg_count];
    var_buf = new JSValue[b.var_count];
    int i;
    for (i = 0; i < n; i++) {
      arg_buf[i] = argv[i];
    }
    for (; i < b.arg_count; i++) {
      arg_buf[i] = JSValue.JS_UNDEFINED;
    }

    for (i = 0; i < b.var_count; i++) {
      var_buf[i] = JSValue.JS_UNDEFINED;
    }
    sf.arg_buf = arg_buf;
    sf.var_buf = var_buf;

    sp = 0;
    pc = 0;
    JSValue top = null;
    sf.prev_frame = rt.current_stack_frame;
    rt.current_stack_frame = sf;
    ctx = b.realm;
    JSAtom atom;
    JSValue val;
    int ret;
    int idx;
    byte[] code_buf = b.byte_code_buf;
    int show_call;
    while (pc < b.byte_code_len) {
      int call_argc;
      int u32;
      JSValue[] call_argv;
      int op = Byte.toUnsignedInt(code_buf[pc++]);
      ShortOPCodeEnum opcode = ShortOPCodeInfo.opcode_enum.get(op);
      JSValue op1, op2;
      switch (opcode) {
        case OP_print:
          top = peek(stack_buf, --sp);
          top.print();
          break;
        case OP_push_i32:
          u32 = JUtils.get_u32(code_buf, pc);
          push(stack_buf, sp++, JSValue.JS_NewInt32(ctx, u32));
          pc += 4;
          break;
        case OP_push_atom_value:
          u32 = JUtils.get_u32(code_buf, pc);
          push(stack_buf, sp++, ctx.JS_AtomToValue(u32));
          pc += 4;
          break;
        case OP_check_define_var:
          atom = new JSAtom(JUtils.get_u32(code_buf, pc));
          flags = code_buf[pc + 4];
          pc += 5;
          if (ctx.JS_CheckDefineGlobalVar(atom, flags) != 0) {

          }
          break;
        case OP_define_var:
          atom = new JSAtom(JUtils.get_u32(code_buf, pc));
          flags = code_buf[pc + 4];
          pc += 5;
          if (ctx.JS_DefineGlobalVar(ctx, atom, flags) != 0) {

          }
          break;
        case OP_put_var:
        case OP_put_var_init:
          atom = new JSAtom(JUtils.get_u32(code_buf, pc));
          pc += 4;
          top = peek(stack_buf, --sp);
          ret = JS_SetGlobalVar(ctx, atom, top, opcode.ordinal() - OPCodeEnum.OP_put_var.ordinal());
          if (ret < 0) {

          }
          break;
        case OP_get_var_undef:
        case OP_get_var:

          atom = new JSAtom(JUtils.get_u32(code_buf, pc));
          pc += 4;

          val = JS_GetGlobalVar(ctx, atom, op - OPCodeEnum.OP_get_var_undef.ordinal());
          if (JS_IsException(val)) {
            return JSThrower.JS_Throw(ctx, val);
          }
          push(stack_buf, sp++, val);
          break;
        case OP_undefined:
          push(stack_buf, sp++, JSValue.JS_UNDEFINED);
          break;
        case OP_null:
          push(stack_buf, sp++, JSValue.JS_NULL);
          break;
        case OP_dup:
          top = peek(stack_buf, (sp - 1));
          push(stack_buf, sp++, top);
          break;
        case OP_add:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            long r;
            r = op1.JS_VALUE_GET_INT() + op2.JS_VALUE_GET_INT();
            if ((int) r != r) {
              stdlib_h.abort();
            }
            push(stack_buf, sp - 2, JSValue.JS_NewInt32(ctx, (int) r));
            sp--;
          }
          break;
        case OP_sub:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            long r;
            r = op1.JS_VALUE_GET_INT() - op2.JS_VALUE_GET_INT();
            if ((int) r != r) {
              stdlib_h.abort();
            }
            push(stack_buf, sp - 2, JSValue.JS_NewInt32(ctx, (int) r));
            sp--;
          }
          break;
        case OP_mul:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            long r;
            r = op1.JS_VALUE_GET_INT() * op2.JS_VALUE_GET_INT();
            if ((int) r != r) {
              stdlib_h.abort();
            }
            push(stack_buf, sp - 2, JSValue.JS_NewInt32(ctx, (int) r));
            sp--;
          }
          break;
        case OP_div:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewFloat64(ctx, (double) v1 / (double) v2));
            sp--;
          }
          break;
        case OP_lt:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewBool(ctx, v1 < v2));
            sp--;
          }
          break;
        case OP_lte:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewBool(ctx, v1 <= v2));
            sp--;
          }
          break;
        case OP_gt:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewBool(ctx, v1 > v2));
            sp--;
          }
          break;
        case OP_gte:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewBool(ctx, v1 >= v2));
            sp--;
          }
          break;
        case OP_eq:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          show_call = js_eq_slow(ctx, stack_buf, sp, false);
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewBool(ctx, v1 == v2));
            sp--;
          } else {
            if (show_call != 0) {

            }
            sp--;
          }
          break;
        case OP_neq:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          show_call = js_eq_slow(ctx, stack_buf, sp, true);
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewBool(ctx, v1 != v2));
            sp--;
          } else {
            if (show_call != 0) {

            }
            sp--;
          }
          break;
        case OP_strict_eq:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          show_call = js_strict_eq_slow(ctx, stack_buf, sp, false);
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewBool(ctx, v1 == v2));
            sp--;
          } else {
            if (show_call != 0) {

            }
            sp--;
          }
          break;
        case OP_strict_neq:
          op1 = peek(stack_buf, (sp - 2));
          op2 = peek(stack_buf, (sp - 1));
          show_call = js_strict_eq_slow(ctx, stack_buf, sp, true);
          if (JSValue.JS_VALUE_IS_BOTH_INT(op1, op2)) {
            int v1 = op1.JS_VALUE_GET_INT();
            int v2 = op2.JS_VALUE_GET_INT();
            push(stack_buf, sp - 2, JSValue.JS_NewBool(ctx, v1 != v2));
            sp--;
          } else {
            if (show_call != 0) {

            }
            sp--;
          }
          break;
        case OP_push_false:
          push(stack_buf, sp++, JSValue.JS_FALSE);
          break;
        case OP_push_true:
          push(stack_buf, sp++, JSValue.JS_TRUE);
          break;
        case OP_get_loc:
          idx = JUtils.get_u16(code_buf, pc);
          pc += 2;
          push(stack_buf, sp, var_buf[idx]);
          sp++;
        break;
        case OP_put_loc:
          idx = JUtils.get_u16(code_buf, pc);
          pc += 2;
          JSContext.set_value(ctx, var_buf[idx], peek(stack_buf, sp-1));
          sp--;
          break;
        case OP_set_loc:
          idx = JUtils.get_u16(code_buf, pc);
          pc += 2;
          JSContext.set_value(ctx, var_buf[idx], peek(stack_buf, sp-1));
          break;
        case OP_goto:
          pc += get_u32(code_buf, pc);
          if (js_poll_interrupts(ctx))
                  on_exception();
          break;

        case OP_goto16:
          pc += get_u16(code_buf, pc);
          if (js_poll_interrupts(ctx))
            on_exception();
          break;
        case OP_goto8:
        pc += get_u8(code_buf, pc);
        if (js_poll_interrupts(ctx))
          on_exception();
        break;
        case OP_if_true:
        {
          int res;

          op1 = peek(stack_buf, sp-1);
          pc += 4;
          if (JS_VALUE_GET_TAG(op1).ordinal() <= JS_TAG_UNDEFINED.ordinal()) {
            res = JS_VALUE_GET_INT(op1);
          } else {
            res = JS_ToBoolFree(ctx, op1);
          }
          sp--;
          if (res != 0) {
            pc += get_u32(code_buf, pc - 4) - 4;
          }
          if (js_poll_interrupts(ctx))
            on_exception();
        }
        break;
        case OP_if_false:
        {
          int res;
          op1 = peek(stack_buf, sp-1);
          pc += 4;
          if (JS_VALUE_GET_TAG(op1).ordinal() <= JS_TAG_UNDEFINED.ordinal()) {
            res = JS_VALUE_GET_INT(op1);
          } else {
            res = JS_ToBoolFree(ctx, op1);
          }
          sp--;
          if (res == 0) {
            pc += get_u32(code_buf, pc - 4) - 4;
          }
          if (js_poll_interrupts(ctx))
            on_exception();
        }
        break;
        case OP_if_true8:
        {
          int res;

          op1 = peek(stack_buf, sp-1);
          pc += 1;
          if (JS_VALUE_GET_TAG(op1).ordinal() <= JS_TAG_UNDEFINED.ordinal()) {
            res = JS_VALUE_GET_INT(op1);
          } else {
            res = JS_ToBoolFree(ctx, op1);
          }
          sp--;
          if (res != 0) {
            pc += get_u8(code_buf, pc-1) - 1;
          }
          if (js_poll_interrupts(ctx))
            on_exception();
        }
        break;
        case OP_if_false8:
        {
          int res;
          op1 = peek(stack_buf, sp-1);;
          pc += 1;
          if (JS_VALUE_GET_TAG(op1).ordinal() <= JS_TAG_UNDEFINED.ordinal()) {
            res = JS_VALUE_GET_INT(op1);
          } else {
            res = JS_ToBoolFree(ctx, op1);
          }
          sp--;
          if (res == 0) {
            pc += get_u8(code_buf, pc-1) - 1;
          }
          if (js_poll_interrupts(ctx))
            on_exception();
        }
        break;
      }
    }

    rt.current_stack_frame = sf.prev_frame;
    return ret_val;
  }

  private static void on_exception() {

  }

  static  boolean __js_poll_interrupts(JSContext ctx)
  {
    JSRuntime rt = ctx.rt;
    ctx.interrupt_counter = JS_INTERRUPT_COUNTER_INIT;
    if (rt.interrupt_handler(rt, rt.interrupt_opaque)) {
      /* XXX: should set a specific flag to avoid catching */
      JS_ThrowInternalError(ctx, "interrupted");
      JS_SetUncatchableError(ctx, rt.current_exception, true);
      return true;
    }
    return false;
  }

  static boolean js_poll_interrupts(JSContext ctx)
  {
    if (--ctx.interrupt_counter <= 0) {
      return __js_poll_interrupts(ctx);
    } else {
      return false;
    }
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
