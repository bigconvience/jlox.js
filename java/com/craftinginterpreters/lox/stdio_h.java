package com.craftinginterpreters.lox;


import java.io.PrintStream;

/**
 * @author benpeng.jiang
 * @title: stdio
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/181:52 PM
 */
public class stdio_h {
  static final PrintStream stderr = System.err;

  public static void fprintf(PrintStream out, String format, Object ... args) {
    out.printf(format, args);
  }

  public static void printf(String format, Object ... args) {
     System.out.printf(format, args);
  }

  public static void perror(String format, Object ... args) {
    System.err.printf(format, args);
  }

  static void print_atom(JSContext ctx, JSAtom prop) {
    ctx.print_atom(prop);
  }
}
