package com.lox.test;

import java.io.PrintStream;

import static com.lox.javascript.lib.lox_lib.*;

import static com.lox.clibrary.stdio_h.*;
import static com.lox.clibrary.stdlib_h.*;
import static com.lox.clibrary.string_h.*;
import static com.lox.test.Test262.perror_exit;

/**
 * @author benpeng.jiang
 * @title: TestUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/2310:31 AM
 */
public class Test262Utils {
  static final String CMD_NAME = "run-test262";
  static final String CONFIG_VERSION = "0.01";

  static void fatal(int errorcode, final String fmt, Object... args) {
    fprintf(stderr, "%s: ", CMD_NAME);
    fprintf(stderr, fmt, args);
    perror("\n");
    exit(errorcode);
  }

  static String get_basename(String filename) {
    int p = filename.indexOf('/');

    if (p < 0)
      return null;
    return strdup_len(filename, p);
  }

  public static String str_append(String p, String sep, String str) {
    StringBuilder pp = new StringBuilder(p);
    pp.append(sep);
    pp.append(str);
    return pp.toString();
  }

  public static String str_append(String pp, String sep, char[] str) {
    return str_append(pp, sep, new String(str));
  }


  public static String compose_path(final String path, char[] name) {
    return compose_path(path, new String(name));
  }

  static String compose_path(final String path, final String name) {
    String d;

    if (TextUtils.isEmpty(path) || name.charAt(0) == '/') {
      d = name;
    } else {
      d = path + '/' + name;
    }
    return d;
  }

  public static char[] str_strip(char[] p) {
    String a = new String(p);
    return a.trim().toCharArray();
  }

  public static String load_file(String filename, int lenp)
  {
    byte[] buf;
    int buf_len = 0;
    buf = js_load_file(null, buf_len, filename);
    if (buf == null)
      perror_exit(1, filename);
    return new String(buf);
  }

  public static PrintStream fopen(String filename) {
    try {
      return new PrintStream(filename);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

}
