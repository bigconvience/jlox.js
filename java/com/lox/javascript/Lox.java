package com.lox.javascript;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.lox.javascript.JSRuntime.JS_NewCustomContext;
import static com.lox.javascript.LoxJS.js_std_add_helpers;

public class Lox {
  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64); // [64]
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }
  private static void runFile(String path) throws IOException {
    File file = new File(path);
    String filename = file.getAbsolutePath();
    byte[] bytes = Files.readAllBytes(Paths.get(filename));
    run(new String(bytes, Charset.defaultCharset()), filename);

    // Indicate an error in the exit code.
    if (hadError) System.exit(65);
    if (hadRuntimeError) System.exit(70);
  }
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) { // [repl]
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      run(line, "<prompt>");
      hadError = false;
    }
  }


  private static void run(String source, String filename) {
    if (source.length() == 0) {
      return;
    }
    JSRuntime rt = new JSRuntime();

    JSContext ctx = JS_NewCustomContext(rt);
    js_std_add_helpers(ctx, 0, new String[0]);
    LoxJS.JS_Eval(ctx, source, filename, LoxJS.JS_EVAL_TYPE_GLOBAL);
    // Stop if there was a syntax error.
    if (hadError) return;


    // Stop if there was a resolution error.
    if (hadError) return;

  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line_num, " at end", message);
    } else {
      report(token.line_num, " at '" + token.lexeme + "'", message);
    }
  }
  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() +
        "\n[line " + error.token.line_num + "]");
    hadRuntimeError = true;
  }
}
