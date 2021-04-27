package com.lox.javascript.lib;

import com.lox.javascript.JSContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author benpeng.jiang
 * @title: lox_lib
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/249:06 AM
 */
public class lox_lib {
  public static byte[] js_load_file(JSContext ctx, int pbuf_len, String path) {
    byte[] bytes = new byte[0];
    try {
      bytes = Files.readAllBytes(Paths.get(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return bytes;
  }

  public static int getUnsignedByte(byte b) {
    return b & 0x0FF;
  }

  public static int getUnsignedShort(short data) {
    return data & 0x0FFFF;
  }

  public static long getUnsignedInt(int data) {
    // data & 0xFFFFFFFF 和 data & 0xFFFFFFFFL 结果是不同的，需要注意，有可能与 JDK 版本有关
    return data & 0xFFFFFFFFL;
  }
}
