package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: PrintUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/1011:18 PM
 */
public class PrintUtils {
   static void println(String fmt) {
    System.out.println(fmt);
  }

   static void printf(String fmt) {
    System.out.printf(fmt);
  }

  static void print_atom(JSContext ctx, JSAtom prop) {
     ctx.print_atom(prop);
  }
}
