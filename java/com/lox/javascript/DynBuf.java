package com.lox.javascript;

import java.util.Arrays;

import static com.lox.javascript.OPCodeEnum.OP_invalid;

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

  public DynBuf() {
  }

  private void memcpy(byte[] data, int position, int len) {
    for (int i = 0; i < len; i++) {
      buf[size + i] = data[position + i];
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

  static int dbuf_put(DynBuf bc,byte[] data, int position, int len) {
    return bc.dbuf_put(data, position, len);
  }
  int dbuf_put(byte[] data, int position, int len) {
    if (size + len > allocatedSize) {
      if (realloc(size + len) != 0) {
        return -1;
      }
    }

    memcpy(data, position, len);
    size += len;
    return 0;
  }


  int dbuf_put(byte[] data) {
    return dbuf_put(data, 0, data.length);
  }


  int get_byte(int index) {
    return 0xFF & buf[index];
  }

  static int dbuf_putc(DynBuf bc, OPCodeEnum val) {
    return dbuf_putc(bc, val.ordinal());
  }

  static int dbuf_putc(DynBuf bc, OPSpecialObjectEnum val) {
   return dbuf_putc(bc, val.ordinal());
  }

  static void put_short_code(DynBuf bc_out, OPCodeEnum op, int idx)
  {
   put_short_code(bc_out, op.ordinal(), idx);
  }

  static void put_short_code(DynBuf bc_out, int op, int idx)
  {
    dbuf_putc(bc_out, op);
    dbuf_put_u16(bc_out, idx);
  }

  static int dbuf_putc(DynBuf bc, int val) {
    return bc.dbuf_putc(val);
  }

  int dbuf_putc(int v) {
    byte input = (byte) v;
    return dbuf_putc(input);
  }


  int dbuf_putc(byte c) {
    byte[] input = {c};
    return dbuf_put(input);
  }


  int putstr(String str) {
    return dbuf_put(str.getBytes());
  }

  int dbuf_put_u32(JSAtom val) {
    return dbuf_put_u32(val.getVal());
  }

  static int dbuf_put_u32(DynBuf bc, int val) {
    return bc.dbuf_put_u32(val);
  }

  static int dbuf_put_u32(DynBuf bc, JSAtom val) {
    return bc.dbuf_put_u32(val);
  }

  static int dbuf_put_u32(DynBuf bc, JSAtomEnum val) {
    return bc.dbuf_put_u32(val.toJSAtom());
  }

  int dbuf_put_u32(int val) {
    byte[] input = JUtils.intToByteArray(val);
    return dbuf_put(input);
  }

  static int dbuf_put_u16(DynBuf bc, int val) {
    return bc.dbuf_put_u16((short) val);
  }

  int dbuf_put_u16(int val) {
    return dbuf_put_u16((short) val);
  }

  int dbuf_put_u16(short val) {
    return dbuf_put(shortToByteArray(val));
  }

  static int put_value(DynBuf bc, Object val) {
    if (val instanceof JSAtom) {
      return bc.put_atom((JSAtom) val);
    }
    return 0;
  }

  int put_atom(JSAtom atom) {
    return dbuf_put_u32(atom.getVal());
  }

  int dbuf_putc(OPCodeEnum opCodeEnum) {
    return dbuf_putc(opCodeEnum.ordinal());
  }


  public static byte[] shortToByteArray(short i) {
    byte[] result = new byte[2];
    result[0] = (byte) ((i >> 8) & 0xFF);
    result[1] = (byte) (i & 0xFF);
    return result;
  }

  public static OPCodeEnum getOPCode(byte[] buf, int pc) {
    if ( pc < 0)
      return OP_invalid;
    else {
      int op = Byte.toUnsignedInt(buf[pc]);
      OPCodeEnum opcode = OPCodeInfo.opcode_enum.get(op);
      return opcode;
    }
  }
}
