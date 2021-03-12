//> Appendix II expr
package com.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

abstract class Expr {
  interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitConditionExpr(Condition expr);
    R visitCoalesceExpr(Coalesce expr);
    R visitBinaryExpr(Binary expr);
    R visitCallExpr(Call expr);
    R visitGetExpr(Get expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitObjectLiteralExpr(ObjectLiteral expr);
    R visitLogicalExpr(Logical expr);
    R visitBitwiseExpr(Bitwise expr);
    R visitSetExpr(Set expr);
    R visitThisExpr(This expr);
    R visitUnaryExpr(Unary expr);
    R visitPostfixExpr(Postfix expr);
    R visitVariableExpr(Variable expr);
  }

  // Nested Expr classes here...
//> expr-assign
  static class Assign extends Expr {
    Assign( Expr.Variable left, TokenType operator, Expr value) {
      this.left = left;
      this.operator = operator;
      this.value = value;
    }

    public Assign(Variable left, TokenType operator, Expr value, PutLValueEnum putLValueEnum) {
      this.left = left;
      this.operator = operator;
      this.value = value;
      this.putLValueEnum = putLValueEnum;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Expr.Variable left;
    final TokenType operator;
    final Expr value;
    PutLValueEnum putLValueEnum;
  }
  //< expr-assign

  static class Condition extends Expr {
    Condition(Expr first, Expr middle, Expr last) {
      this.first = first;
      this.middle = middle;
      this.last = last;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitConditionExpr(this);
    }

    final Expr first;
    final Expr middle;
    final Expr last;
  }

  static class Coalesce extends Expr {
    Coalesce(Expr left, Expr right) {
      this.left = left;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCoalesceExpr(this);
    }

    final Expr left;
    final Expr right;
  }

//> expr-binary
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  //< expr-binary
//> expr-call
  static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
  }
  //< expr-call
//> expr-get
  static class Get extends Expr {
    Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetExpr(this);
    }

    final Expr object;
    final Token name;
  }
  //< expr-get
//> expr-grouping
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }
  //< expr-grouping
//> expr-literal
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }
  //< expr-literal
  static class ObjectLiteral extends Expr {
    ObjectLiteral(Map<String, Expr> value) {
      this.prop = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitObjectLiteralExpr(this);
    }

    final Map<String, Expr> prop;
  }
//> expr-logical
  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }

  static class Bitwise extends Expr {
    Bitwise(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBitwiseExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  //< expr-logical
//> expr-set
  static class Set extends Expr {
    Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetExpr(this);
    }

    final Expr object;
    final Token name;
    final Expr value;
  }
  //< expr-set
//> expr-this
  static class This extends Expr {
    This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisExpr(this);
    }

    final Token keyword;
  }
  //< expr-this
//> expr-unary
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
  //< expr-unary

  static class Postfix extends Expr {
    Postfix(Token operator, Expr left) {
      this.operator = operator;
      this.left = left;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPostfixExpr(this);
    }

    final Token operator;
    final Expr left;
  }
//> expr-variable
  static class Variable extends Expr {
    Variable(Token name, int scope_level) {
      this.name = name;
      this.scope_level = scope_level;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
    final int scope_level;
    TokenType tok;
  }
//< expr-variable

  abstract <R> R accept(Visitor<R> visitor);
}
//< Appendix II expr
