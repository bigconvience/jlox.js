package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: Utils
 * @projectName LoxScript
 * @description: TODO
 * @date 2020/12/299:37 PM
 */
public class JUtils {
  public static int toInt(Object value) {
    if (value instanceof Double) {
      return ((Double) value).intValue();
    }
    throw new RuntimeException("need number");
  }

  public static void JSThrowTypeErrorReadOnly(Token token) {
    throw new RuntimeError(token, token.lexeme + " is read-only");
  }

  public static byte[] intToByteArray(int i) {
    byte[] result = new byte[4];
    result[0] = (byte) ((i >> 24) & 0xFF);
    result[1] = (byte) ((i >> 16) & 0xFF);
    result[2] = (byte) ((i >> 8) & 0xFF);
    result[3] = (byte) (i & 0xFF);
    return result;
  }

  public static int byteArrToInteger(byte[] byteArr, int startIdx){
    return byteArrToInteger(byteArr, startIdx, 4);
  }

  public static int byteArrToShort(byte[] byteArr, int startIdx){
    return byteArrToInteger(byteArr, startIdx, 2);
  }

  public static int byteArrToInteger(byte[] byteArr, int startIdx, int len){
    int convertedInterger = 0;

    for(int i = 0; i < len; i++){

      byte curValue = byteArr[startIdx + len - 1 - i];
      long shiftedValue = curValue << (i * 8);
      long mask = 0xFF << (i * 8);
      long maskedShiftedValue = shiftedValue & mask;

      convertedInterger |= maskedShiftedValue;
    }

    return convertedInterger;
  }

  static void put_u32(final byte[] tab, int pos, JSAtom atom) {
    put_u32(tab, pos, atom.getVal());
  }

  static void put_u32(final byte[] tab, int pos, int val) {
    tab[pos + 0] = (byte) ((val >> 24) & 0xFF);
    tab[pos + 1] = (byte) ((val >> 16) & 0xFF);
    tab[pos + 2] = (byte) ((val >> 8) & 0xFF);
    tab[pos + 3] = (byte) (val & 0xFF);
  }

  static int get_i32(final byte[] tab, int pc)
  {
    return byteArrToInteger(tab, pc);
  }

  static int get_u32(final byte[] tab, int pc)
  {
    return byteArrToInteger(tab, pc);
  }

  static JSAtom get_atom(final byte[] tab, int pc)
  {
    return new JSAtom(byteArrToInteger(tab, pc));
  }

  static OPCodeEnum get_opcode(final byte[] tab, int pc)
  {
    return OPCodeEnum.values()[get_u8(tab, pc)];
  }

  static void put_u16(final byte[] tab, int pos, int val) {
    tab[pos + 0] =  (byte) ((val >> 8)  & 0xFF);
    tab[pos + 1] =  (byte) (val & 0xFF);
  }


  static int get_u16(final byte[] tab, int cp) {
    return byteArrToShort(tab, cp);
  }

  static int get_i16(final byte[] tab, int cp) {
    return 0XFFFF & tab[cp];
  }

  static void put_u8(final byte[] tab, int pos, OPCodeEnum op) {
    put_u8(tab, pos, op.ordinal());
  }

  static void put_u8(final byte[] tab, int pos, int val) {
    tab[pos + 0] =  (byte) (val & 0xFF);
  }


  static int get_u8(final byte[] tab, int cp) {
    return Byte.toUnsignedInt(tab[cp]);
  }

  static int get_i8(final byte[] tab, int cp) {
    return 0XFF & tab[cp];
  }
}
