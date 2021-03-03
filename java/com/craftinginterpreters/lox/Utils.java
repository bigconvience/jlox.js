package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: Utils
 * @projectName LoxScript
 * @description: TODO
 * @date 2020/12/299:37 PM
 */
public class Utils {
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

}
