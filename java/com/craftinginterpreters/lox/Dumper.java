package com.craftinginterpreters.lox;

import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.JSFunctionKindEnum.*;
import static com.craftinginterpreters.lox.JSVarKindEnum.*;
import static com.craftinginterpreters.lox.OPCodeEnum.*;
import static com.craftinginterpreters.lox.JUtils.*;
import static com.craftinginterpreters.lox.OPCodeFormat.*;

/**
 * @author benpeng.jiang
 * @title: Dumper
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/1210:27 AM
 */
public class Dumper {
  static void dump_byte_code(JSContext ctx,
                             int pass,
                             final byte[] tab, int len,
                             final List<JSVarDef> args, int arg_count,
                             final List<JSVarDef> vars, int var_count,
                             final List<JSClosureVar> closure_var, int closure_var_count,
                             final List<JSValue> cpool, int cpool_count,
                             final String source, int line_num,
                             final LabelSlot[] label_slots, JSFunctionBytecode b) {
    if (!Config.dump) {
      return;
    }
    printf("pass %d\n", pass);
    JSOpCode oi;
    int pos, pos_next = 0, op, size, idx, addr, line, line1, in_source;
    byte[] bits = new byte[len];
    for (pos = 0; pos < len; pos = pos_next) {
      op = Byte.toUnsignedInt(tab[pos]);
      oi = OPCodeInfo.opcode_info.get(op);

      pos_next = pos + oi.size;
      switch (oi.fmt) {
        case atom_label_u8:
        case atom_label_u16:
          pos += 4;
          /* fall thru */
        case label:
        case label_u16:
          pos++;
          addr = get_u32(tab, pos);
          if (pass == 1)
            addr = label_slots[addr].pos;
          if (pass == 2)
            addr = label_slots[addr].pos2;
          if (pass == 3)
            addr += pos;
          if (addr >= 0 && addr < len)
            bits[addr] |= 1;
          break;
      }
    }

    line1 = line = 1;
    pos = 0;
    while (pos < len) {
      op = Byte.toUnsignedInt(tab[pos]);
      if (source != null) {
        if (op == OPCodeEnum.OP_line_num.ordinal()) {
          line1 = get_u32(tab, pos + 1) - line_num + 1;
        }
      }
      if (op >= OP_COUNT.ordinal()) {
        println("invalid opcode " + op);
        pos++;
        continue;
      }
      oi = OPCodeInfo.opcode_info.get(op);
      size = oi.size;
      if (pos + size > len) {
        println("truncated opcode " + op);
        break;
      }

      if (bits[pos] != 0) {
        printf("%5d:  ", pos);
      } else {
        printf("        ");
      }
      printf(oi.name);
      pos++;
      switch (oi.fmt) {
        case none_int:
          printf(" %d", op - OP_push_0.ordinal());
          break;
        case npopx:
          printf(" %d", op - OP_call0.ordinal());
          break;
        case u8:
          printf(" %d", get_u8(tab, pos));
          break;
        case i8:
          printf(" %d", get_i8(tab, pos));
          break;
        case u16:
        case npop:
          printf(" %d", get_u16(tab, pos));
          break;
        case npop_u16:
          printf(" %d,%d", get_u16(tab, pos), get_u16(tab, pos + 2));
          break;
        case i16:
          printf(" %d", get_i16(tab, pos));
          break;
        case i32:
          printf(" %d", get_i32(tab, pos));
          break;
        case u32:
          printf(" %d", get_u32(tab, pos));
          break;

        case atom:
          printf(" ");
          ctx.print_atom(get_u32(tab, pos));
          break;
        case atom_u8:
          printf(" ");
          ctx.print_atom(get_u32(tab, pos));
          printf(", %d", get_u8(tab, pos + 4));
          break;
        case atom_u16:
          printf(" ");
          ctx.print_atom(get_u32(tab, pos));
          printf(", %d", get_u16(tab, pos + 4));
          break;
        case atom_label_u8:
        case atom_label_u16:
          printf(" ");
          ctx.print_atom(get_u32(tab, pos));
          addr = get_u32(tab, pos + 4);
          if (pass == 1)
            printf(",%d:%d", addr, label_slots[addr].pos);
          if (pass == 2)
            printf(",%d:%d", addr, label_slots[addr].pos2);
          if (pass == 3)
            printf(",%d", addr + pos + 4);
          if (oi.fmt == atom_label_u8)
            printf(",%d", get_u8(tab, pos + 8));
          else
            printf(",%d", get_u16(tab, pos + 8));
          break;

        case label:
          addr = get_u32(tab, pos);
          if (pass == 1)
            printf(" %d:%d", addr, label_slots[addr].pos);
          if (pass == 2)
            printf(" %d:%d", addr, label_slots[addr].pos2);
          if (pass == 3)
            printf(" %d", addr + pos);
          break;


      }
      println("");
      pos += oi.size - 1;
    }
  }

