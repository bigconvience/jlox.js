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
}
