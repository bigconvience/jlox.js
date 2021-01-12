//> Appendix II stmt
package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);

    R visitClassStmt(Class stmt);

    R visitExpressionStmt(Expression stmt);

    R visitFunctionStmt(Function stmt);

    R visitIfStmt(If stmt);

    R visitPrintStmt(Print stmt);

    R visitReturnStmt(Return stmt);

    R visitVarStmt(Var stmt);

    R visitWhileStmt(While stmt);
  }

  // Nested Stmt classes here...
//> stmt-block
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }

  //< stmt-block
//> stmt-class
  static class Class extends Stmt {
    Class(Token name, List<Stmt.Function> methods) {
      this.name = name;
      this.methods = methods;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    final Token name;
    final List<Stmt.Function> methods;
  }

  //< stmt-class
//> stmt-expression
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }

  //< stmt-expression
//> stmt-function
  static class Function extends Stmt {
    Function(Token name, List<Token> params, final Function parent) {
      this.name = name;
      this.params = params;
      this.parent = parent;
      vars = new HashMap<>();
      hoistDef = new HashMap<>();
      scopes = new ArrayList<>();
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final List<Token> params;
    final Function parent;
    final List<JSVarScope> scopes;
    final Map<String, JSVarDef> vars;
    final Map<String, JSHoistedDef> hoistDef;
    List<Stmt> body;
    int evalType;
    boolean isGlobalVar;
    int scopeLevel;

    void addVarDef(String name, JSVarDef varDef) {
      vars.put(name, varDef);
    }

    JSVarDef getVarDef(String name) {
      return vars.get(name);
    }

    JSHoistedDef findHoistedDef(Token name) {
      return hoistDef.get(name.lexeme);
    }

    JSHoistedDef addHoistedDef(Token name) {
      JSHoistedDef hoistedDef = new JSHoistedDef();
      hoistedDef.name = name;
      hoistDef.put(name.lexeme, hoistedDef);
      return hoistedDef;
    }
  }

  //< stmt-function
//> stmt-if
  static class If extends Stmt {
    If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    final Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }

  //< stmt-if
//> stmt-print
  static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final Expr expression;
  }

  //< stmt-print
//> stmt-return
  static class Return extends Stmt {
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    final Token keyword;
    final Expr value;
  }

  //< stmt-return
//> stmt-var
  static class Var extends Stmt {
    Var(JSVarDefEnum varDefType, Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
      this.varDefType = varDefType;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    final Token name;
    final Expr initializer;
    final JSVarDefEnum varDefType;

  }

  //< stmt-var
//> stmt-while
  static class While extends Stmt {
    While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }
//< stmt-while

  abstract <R> R accept(Visitor<R> visitor);
}
//< Appendix II stmt
