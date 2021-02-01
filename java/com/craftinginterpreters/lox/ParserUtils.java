package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: ParseUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/233:58 PM
 */
public class ParserUtils {
  /**
   * scope caterpillar
   * @param fd
   * @param name
   * @param scope
   * @return
   */
  public static JSVarDef findLexicalDef(Stmt.Function fd, Token name, JSVarScope scope) {
    String varName = name.lexeme;
    while (scope != null) {
      JSVarDef varDef = scope.get(varName);
      if (varDef != null && varDef.isLexical) {
        return varDef;
      }
      scope = scope.prev;
    }

    if (fd.isEval && fd.evalType == JSEval.JS_EVAL_TYPE_GLOBAL) {
      return findLexicalHoistedDef(fd, name);
    }
    return null;
  }

  public static JSHoistedDef findLexicalHoistedDef(Stmt.Function fd, Token name) {
    JSHoistedDef hoistedDef = findHoistedDef(fd, name);
    if (hoistedDef != null && hoistedDef.isLexical) {
      return hoistedDef;
    }
    return null;
  }

  public static JSHoistedDef findHoistedDef(Stmt.Function fd, Token name) {
    if (fd != null) {
      return fd.findHoistedDef(name);
    }
    return null;
  }

  public JSVarDef findVarInChildScope(Stmt.Function fd, Token name, JSVarScope scope) {
    JSVarDef vd = scope.get(name.lexeme);
    if (vd != null && isChildScope(vd.scope, scope)) {
        return vd;
    }
    return null;
  }

  public static boolean isChildScope(JSVarScope scope, JSVarScope parentScope) {
    while (scope != null) {
      if(scope.prev == parentScope) {
        return true;
      }
      scope = scope.prev;
    }
    return false;
  }

  public static JSVarDef findVar(Stmt.Function fd, Token name) {
    JSVarDef vd = fd.getVarDef(name.lexeme);
    if (vd != null) {
      return vd;
    }

    return findArg(fd, name);
  }

  public static JSVarDef findArg(Stmt.Function fd, Token name) {
    return fd.getArgDef(name.lexeme);
  }

  public static JSHoistedDef addHoistedDef(Stmt.Function fd,
                                           Token name,
                                           boolean isLexical) {
    JSHoistedDef hd = fd.addHoistedDef(name);
    hd.name = name;
    hd.isLexical = isLexical;
    hd.scope = fd.curScope;
    return hd;
  }

  public static JSVarDef addScopeVar(Stmt.Function func, Token name, JSVarKindEnum varKind) {
    JSVarDef vd = new JSVarDef();
    vd.varKind = varKind;
    vd.scope = func.curScope;
    vd.name = name;
    func.curScope.addVar(vd);
    return vd;
  }

  public static JSVarDef addVar(Stmt.Function func, Token name) {
    JSVarDef vd = new JSVarDef();
    vd.name = name;
    func.addVarDef(name.lexeme, vd);
    return vd;
  }

  public static void pushScope(Stmt.Function curFunc) {
    if (curFunc != null) {
      JSVarScope varScope = new JSVarScope(curFunc.curScope);
      curFunc.curScope = varScope;
    }
  }

  public static void popScope(Stmt.Function curFunc) {
    if (curFunc != null) {
      JSVarScope varScope = curFunc.curScope;
      curFunc.curScope = varScope.prev;
    }
  }
}
