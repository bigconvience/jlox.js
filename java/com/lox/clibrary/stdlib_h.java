package com.lox.clibrary;

/**
 * @author benpeng.jiang
 * @title: Stdlib
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/1810:40 AM
 */
public class stdlib_h {
  public static void abort() {
    System.exit(65);
  }

  public static void exit(int status) {
    System.exit(status);
  }

  public static int atoi(String str) {
    return Integer.valueOf(str);
  }
}
