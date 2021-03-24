package com.lox.javascript;

import static com.lox.javascript.DynBuf.*;
import static com.lox.javascript.OPSpecialObjectEnum.*;
import static com.lox.javascript.OPCodeInfo.opcode_info;


/**
 * @author benpeng.jiang
 * @title: LabelSlot
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/52:26 PM
 */
public class LabelSlot {
  int ref_count;
  int pos;    /* phase 1 address, -1 means not resolved yet */
  int pos2;   /* phase 2 address, -1 means not resolved yet */
  int addr;   /* phase 3 address, -1 means not resolved yet */

  /* peephole optimizations and resolve goto/labels */
  static  int resolve_labels(JSContext ctx, JSFunctionDef s)
  {
    int pos, pos_next, bc_len, op, op1, len, i, line_num;
    byte[] bc_buf;
    DynBuf bc_out;
    LabelSlot[] label_slots;
    LabelSlot ls;
    RelocEntry re, re_next;
    CodeContext cc;
    int label;

    JumpSlot[] jp;


    label_slots = s.label_slots.toArray(new LabelSlot[0]);

    line_num = s.line_num;

    cc = new CodeContext();
    cc.bc_buf = bc_buf = s.byte_code.buf;
    cc.bc_len = bc_len = s.byte_code.size;
    bc_out = new DynBuf();


    if (s.jump_size != 0) {
      s.jump_slots = new JumpSlot[s.jump_size];
    }


    if (s.line_number_size != 0 && (s.js_mode & (1 << 1)) == 0) {
      s.line_number_slots = new LineNumberSlot[s.line_number_size];
      for (i = 0; i < s.line_number_size; i++) {
        s.line_number_slots[i] = new LineNumberSlot();
      }
      s.line_number_last = s.line_num;
      s.line_number_last_pc = 0;
    }


    if (s.home_object_var_idx >= 0) {
      dbuf_putc(bc_out, OPCodeEnum.OP_special_object);
      dbuf_putc(bc_out, OP_SPECIAL_OBJECT_HOME_OBJECT.ordinal());
      put_short_code(bc_out, OPCodeEnum.OP_put_loc, s.home_object_var_idx);
    }

    if (s.this_active_func_var_idx >= 0) {
      dbuf_putc(bc_out, OPCodeEnum.OP_special_object);
      dbuf_putc(bc_out, OP_SPECIAL_OBJECT_THIS_FUNC);
      put_short_code(bc_out, OPCodeEnum.OP_put_loc, s.this_active_func_var_idx);
    }

    if (s.new_target_var_idx >= 0) {
      dbuf_putc(bc_out, OPCodeEnum.OP_special_object);
      dbuf_putc(bc_out, OP_SPECIAL_OBJECT_NEW_TARGET);
      put_short_code(bc_out, OPCodeEnum.OP_put_loc, s.new_target_var_idx);
    }


    if (s.this_var_idx >= 0) {
      if (s.is_derived_class_constructor) {
        dbuf_putc(bc_out, OPCodeEnum.OP_set_loc_uninitialized);
        dbuf_put_u16(bc_out, s.this_var_idx);
      } else {
        dbuf_putc(bc_out, OPCodeEnum.OP_push_this);
        put_short_code(bc_out, OPCodeEnum.OP_put_loc, s.this_var_idx);
      }
    }

    if (s.arguments_var_idx >= 0) {
      if ((s.js_mode & (1 << 0)) != 0 || !s.has_simple_parameter_list) {
        dbuf_putc(bc_out, OPCodeEnum.OP_special_object);
        dbuf_putc(bc_out, OP_SPECIAL_OBJECT_ARGUMENTS);
      } else {
        dbuf_putc(bc_out, OPCodeEnum.OP_special_object);
        dbuf_putc(bc_out, OP_SPECIAL_OBJECT_MAPPED_ARGUMENTS);
      }
      put_short_code(bc_out, OPCodeEnum.OP_put_loc, s.arguments_var_idx);
    }

    if (s.func_var_idx >= 0) {
      dbuf_putc(bc_out, OPCodeEnum.OP_special_object);
      dbuf_putc(bc_out, OP_SPECIAL_OBJECT_THIS_FUNC);
      put_short_code(bc_out, OPCodeEnum.OP_put_loc, s.func_var_idx);
    }

    if (s.var_object_idx >= 0) {
      dbuf_putc(bc_out, OPCodeEnum.OP_special_object);
      dbuf_putc(bc_out, OP_SPECIAL_OBJECT_VAR_OBJECT);
      put_short_code(bc_out, OPCodeEnum.OP_put_loc, s.var_object_idx);
    }

    for (pos = 0; pos < bc_len; pos = pos_next) {
      int val;
      op = Byte.toUnsignedInt(bc_buf[pos]);

      len = opcode_info.get(op).size;
      pos_next = pos + len;

      OPCodeEnum opCodeEnum = OPCodeEnum.values()[op];
      switch(opCodeEnum) {
        case OP_line_num:
          line_num = JUtils.get_u32(bc_buf,  pos + 1);
          break;


        default:
          add_pc2line_info(s, bc_out.size, line_num);
          dbuf_put(bc_out, bc_buf,  pos, len);
          break;
      }
    }


    s.byte_code = bc_out;
    return -1;

  }

  /* the pc2line table gives a line number for each PC value */
  static void add_pc2line_info(JSFunctionDef s, int pc, int line_num)
  {
    if (s.line_number_slots != null 
      &&  s.line_number_count < s.line_number_size
      &&  pc >= s.line_number_last_pc
      &&  line_num != s.line_number_last) {
      s.line_number_slots[s.line_number_count].pc = pc;
      s.line_number_slots[s.line_number_count].line_num = line_num;
      s.line_number_count++;
      s.line_number_last_pc = pc;
      s.line_number_last = line_num;
    }
  }

}
