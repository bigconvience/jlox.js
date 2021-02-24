package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: ParseUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/233:58 PM
 */
public class ParserUtils {
  public static JSFunctionDef jsNewFunctionDef(JSContext ctx, JSFunctionDef parent,
                                               boolean isEval, boolean isFuncExpr, String filename, int lineNum) {
    JSFunctionDef fd = new JSFunctionDef(parent, isEval, isFuncExpr, filename, lineNum);
    fd.scopeLevel = 0;
    fd.scopeFirst = -1;
    fd.addScope();
    fd.scopes.get(0).first = -1;
    fd.scopes.get(0).parent = -1;
    return fd;
  }

  public static boolean isFuncDecl(JSVarKindEnum varKind) {
    return varKind == JSVarKindEnum.JS_VAR_FUNCTION_DECL ||
      varKind == JSVarKindEnum.JS_VAR_NEW_FUNCTION_DECL;
  }
}
