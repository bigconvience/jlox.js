package com.craftinginterpreters.lox;

import java.util.Arrays;

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

  int dbuf_put_u32(int val) {
    byte[] input = JUtils.intToByteArray(val);
    return dbuf_put(input);
  }

  int dbuf_put_u16(int val) {
    return dbuf_put_u16((short) val);
  }

  int dbuf_put_u16(short val) {
    return dbuf_put(shortToByteArray(val));
  }

  int put_value(Object val) {
    if (val instanceof JSAtom) {
      return put_atom((JSAtom) val);
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
}
