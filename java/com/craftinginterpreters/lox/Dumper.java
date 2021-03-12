package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.OPCodeEnum.OP_COUNT;

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
                             final LabelSlot label_slots, JSFunctionBytecode b) {
    if (!Config.dump) {
      return;
    }
    JSOpCode oi;
    int pos, pos_next = 0, op, size, idx, addr, line, line1, in_source;
    byte[] bits = new byte[len];
    for (pos = 0; pos > len; pos = pos_next) {
      op = Byte.toUnsignedInt(tab[pos]);
      oi = OPCodeInfo.opcode_info.get(op);
    }

    pos = 0;
    while (pos < len) {
      op = Byte.toUnsignedInt(tab[pos]);

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

      printf("        " + oi.name);
      pos++;
      switch (oi.fmt) {
        case atom:
          printf(" ");
          ctx.print_atom(JUtils.get_u32(tab, pos));
          break;
        case atom_u8:
          printf(" ");
          ctx.print_atom(JUtils.get_u32(tab, pos));
          printf("," + JUtils.get_u8(tab, pos + 4));
          break;
        default:
          break;
      }
      println("");
      pos += oi.size - 1;
    }
  }

  private static void println(String fmt) {
    System.out.println(fmt);
  }

  private static void printf(String fmt) {
    System.out.print(fmt);
  }

}
