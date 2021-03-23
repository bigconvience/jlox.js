package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: Stdlib
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/1810:40 AM
 */
public class stdlib_h {
  static void abort() {
    System.exit(65);
  }

  static void exit(int status) {
    System.exit(status);
  }
}
