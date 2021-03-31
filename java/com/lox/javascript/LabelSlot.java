package com.lox.javascript;

import static com.lox.javascript.Config.OPTIMIZE;
import static com.lox.javascript.DynBuf.*;
import static com.lox.javascript.JSFunctionDef.*;
import static com.lox.javascript.JUtils.*;
import static com.lox.javascript.OPCodeEnum.*;
import static com.lox.javascript.OPSpecialObjectEnum.*;
import static com.lox.javascript.OPCodeInfo.*;


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
  RelocEntry first_reloc;

  /* peephole optimizations and resolve goto/labels */
  static  int resolve_labels(JSContext ctx, JSFunctionDef s)
  {
    int pos, pos_next, bc_len, op, op1 = -1, len, i, line_num;
    PInteger pPosNext = new PInteger();
    PInteger pOp1= new PInteger();
    PInteger pLine = new PInteger();
    OPCodeEnum op1CodeEnum;
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
      for (i = 0; i < s.jump_size; i++) {
        s.jump_slots[i] = new JumpSlot();
      }
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
          line_num = get_u32(bc_buf,  pos + 1);
          break;

        case OP_label:
          {
            label = get_u32(bc_buf, pos + 1);
            assert(label >= 0 && label < s.label_slots.size());
            ls = label_slots[label];
            assert(ls.addr == -1);
            ls.addr = bc_out.size;
            /* resolve the relocation entries */
            for(re = ls.first_reloc; re != null; re = re_next) {
              int diff = ls.addr - re.addr;
              re_next = re.next;
              switch (re.size) {
                case 4:
                  put_u32(bc_out.buf,  re.addr, diff);
                  break;
                case 2:
                  assert(diff <= ((0x7FFF)&diff));
                  put_u16(bc_out.buf, re.addr, diff);
                  break;
                case 1:
                  assert(diff == ((0x7F)&diff));
                  put_u8(bc_out.buf, re.addr, diff);
                  break;
              }
            }
            ls.first_reloc = null;
          }
          break;

        case OP_return:
        case OP_return_undef:
        case OP_return_async:
        case OP_throw:
        case OP_throw_var:
          pLine.value = line_num;
          pos_next = skip_dead_code(s, bc_buf, bc_len, pos_next, pLine);
          line_num = pLine.value;

          on_no_change(s, bc_out, bc_buf, pos, len, line_num);
          break;
        case OP_goto:
          label = get_u32(bc_buf, pos + 1);
          has_goto:
          if (OPTIMIZE) {
            int line1 = -1;
            /* Use custom matcher because multiple labels can follow */
            pOp1.value = op1;
            pLine.value = line1;
            label = find_jump_target(s, label, pOp1, pLine);
            op1 = pOp1.value;
            line1 = pLine.value;
            op1CodeEnum = OPCodeEnum.values()[op1];
            if (code_has_label(cc, pos_next, label)) {
              /* jump to next instruction: remove jump */
              update_label(s, label, -1);
              break;
            }
            if (op1CodeEnum == OP_return || op1CodeEnum == OP_return_undef || op1CodeEnum == OP_throw) {
              /* jump to return/throw: remove jump, append return/throw */
              /* updating the line number obfuscates assembly listing */
              //if (line1 >= 0) line_num = line1;
              update_label(s, label, -1);
              add_pc2line_info(s, bc_out.size, line_num);
              dbuf_putc(bc_out, op1);
              pLine.value = line_num;
              pos_next = skip_dead_code(s, bc_buf, bc_len, pos_next, pLine);
              line1 = pLine.value;
              break;
            }
            /* XXX: should duplicate single instructions followed by goto or return */
                /* For example, can match one of these followed by return:
                   push_i32 / push_const / push_atom_value / get_var /
                   undefined / null / push_false / push_true / get_ref_value /
                   get_loc / get_arg / get_var_ref
                 */
          }
          pPosNext.value = pos_next;
          goto_has_label(ctx, s, op, bc_out, label, bc_buf, bc_len, pos, pPosNext, line_num, label_slots);
          pos_next = pPosNext.value;
          break;

        case OP_catch:
          label = get_u32(bc_buf, pos + 1);
          pPosNext.value = pos_next;
          goto_has_label(ctx, s, op, bc_out, label, bc_buf, bc_len, pos, pPosNext, line_num, label_slots);
          pos_next = pPosNext.value;
            break;
        case OP_if_true:
        case OP_if_false:
          label = get_u32(bc_buf, pos + 1);
          pPosNext.value = pos_next;
          goto_has_label(ctx, s, op, bc_out, label, bc_buf, bc_len, pos, pPosNext, line_num, label_slots);
          pos_next = pPosNext.value;
          break;
        default:
          on_no_change(s, bc_out, bc_buf, pos, len, line_num);
          break;
      }
    }

    s.byte_code = bc_out;
    return -1;

  }

  private static void on_no_change(JSFunctionDef s,
                                   DynBuf bc_out,
                                   byte[] bc_buf,
                                   int pos, int len, int line_num) {
    add_pc2line_info(s, bc_out.size, line_num);
    dbuf_put(bc_out, bc_buf,  pos, len);
  }

  private static void goto_has_label(JSContext ctx, JSFunctionDef s,
                                     int op,
                                     DynBuf bc_out, int label,
                                     byte[] bc_buf,
                                     int bc_len,
                                     int pos,
                                     PInteger pos_next,
                                     int line_num, LabelSlot[] label_slots) {
    OPCodeEnum opCodeEnum = OPCodeEnum.values()[op];
    LabelSlot ls;
    JumpSlot jp;
    add_pc2line_info(s, bc_out.size, line_num);

    if (opCodeEnum == OP_goto) {
      PInteger pLineNumber = new PInteger();
      pLineNumber.value = line_num;
      pos_next.value = skip_dead_code(s, bc_buf, bc_len, pos_next.value, pLineNumber);
      line_num = pLineNumber.value;
    }


    ls = label_slots[label];

    jp = s.jump_slots[s.jump_count++];
    jp.op = op;
    jp.size = 4;
    jp.pos = bc_out.size + 1;
    jp.label = label;

    if (ls.addr == -1) {
      int diff = ls.pos2 - pos - 1;
      if (diff < 128 && (opCodeEnum == OP_if_false || opCodeEnum == OP_if_true || opCodeEnum == OP_goto)) {
        jp.size = 1;
        jp.op = OP_if_false8.ordinal() + (op - OP_if_false.ordinal());
        dbuf_putc(bc_out, OPCodeEnum.values()[jp.op]);
        dbuf_putc(bc_out, 0);
        if (add_reloc(ctx, ls, bc_out.size - 1, 1) == null)
                        goto_fail();

        return;
      }
      if (diff < 32768 && opCodeEnum == OP_goto) {
        jp.size = 2;
        jp.op = OP_goto16.ordinal();
        dbuf_putc(bc_out, OP_goto16);
        dbuf_put_u16(bc_out, 0);
        if (add_reloc(ctx, ls, bc_out.size - 2, 2) == null)
          goto_fail();
        return;
      }
    } else {
      int diff = ls.addr - bc_out.size - 1;
      if (diff < 128 && (opCodeEnum == OP_if_false || opCodeEnum == OP_if_true || opCodeEnum == OP_goto)) {
        jp.size = 1;
        jp.op = OP_if_false8.ordinal() + (op - OP_if_false.ordinal());
        dbuf_putc(bc_out, OPCodeEnum.values()[jp.op]);
        dbuf_putc(bc_out, diff);
        return;
      }
      if ((diff < 32768) && opCodeEnum == OP_goto) {
        jp.size = 2;
        jp.op = OP_goto16.ordinal();
        dbuf_putc(bc_out, OP_goto16);
        dbuf_put_u16(bc_out, diff);
        return;
      }
    }
    dbuf_putc(bc_out, op);
    dbuf_put_u32(bc_out, ls.addr - bc_out.size);
    if (ls.addr == -1) {
      /* unresolved yet: create a new relocation entry */
      if (add_reloc(ctx, ls, bc_out.size - 4, 4) == null)
        goto_fail();
    }
  }

  private static void goto_fail() {

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

  /* return the target label, following the OP_goto jumps
   the first opcode at destination is stored in *pop
 */
  static int find_jump_target(JSFunctionDef s, int label, PInteger pop, PInteger pline)
  {
    int i, pos, op = -1;
    OPCodeEnum opCodeEnum;
    update_label(s, label, -1);
    for (i = 0; i < 10; i++) {
      assert(label >= 0 && label < s.label_slots.size());
      pos = s.label_slots.get(label).pos2;
      for (;;) {
        op = Byte.toUnsignedInt(s.byte_code.buf[pos]);
        opCodeEnum = OPCodeEnum.values()[op];
        switch(opCodeEnum) {
          case OP_line_num:
            if (pline != null)
                    pline.value = get_u32(s.byte_code.buf, pos + 1);
            /* fall thru */
          case OP_label:
            pos += opcode_info.get(op).size;
            continue;
          case OP_goto:
            label = get_u32(s.byte_code.buf, pos + 1);
            break;
          case OP_drop:
            /* ignore drop opcodes if followed by OP_return_undef */
            while (Byte.toUnsignedInt(s.byte_code.buf[++pos]) == OP_drop.ordinal())
              continue;
            if (Byte.toUnsignedInt(s.byte_code.buf[pos]) == OP_return_undef.ordinal())
              op = OP_return_undef.ordinal();
            /* fall thru */
          default:
                on_done(s, label, pop, op);
                return label;
        }
        break;
      }
    }
    /* cycle detected, could issue a warning */
    on_done(s, label, pop, op);
    return label;
  }

  private static void on_done(JSFunctionDef s, int label, PInteger pop, int op) {
       pop.value = op;
      update_label(s, label, +1);
  }

  static int skip_dead_code(JSFunctionDef s, final byte[] bc_buf, int bc_len,
                            int pos, PInteger linep)
  {
    int op, len, label;
    OPCodeEnum opCodeEnum;

    for (; pos < bc_len; pos += len) {
      op = Byte.toUnsignedInt(bc_buf[pos]);
      opCodeEnum = OPCodeEnum.values()[op];
      len = opcode_info.get(op).size;
      if (opCodeEnum == OP_line_num) {
        linep.value = get_u32(bc_buf, pos + 1);
      } else if (opCodeEnum == OP_label) {
        label = get_u32(bc_buf, pos + 1);
        if (update_label(s, label, 0) > 0)
          break;
      } else {
        /* XXX: output a warning for unreachable code? */
        JSAtom atom;
        switch(opcode_info.get(op).fmt) {
          case label:
          case label_u16:
            label = get_u32(bc_buf, pos + 1);
            update_label(s, label, -1);
            break;
          case atom_label_u8:
          case atom_label_u16:
            label = get_u32(bc_buf, pos + 5);
            update_label(s, label, -1);
            /* fall thru */
          case atom:
          case atom_u8:
          case atom_u16:
            atom = get_atom(bc_buf, pos + 1);
            break;
          default:
            break;
        }
      }
    }
    return pos;
  }


  static RelocEntry add_reloc(JSContext ctx, LabelSlot ls, int addr, int size)
  {
    RelocEntry re = new RelocEntry();
    if (re == null)
      return null;
    re.addr = addr;
    re.size = size;
    re.next = ls.first_reloc;
    ls.first_reloc = re;
    return re;
  }

  static boolean code_has_label(CodeContext s, int pos, int label)
  {
    OPCodeEnum opCodeEnum;
    while (pos < s.bc_len) {
      int op = Byte.toUnsignedInt(s.bc_buf[pos]);
      opCodeEnum = OPCodeEnum.values()[op];
      if (opCodeEnum == OP_line_num) {
        pos += 5;
        continue;
      }
      if (opCodeEnum == OP_label) {
        int lab = get_u32(s.bc_buf, pos + 1);
        if (lab == label)
          return true;
        pos += 5;
        continue;
      }
      if (opCodeEnum == OP_goto) {
        int lab = get_u32(s.bc_buf, pos + 1);
        if (lab == label)
          return true;
      }
      break;
    }
    return false;
  }
}
