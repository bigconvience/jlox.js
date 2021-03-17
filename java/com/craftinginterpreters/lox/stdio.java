package com.craftinginterpreters.lox;


/**
 * @author benpeng.jiang
 * @title: stdio
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/181:52 PM
 */
public class stdio {
  public static void printf(String format, Object ... args) {
     System.out.printf(format, args);
  }

  public static void err_printf(String format, Object ... args) {
    System.err.printf(format, args);
  }

}
