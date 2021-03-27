//> Appendix II stmt
package com.lox.javascript;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);

    R visitClassStmt(Class stmt);

    R visitExpressionStmt(Expression stmt);

    R visitFunctionStmt(JSFunctionDef stmt);

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

    public Block(int line, List<Stmt> statements) {
      this.statements = statements;
      this.line_number = line;
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
    Class(Token name, List<JSFunctionDef> methods) {
      this.name = name;
      this.methods = methods;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    final Token name;
    final List<JSFunctionDef> methods;
  }

  //< stmt-class
//> stmt-expression
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    Expression(int line, Expr expression) {
      this.line_number = line;
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
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
    int end_line;
  }

  //< stmt-if
//> stmt-print
  static class Print extends Stmt {
    Print(int line, Expr expression) {
      this.expression = expression;
      this.line_number = line;
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
    Var(int line, final JSVarDefEnum varDef, JSAtom name,  Expr initializer) {
      this.line_number = line;
      this.varDef = varDef;
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    final JSAtom name;
    final JSVarDefEnum varDef;
    final Expr initializer;
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

  int line_number;

  abstract <R> R accept(Visitor<R> visitor);
}
//< Appendix II stmt
