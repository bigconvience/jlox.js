package com.lox.javascript;

import static com.lox.javascript.JSContext.JS_STACK_SIZE_MAX;
import static com.lox.javascript.JSThrower.*;
import static com.lox.javascript.JUtils.*;
import static com.lox.javascript.ShortOPCodeEnum.*;
import static com.lox.javascript.OPCodeFormat.*;

/**
 * @author benpeng.jiang
 * @title: StackSizeState
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/49:08 PM
 */
public class StackSizeState {
  int stack_len_max;
  short[] stack_level_tab;

  static int compute_stack_size_rec(JSContext ctx,
                                    JSFunctionDef fd,
                                    StackSizeState s,
                                    int pos, int op, int stack_len)
  {
    int bc_len, diff, n_pop, pos_next;
    JSOpCode oi;
    byte[] bc_buf;

    if (stack_len > s.stack_len_max) {
      s.stack_len_max = stack_len;
      if (s.stack_len_max > JS_STACK_SIZE_MAX) {
        on_stack_overflow(ctx, op, pos);
        return -1;
      }
    }
    bc_buf = fd.byte_code.buf;
    bc_len = fd.byte_code.size;
    while (pos < bc_len) {
      if (pos >= bc_len)
        on_stack_overflow(ctx, op, pos);
      if (get_u16(s.stack_level_tab, pos) != 0xffff) {
        /* already explored: check that the stack size is consistent */
        if (s.stack_level_tab[pos] != stack_len) {
          JS_ThrowInternalError(ctx, "unconsistent stack size: %d %d (pc=%d)",
            s.stack_level_tab[pos], stack_len, pos);
          return -1;
        } else {
          return 0;
        }
      } else {
        s.stack_level_tab[pos] = (short) stack_len;
      }

      op = get_u8(bc_buf, pos);
      if (op == 0 || op >= OP_COUNT.ordinal()) {
        JS_ThrowInternalError(ctx, "invalid opcode (op=%d, pc=%d)", op, pos);
        return -1;
      }
      oi = ShortOPCodeInfo.opcode_info.get(op);
      pos_next = pos + oi.size;
      if (pos_next > bc_len) {
        buf_overflow:
        JS_ThrowInternalError(ctx, "bytecode buffer overflow (op=%d, pc=%d)", op, pos);
        return -1;
      }
      n_pop = oi.n_pop;
      /* call pops a variable number of arguments */
      if (oi.fmt == npop || oi.fmt == npop_u16) {
        n_pop += get_u16(bc_buf, pos + 1);
      } else {
        if (oi.fmt == npopx) {
          n_pop += op - OP_call0.ordinal();
        }
      }

      if (stack_len < n_pop) {
        JS_ThrowInternalError(ctx, "stack underflow (op=%d, pc=%d)", op, pos);
        return -1;
      }
      stack_len += oi.n_push - n_pop;
      if (stack_len > s.stack_len_max) {
        s.stack_len_max = stack_len;
        if (s.stack_len_max > JS_STACK_SIZE_MAX) {
          on_stack_overflow(ctx, op, pos);
          return -1;
        }
      }
      ShortOPCodeEnum opCodeEnum = ShortOPCodeEnum.values()[op];
      switch(opCodeEnum) {
        case OP_tail_call:
        case OP_tail_call_method:
        case OP_return:
        case OP_return_undef:
        case OP_return_async:
        case OP_throw:
        case OP_throw_var:
        case OP_ret:
            return 0;
        case OP_goto:
          diff = get_u32(bc_buf, pos + 1);
          pos_next = pos + 1 + diff;
          break;
        case OP_goto16:
          diff = get_u16(bc_buf, pos + 1);
          pos_next = pos + 1 + diff;
          break;
        case OP_goto8:
          diff = get_u8(bc_buf, pos + 1);
          pos_next = pos + 1 + diff;
          break;
        case OP_if_true8:
        case OP_if_false8:
          diff = get_u8(bc_buf, pos + 1);
          if (compute_stack_size_rec(ctx, fd, s, pos + 1 + diff, op, stack_len) != 0)
            return -1;
          break;
        case OP_if_true:
        case OP_if_false:
        case OP_catch:
          diff = get_u32(bc_buf, pos + 1);
          if (compute_stack_size_rec(ctx, fd, s, pos + 1 + diff, op, stack_len) != 0)
            return -1;
          break;
        case OP_gosub:
          diff = get_u32(bc_buf, pos + 1);
          if (compute_stack_size_rec(ctx, fd, s, pos + 1 + diff, op, stack_len + 1) != 0)
            return -1;
          break;
        case OP_with_get_var:
        case OP_with_delete_var:
          diff = get_u32(bc_buf, pos + 5);
          if (compute_stack_size_rec(ctx, fd, s, pos + 5 + diff, op, stack_len + 1) != 0)
            return -1;
          break;
        case OP_with_make_ref:
        case OP_with_get_ref:
        case OP_with_get_ref_undef:
          diff = get_u32(bc_buf, pos + 5);
          if (compute_stack_size_rec(ctx, fd, s, pos + 5 + diff, op, stack_len + 2) != 0)
            return -1;
          break;
        case OP_with_put_var:
          diff = get_u32(bc_buf, pos + 5);
          if (compute_stack_size_rec(ctx, fd, s, pos + 5 + diff, op, stack_len - 1) != 0)
            return -1;
          break;

        default:
          break;
      }
      pos = pos_next;
    }
    return 0;
  }

  private static void on_stack_overflow(JSContext ctx, int op, int pos) {
    JS_ThrowInternalError(ctx, "stack overflow (op=%d, pc=%d)", op, pos);
  }
}
