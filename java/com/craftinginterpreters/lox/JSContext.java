package com.craftinginterpreters.lox;

import java.util.List;

/**
 * @author benpeng.jiang
 * @title: JSContext
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/222:00 PM
 */
public class JSContext {
  private static final Interpreter interpreter = new Interpreter();

  public JSValue globalObj;

  JSValue evalInternal(JSContext ctx, JSValue thisObject, String input, String filename, int flags, int scope_idx) {
    JSValue retVal = null;
    int err;
    int evalType = flags & JSEvaluator.JS_EVAL_TYPE_MASK;
    Scanner scanner = new Scanner(input);

    JSFunctionDef fd = ParserUtils.jsNewFunctionDef(ctx, null, true, false, filename, 1);
    fd.evalType = evalType;
    fd.funcName = "<eval>";


    Parser parser = new Parser(scanner, this, fd);
    parser.fileName = filename;
    parser.parseProgram();

    Resolver resolver = new Resolver(interpreter);
    resolver.visitFunctionStmt(fd);
    interpreter.evalFunction(fd);

    return retVal;
  }
}
