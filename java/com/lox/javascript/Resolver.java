package com.lox.javascript;

import com.lox.clibrary.stdlib_h;

import java.util.*;

import static com.lox.javascript.DynBuf.*;
import static com.lox.javascript.FuncCallType.*;
import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSFunctionDef.*;
import static com.lox.javascript.JSFunctionKindEnum.*;
import static com.lox.javascript.JSVarDefEnum.*;
import static com.lox.javascript.LoxJS.*;
import static com.lox.javascript.OPCodeEnum.*;
import static com.lox.javascript.PutLValueEnum.*;
import static com.lox.clibrary.stdlib_h.abort;
import static com.lox.javascript.TokenType.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
  
static final int DECL_MASK_FUNC = (1 << 0); /* allow normal function declaration */
  /* ored with DECL_MASK_FUNC if function declarations are allowed with a label */
static final int DECL_MASK_FUNC_WITH_LABEL = (1 << 1);
static final int DECL_MASK_OTHER  = (1 << 2); /* all other declarations */
static final int DECL_MASK_ALL =  (DECL_MASK_FUNC | DECL_MASK_FUNC_WITH_LABEL | DECL_MASK_OTHER);
    
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  JSFunctionDef cur_func;
  private DynBuf bc;
  final JSContext ctx;
  private final JSRuntime rt;
  int last_line_num;
  boolean is_module;

  Resolver(JSContext jsContext, JSFunctionDef fd) {
    ctx = jsContext;
    cur_func = fd;
    bc = new DynBuf();
    cur_func.byte_code = bc;
    rt = ctx.rt;
    last_line_num = 0;
  }

  private enum FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD
  }

  private enum ClassType {
    NONE,
    CLASS
  }

  private ClassType currentClass = ClassType.NONE;

  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {

      resolve(statement);
    }
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    update_line(stmt.line_number);
    push_scope(this);
    resolve(stmt.statements);
    pop_scope(this);
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;

    declare(stmt.name);
    define(stmt.name);

    beginScope();
    scopes.peek().put("this", true);

    for (JSFunctionDef method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("init")) {
        declaration = FunctionType.INITIALIZER;
      }

      resolveFunction(method, declaration); // [local]
    }

    endScope();

    currentClass = enclosingClass;
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression);
    Resolver s = this;
    if (cur_func.eval_ret_idx >= 0) {
            /* store the expression value so that it can be returned
               by eval() */
      emit_op(s, OP_put_loc);
      emit_u16(s, cur_func.eval_ret_idx);
    } else {
      emit_op(s, OPCodeEnum.OP_drop); /* drop the result */
    }
    return null;
  }

  @Override
  public Void visitFunctionStmt(JSFunctionDef stmt) {
    JSFunctionDef fd = stmt;
    Resolver s = new Resolver(ctx, fd);
    s.update_line(fd.decl_line_number);
    s.push_scope(s);
    s.resolve(stmt.body);
    if (js_is_live_code(s)) {
      s.update_line(fd.leave_line_number);
      emit_return(s, false);
    }
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    int label1, label2, mask;
    Resolver s = this;
    push_scope(s);
    set_eval_ret_undefined(s);
    resolve(stmt.condition);
    update_line(stmt.line_number);
    label1 = emit_goto(s, OP_if_false, -1);
    if ((cur_func.js_mode & JS_MODE_STRICT) != 0) {
      mask = 0;
    } else {
      mask = DECL_MASK_FUNC;
    }
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) {
      update_line(stmt.elseBranch);
      label2 = emit_goto(s, OP_goto, -1);

      emit_label(s, label1);
      resolve(stmt.elseBranch);
      label1 = label2;
    }
    update_line(stmt.end_line);
    emit_label(s, label1);
    pop_scope(s);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Expr expr = stmt.expression;
    expr.accept(this);

    emit_op(OPCodeEnum.OP_print);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (stmt.value != null) {
      resolve(stmt.value);
      emit_return(this, true);
    } else {
      emit_return(this, false);
    }

    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Resolver s = this;
    JSFunctionDef fd = cur_func;

    DynBuf bc_buf = bc;
    JSVarDefEnum varDef = stmt.varDef;
    JSAtom name = stmt.name;
    JSVarUtils.define_var(s, fd, name, varDef);
    int scope = fd.scope_level;

    Expr initializer = stmt.initializer;
    if (initializer != null) {
      if (varDef == JS_VAR_DEF_VAR) {
        emit_op(OPCodeEnum.OP_scope_get_var);
        emit_u32(name);
        emit_u16(scope);
        LValue lValue = LValue.get_lvalue(this, bc_buf, false, TOK_ASSIGN);
        initializer.accept(this);
        LValue.put_lvalue(this, lValue, PUT_LVALUE_NOKEEP, false);
      } else {
        initializer.accept(this);
        emit_op((varDef == JS_VAR_DEF_LET || varDef == JS_VAR_DEF_CONST)
          ? OPCodeEnum.OP_scope_put_var_init : OPCodeEnum.OP_scope_put_var);
        emit_u32(name);
        emit_u16(scope);
      }
    } else {
      if (varDef == JS_VAR_DEF_LET) {
        emit_op(OP_undefined);
        emit_op(OPCodeEnum.OP_scope_put_var_init);
        emit_u32(name);
        emit_u16(scope);
      }
    }

    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    JSFunctionDef s = cur_func;
    Expr value = expr.value;
    Expr left = expr.left;
    left.accept(this);

    int tok = expr.operator.ordinal();

    DynBuf bc_buf = bc;

    if (tok == TOK_ASSIGN.ordinal()
      || tok >= TOK_MUL_ASSIGN.ordinal() && tok <= TOK_POW_ASSIGN.ordinal()) {
      LValue lValue = LValue.get_lvalue(this, bc_buf, tok != TOK_ASSIGN.ordinal(), TokenType.values()[tok]);
      value.accept(this);
      LValue.put_lvalue(this, lValue, PUT_LVALUE_KEEP_TOP, false);
    }
    return null;
  }

  @Override
  public Void visitConditionExpr(Expr.Condition expr) {
    resolve(expr.first);
    resolve(expr.middle);
    resolve(expr.last);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    OPCodeEnum opcode = null;
    Token tok = expr.operator;
    switch (tok.type) {
      case TOK_PLUS:
        opcode = OPCodeEnum.OP_add;
        break;
      case TOK_MINUS:
        opcode = OPCodeEnum.OP_sub;
        break;
      case TOK_STAR:
        opcode = OPCodeEnum.OP_mul;
        break;
      case TOK_SLASH:
        opcode = OPCodeEnum.OP_div;
        break;
      case TOK_LT:
        opcode = OPCodeEnum.OP_lt;
        break;
      case TOK_LTE:
        opcode = OPCodeEnum.OP_lte;
        break;
      case TOK_GT:
        opcode = OPCodeEnum.OP_gt;
        break;
      case TOK_GTE:
        opcode = OPCodeEnum.OP_gte;
        break;
      case TOK_EQ:
        opcode = OPCodeEnum.OP_eq;
        break;
      case TOK_NEQ:
        opcode = OPCodeEnum.OP_neq;
        break;
      case TOK_STRICT_EQ:
        opcode = OPCodeEnum.OP_strict_eq;
        break;
      case TOK_STRICT_NEQ:
        opcode = OPCodeEnum.OP_strict_neq;
        break;
      default:
        stdlib_h.abort();
    }
    emit_op(opcode);
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    Resolver s = this;
    OPCodeEnum opcode;
    int arg_count, drop_count;
    resolve(expr.callee);
    JSFunctionDef fd = cur_func;
    switch (opcode = get_prev_opcode(fd)) {
      case OP_get_field:
        fd.byte_code.buf[fd.last_opcode_pos] = (byte) OP_get_field2.ordinal();
        drop_count = 2;
        break;
    }
    emit_func_call(opcode, s, expr.arguments.size(), fd, expr.call_type);

    return null;
  }

  private static void emit_func_call(OPCodeEnum opcode, Resolver s, int arg_count,
                              JSFunctionDef fd, FuncCallType call_type) {
    switch(opcode) {
      case OP_get_field:
      case OP_scope_get_private_field:
      case OP_get_array_el:
      case OP_scope_get_ref:
        emit_op(s, OP_call_method);
        emit_u16(s, arg_count);
        break;
      case OP_eval:
        emit_op(s, OP_eval);
        emit_u16(s, arg_count);
        emit_u16(s, fd.scope_level);
        fd.has_eval_call = TRUE;
        break;
      default:
        if (call_type == FUNC_CALL_SUPER_CTOR) {
          emit_op(s, OP_call_constructor);
          emit_u16(s, arg_count);

          /* set the 'this' value */
          emit_op(s, OP_dup);
          emit_op(s, OP_scope_put_var_init);
          emit_atom(s, JS_ATOM_this);
          emit_u16(s, 0);

          emit_class_field_init(s);
        } else if (call_type == FUNC_CALL_NEW) {
          emit_op(s, OP_call_constructor);
          emit_u16(s, arg_count);
        } else {
          emit_op(s, OP_call);
          emit_u16(s, arg_count);
        }
        break;
    }
  }

  /* initialize the class fields, called by the constructor. Note:
   super() can be called in an arrow function, so <this> and
   <class_fields_init> can be variable references */
  static void emit_class_field_init(Resolver s)
  {
    int label_next;

    emit_op(s, OP_scope_get_var);
    emit_atom(s, JS_ATOM_class_fields_init);
    emit_u16(s, s.cur_func.scope_level);

    /* no need to call the class field initializer if not defined */
    emit_op(s, OP_dup);
    label_next = emit_goto(s, OP_if_false, -1);

    emit_op(s, OP_scope_get_var);
    emit_atom(s, JS_ATOM_this);
    emit_u16(s, 0);

    emit_op(s, OP_swap);

    emit_op(s, OP_call_method);
    emit_u16(s, 0);

    emit_label(s, label_next);
    emit_op(s, OP_drop);
  }

  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object);
    Resolver s = this;
    emit_op(s, OP_get_field);
    emit_atom(s, expr.name.ident_atom);
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    Object val = expr.value;
    if (val instanceof JSAtom) {
      emit_op(OPCodeEnum.OP_push_atom_value);
      put_value(bc, val);
    } else if (val instanceof Integer) {
      emit_op(OPCodeEnum.OP_push_i32);
      emit_u32((Integer) val);
    } else if (val instanceof Boolean) {
      emit_op((Boolean) val ? OPCodeEnum.OP_push_true : OPCodeEnum.OP_push_false);
    } else {
      emit_op(OP_null);
    }
    return null;
  }

  @Override
  public Void visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    Resolver s = this;
    TokenType op = expr.operator.type;
    resolve(expr.left);

    int label1 = new_label(s);
    emit_op(s, OP_dup);
    emit_goto(s, op == TOK_LAND ? OP_if_false : OP_if_true, label1);
    emit_op(s, OP_drop);
    resolve(expr.right);
    emit_label(s, label1);

    return null;
  }

  @Override
  public Void visitBitwiseExpr(Expr.Bitwise expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitThisExpr(Expr.This expr) {
    if (currentClass == ClassType.NONE) {
      Lox.error(expr.keyword,
        "Cannot use 'this' outside of a class.");
      return null;
    }

    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    Resolver s = this;
    TokenType op = expr.operator.type;
    switch (op) {
      case TOK_PLUS:
      case TOK_MINUS:
      case TOK_BANG:
      case TOK_BITWISE_BANG:
      case TOK_VOID:
        resolve(expr.right);
        switch (op) {
          case TOK_MINUS:
            emit_op(s, OPCodeEnum.OP_neg);
            break;
          case TOK_PLUS:
            emit_op(s, OPCodeEnum.OP_plus);
            break;
          case TOK_BANG:
            emit_op(s, OPCodeEnum.OP_lnot);
            break;
          case TOK_BITWISE_BANG:
            emit_op(s, OPCodeEnum.OP_not);
            break;
          case TOK_VOID:
            emit_op(s, OPCodeEnum.OP_drop);
            emit_op(s, OP_undefined);
            break;
          default:
            abort();
        }
        break;
    }
    return null;
  }

  @Override
  public Void visitPostfixExpr(Expr.Postfix expr) {
    resolve(expr.left);
    return null;
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    JSAtom name = expr.name.ident_atom;
    int scope = cur_func.scope_level;
    emit_op(OPCodeEnum.OP_scope_get_var);
    emit_u32(name);
    emit_u16(scope);

    return null;
  }


  @Override
  public Void visitCoalesceExpr(Expr.Coalesce expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  public static int js_resolve_program(Resolver s) {
    JSFunctionDef fd = s.cur_func;
    s.resolve(fd.body);
    s.update_line(fd.leave_line_number);
    if (!s.is_module) {
      /* return the value of the hidden variable eval_ret_idx  */
      emit_op(s, OPCodeEnum.OP_get_loc);
      emit_u16(s, fd.eval_ret_idx);

      emit_op(s, OPCodeEnum.OP_return);
    } else {
      emit_op(s, OPCodeEnum.OP_return_undef);
    }
    return 0;
  }

  public void update_line(Stmt stmt) {
    update_line(stmt.line_number);
  }

 public void update_line(int line_number) {
   last_line_num = line_number;
 }

  private void resolve(Stmt stmt) {
    update_line(stmt);
    stmt.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void resolveFunction(
    JSFunctionDef function, FunctionType type) {

    JSFunctionDef enclosureFunc = cur_func;
    cur_func = function;

    beginScope();
    for (Token param : function.params) {
      declare(param);
      define(param);
    }

    resolve(function.body);
    enter_scope(function, 1, null);
    endScope();
    cur_func = enclosureFunc;
  }

  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) return;

    Map<String, Boolean> scope = scopes.peek();
    if (scope.containsKey(name.lexeme)) {
      Lox.error(name,
        "Variable with this name already declared in this scope.");
    }

    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        return;
      }
    }

    // Not found. Assume it is global.
  }

  private void enter_scope(JSFunctionDef s, int scope, DynBuf bcOut) {
    if (scope == 1) {

    }

    for (int scopeIdx = s.scopes.get(scope).first; scopeIdx >= 0; ) {
      JSVarDef vd = s.vars.get(scopeIdx);
      if (vd.scope_level == scopeIdx) {
        if (JSFunctionDef.isFuncDecl(vd.var_kind)) {
          bcOut.dbuf_putc(OPCodeEnum.OP_fclosure);
          bcOut.dbuf_put_u32(vd.func_pool_or_scope_idx);
          bcOut.dbuf_putc(OP_put_loc);
        } else {
          bcOut.dbuf_putc(OPCodeEnum.OP_set_loc_uninitialized);
        }
        bcOut.dbuf_put_u16((short) scopeIdx);
        scopeIdx = vd.scope_next;
      } else {
        break;
      }
    }
  }

  static int emit_op(Resolver s, OPCodeEnum opCodeEnum) {
    return s.emit_op(opCodeEnum);
  }

  static int emit_op(Resolver s, byte val) {
    return s.emit_op(val);
  }

  static int emit_op(Resolver s, int opcode) {
    return s.emit_op((byte)(0xFF & opcode));
  }
  int emit_op(OPCodeEnum opCodeEnum) {
    return emit_op((byte) opCodeEnum.ordinal());
  }

  int emit_op(byte val) {
    Resolver s = this;
    JSFunctionDef fd = s.cur_func;
    DynBuf bc = s.cur_func.byte_code;

    if (fd.last_opcode_line_num != s.last_line_num) {
      bc.dbuf_putc(OPCodeEnum.OP_line_num);
      bc.dbuf_put_u32(s.last_line_num);
      fd.last_opcode_line_num = s.last_line_num;
    }
    fd.last_opcode_pos = bc.size;
    return bc.dbuf_putc(val);
  }

  public static int emit_label(Resolver s, int label) {
    return s.emit_label(label);
  }
  int emit_label(int label) {
    if (label >= 0) {
      emit_op(OPCodeEnum.OP_label);
      emit_u32(label);
      cur_func.label_slots.get(label).pos = cur_func.byte_code.size;
      return cur_func.byte_code.size - 4;
    } else {
      return -1;
    }
  }

  static int emit_atom(Resolver s, JSAtomEnum atom) {
    return s.emit_u32(atom.toJSAtom());
  }

  static int emit_atom(Resolver s, JSAtom atom) {
    return s.emit_u32(atom);
  }
  int emit_u32(JSAtom atom) {
    return emit_u32(atom.getVal());
  }

  int emit_u32(int val) {
    return bc.dbuf_put_u32(val);
  }

  int emit_u16(int val) {
    return bc.dbuf_put_u16(val);
  }

  static int emit_u16(Resolver s, int val) {
    return s.emit_u16(val);
  }

  static int emit_u32(Resolver s, int val) {
    return s.emit_u32(val);
  }


  static int new_label(Resolver s) {
    return s.cur_func.new_label_fd();
  }

  static int emit_goto(Resolver s, OPCodeEnum opcode, int label) {
    if (js_is_live_code(s)) {
      if (label < 0)
        label = new_label(s);
      emit_op(s, opcode);
      emit_u32(s, label);
      s.cur_func.label_slots.get(label).ref_count++;
      return label;
    }
    return -1;
  }

  static OPCodeEnum get_prev_opcode(JSFunctionDef fd) {
      return DynBuf.getOPCode(fd.byte_code.buf, fd.last_opcode_pos);
  }

  static boolean js_is_live_code(Resolver s) {
    switch (get_prev_opcode(s.cur_func)) {
      case OP_tail_call:
      case OP_tail_call_method:
      case OP_return:
      case OP_return_undef:
      case OP_return_async:
      case OP_throw:
      case OP_throw_var:
      case OP_goto:
      case OP_goto8:
      case OP_goto16:
      case OP_ret:
        return FALSE;
      default:
        return TRUE;
    }
  }

  static void set_eval_ret_undefined(Resolver s)
  {
    if (s.cur_func.eval_ret_idx >= 0) {
      emit_op(s, OP_undefined);
      emit_op(s, OP_put_loc);
      emit_u16(s, s.cur_func.eval_ret_idx);
    }
  }

  static int push_scope(Resolver s) {
    if (s.cur_func != null) {
      JSFunctionDef fd = s.cur_func;
      int scope = fd.add_scope();
      emit_op(s, OP_enter_scope);
      emit_u16(s, scope);
      return scope;
    }
    return 0;
  }

  static void pop_scope(Resolver s) {
    if (s.cur_func != null) {
      /* disable scoped variables */
      JSFunctionDef fd = s.cur_func;
      int scope = fd.scope_level;
      emit_op(s, OP_leave_scope);
      emit_u16(s, scope);
      fd.scope_level = fd.scopes.get(scope).parent;
      fd.scope_first = get_first_lexical_var(fd, fd.scope_level);
    }
  }

  /* execute the finally blocks before return */
  static void emit_return(Resolver s, boolean hasval)
  {
    BlockEnv top;
    int drop_count;

    drop_count = 0;
    top = s.cur_func.top_break;
    while (top != null) {
        /* XXX: emit the appropriate OP_leave_scope opcodes? Probably not
           required as all local variables will be closed upon returning
           from JS_CallInternal, but not in the same order. */
      if (top.has_iterator != 0) {
            /* with 'yield', the exact number of OP_drop to emit is
               unknown, so we use a specific operation to look for
               the catch offset */
        if (!hasval) {
          emit_op(s, OP_undefined);
          hasval = TRUE;
        }
        emit_op(s, OP_iterator_close_return);
        if (s.cur_func.func_kind == JS_FUNC_ASYNC_GENERATOR) {
          int label_next;
          emit_op(s, OP_async_iterator_close);
          label_next = emit_goto(s, OP_if_true, -1);
          emit_op(s, OP_await);
          emit_label(s, label_next);
          emit_op(s, OP_drop);
        } else {
          emit_op(s, OP_iterator_close);
        }
        drop_count = -3;
      }
      drop_count += top.drop_count;
      if (top.label_finally != -1) {
        while(drop_count > 0) {
          /* must keep the stack top if hasval */
          emit_op(s, hasval ? OP_nip : OP_drop);
          drop_count--;
        }
        if (!hasval) {
          /* must push return value to keep same stack size */
          emit_op(s, OP_undefined);
          hasval = TRUE;
        }
        emit_goto(s, OP_gosub, top.label_finally);
      }
      top = top.prev;
    }
    if (s.cur_func.is_derived_class_constructor) {
      int label_return;

        /* 'this' can be uninitialized, so it may be accessed only if
           the derived class constructor does not return an object */
      if (hasval) {
        emit_op(s, OP_check_ctor_return);
        label_return = emit_goto(s, OP_if_false, -1);
        emit_op(s, OP_drop);
      } else {
        label_return = -1;
      }

        /* XXX: if this is not initialized, should throw the
           ReferenceError in the caller realm */
      emit_op(s, OP_scope_get_var);
      emit_atom(s, JS_ATOM_this);
      emit_u16(s, 0);

      emit_label(s, label_return);
      emit_op(s, OP_return);
    } else if (s.cur_func.func_kind != JS_FUNC_NORMAL) {
      if (!hasval) {
        emit_op(s, OP_undefined);
      } else if (s.cur_func.func_kind == JS_FUNC_ASYNC_GENERATOR) {
        emit_op(s, OP_await);
      }
      emit_op(s, OP_return_async);
    } else {
      emit_op(s, hasval ? OP_return : OP_return_undef);
    }
  }
}