  static void js_dump_function_bytecode(JSContext ctx, JSFunctionBytecode b) {
    int i;
    char[] atom_buf = new char[64];
    byte[] str;

    if (b.has_debug && b.debug.filename != null) {
      str = ctx.JS_AtomGetStr(b.debug.filename).getBytes();
      printf("%s:%d: ", str, b.debug.line_num);
    }

    str = ctx.JS_AtomGetStr(b.func_name).getBytes();
    printf("function: %s%s\n", b.func_kind, str);
    if (b.js_mode != 0) {
      printf("  mode:");
      if ((b.js_mode & (1 << 0)) != 0)
        printf(" strict");

      if ((b.js_mode & (1 << 2)) != 0)
        printf(" math");

      printf("\n");
    }
    if (b.arg_count != 0 && b.vardefs != null) {
      printf("  args:");
      for (i = 0; i < b.arg_count; i++) {
        printf(" %s", ctx.JS_AtomGetStr(b.vardefs[i].var_name));
      }
      printf("\n");
    }
    if (b.var_count != 0 && b.vardefs != null) {
      printf("  locals:\n");
      for (i = 0; i < b.var_count; i++) {
        JSVarDef vd = b.vardefs[b.arg_count + i];
        printf("%5d: %s %s", i,
          vd.var_kind == JS_VAR_CATCH ? "catch" :
            (vd.var_kind == JS_VAR_FUNCTION_DECL ||
              vd.var_kind == JS_VAR_NEW_FUNCTION_DECL) ? "function" :
              vd.is_const ? "const" :
                vd.is_lexical ? "let" : "var",
          ctx.JS_AtomGetStr(vd.var_name));
        if (vd.scope_level != 0)
          printf(" [level:%d next:%d]", vd.scope_level, vd.scope_next);
        printf("\n");
      }
    }
    if (b.closure_var_count != 0) {
      printf("  closure vars:\n");
      for (i = 0; i < b.closure_var_count; i++) {
        JSClosureVar cv = b.closure_var[i];
        printf("%5d: %s %s:%s%d %s\n", i,
          ctx.JS_AtomGetStr(cv.var_name),
          cv.is_local ? "local" : "parent",
          cv.is_arg ? "arg" : "loc", cv.var_idx,
          cv.is_const ? "const" :
            cv.is_lexical ? "let" : "var");
      }
    }
    printf("  stack_size: %d\n", b.stack_size);
    printf("  opcodes:\n");
    dump_byte_code(ctx,
      3,
      b.byte_code_buf, b.byte_code_len,
      Arrays.asList(b.vardefs), b.arg_count,
      b.vardefs != null ? Arrays.asList(b.vardefs) : null, b.var_count,
      Arrays.asList(b.closure_var), b.closure_var_count,
      Arrays.asList(b.cpool), b.cpool_count,
      b.has_debug ? new String(b.debug.source) : null,
      b.has_debug ? b.debug.line_num : -1,
      null, b);

    if (b.has_debug)
//      dump_pc2line(ctx, b.debug.pc2line_buf, b.debug.pc2line_len, b.debug.line_num);

      printf("\n");
  }

  private static void println(String fmt, Object... args) {
    printf(fmt, args);
    System.out.println();
  }

  private static void printf(String fmt, Object... args) {
    System.out.printf(fmt, args);
  }

}
