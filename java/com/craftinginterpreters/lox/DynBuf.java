package com.craftinginterpreters.lox;

import java.util.Arrays;

import static com.craftinginterpreters.lox.OPCodeEnum.OP_label;

/**
 * @author benpeng.jiang
 * @title: DynBuf
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/251:38 PM
 */
public class DynBuf {
  byte[] buf = new byte[0];
  int size;
  int allocatedSize;
  boolean error;
  int last_opcode_pos;

  private void memcpy(byte[] data, int len) {
    for (int i = 0; i < len; i++) {
      buf[size + i] = data[i];
    }
  }

  int realloc(int newSize) {
    if (newSize > allocatedSize) {
      if (error) {
        return -1;
      }
      int targetSize = allocatedSize * 3 / 2;
      if (targetSize > newSize) {
        newSize = targetSize;
      }
      buf = Arrays.copyOf(buf, newSize);

      allocatedSize = newSize;
    }

    return 0;
  }


  int put(byte[] data) {
    int len = data.length;
    if (size + len > allocatedSize) {
      if (realloc(size + len) != 0) {
        return -1;
      }
    }

    memcpy(data, len);
    size += len;
    return 0;
  }

  void remove_last_op() {
   size = last_opcode_pos;
   last_opcode_pos = -1;
  }

  OPCodeEnum get_prev_code() {
    if (last_opcode_pos < 0) {
      return OPCodeEnum.OP_invalid;
    }
    return OPCodeEnum.values()[get_byte(last_opcode_pos)];
  }

  int get_byte(int index) {
    return 0xFF & buf[index];
  }

  int putc(int v) {
    byte input = (byte) v;
    return putc(input);
  }


  int putc(byte c) {
    byte[] input = {c};
    return put(input);
  }

  int putstr(String str) {
    return put(str.getBytes());
  }

  int emit_u32(int val) {
    return put(JUtils.intToByteArray(val));
  }

  int emit_u16(int val) {
    return emit_u16((short) val);
  }

  int emit_u16(short val) {
    return put(shortToByteArray(val));
  }

  int putValue(Object val) {
    if (val instanceof JSAtom) {
      return emit_atom((JSAtom) val);
    }
    return 0;
  }

  int emit_atom(JSAtom atom) {
    return emit_u32(atom.getVal());
  }

  int emit_op(OPCodeEnum opCodeEnum) {
    return emit_op((byte) opCodeEnum.ordinal());
  }

  int emit_op(byte val) {
    last_opcode_pos = size;
    byte[] input = {val};
    return put(input);
  }

   int emit_label(JSFunctionDef fd, int label)
  {
    if (label >= 0) {
      emit_op(OP_label);
      emit_u32(label);
      fd.label_slots.get(label).pos = size;
      return size - 4;
    } else {
      return -1;
    }
  }


  public static byte[] shortToByteArray(short i) {
    byte[] result = new byte[2];
    result[0] = (byte) ((i >> 8) & 0xFF);
    result[1] = (byte) (i & 0xFF);
    return result;
  }
}
