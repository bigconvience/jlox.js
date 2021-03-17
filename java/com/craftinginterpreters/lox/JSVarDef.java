package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.JSAtomEnum.JS_ATOM_this;
import static com.craftinginterpreters.lox.JSContext.JS_MAX_LOCAL_VARS;
import static com.craftinginterpreters.lox.JSThrower.JS_ThrowInternalError;
import static com.craftinginterpreters.lox.LoxJS.JS_MODE_STRICT;

/**
 * @author benpeng.jiang
 * @title: JSVarDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2020/12/313:47 PM
 */
public class JSVarDef {
  Token name;
  boolean is_const;
  boolean is_lexical;
  int scope_level;
  int scope_next;
  JSVarScope scope;
  JSVarKindEnum var_kind;
  JSAtom var_name;
  int func_pool_or_scope_idx;
  boolean isGlobalVar;
  int cpool_idx;
  int varIdx;
  boolean forceInit;
  boolean is_captured;
  boolean is_func_var;


  static final int GLOBAL_VAR_OFFSET = 0x40000000;
  static final int ARGUMENT_VAR_OFFSET = 0x20000000;

  static int find_var(JSContext ctx, JSFunctionDef fd, JSAtom name)
  {
    int i;
    for(i = fd.vars.size(); i-- > 0;) {
      if (fd.vars.get(i).var_name == name && fd.vars.get(i).scope_level == 0)
        return i;
    }
    return find_arg(ctx, fd, name);
  }

  static int find_arg(JSContext ctx, JSFunctionDef fd, JSAtom name)
  {
    int i;
    for(i = fd.args.size(); i-- > 0;) {
      if (fd.args.get(i).var_name == name)
        return i | ARGUMENT_VAR_OFFSET;
    }
    return -1;
  }

  static int add_var(JSContext ctx, JSFunctionDef fd, JSAtom name)
  {
    JSVarDef vd = new JSVarDef();
//    printf("jbp level:%d, parent level:%d,add_var:", fd.scope_level, fd.parent_scope_level);
//    print_atom(ctx, name);printf("\n");
    /* the local variable indexes are currently stored on 16 bits */
    if (fd.vars.size() >= JS_MAX_LOCAL_VARS) {
      JS_ThrowInternalError(ctx, "too many local variables");
      return -1;
    }

    fd.vars.add(vd);
    vd.var_name = name;
    return fd.vars.size() - 1;
  }

  static int add_scope_var(JSContext ctx, JSFunctionDef fd, JSAtom name,
                           JSVarKindEnum var_kind)
  {
    int idx = add_var(ctx, fd, name);
    if (idx >= 0) {
      JSVarDef vd = fd.vars.get(idx);
      vd.var_kind = var_kind;
      vd.scope_level = fd.scope_level;
      vd.scope_next = fd.scope_first;
      fd.scopes.get(fd.scope_level).first = idx;
      fd.scope_first = idx;
    }
    return idx;
  }

  static int add_func_var(JSContext ctx, JSFunctionDef fd, JSAtom name)
  {
    int idx = fd.func_var_idx;
    if (idx < 0 && (idx = add_var(ctx, fd, name)) >= 0) {
      fd.func_var_idx = idx;
      fd.vars.get(idx).is_func_var = true;
      if ((fd.js_mode & JS_MODE_STRICT) != 0)
        fd.vars.get(idx).is_const = true;
    }
    return idx;
  }

  static int add_arguments_var(JSContext ctx, JSFunctionDef fd, JSAtom name)
  {
    int idx = fd.arguments_var_idx;
    if (idx < 0 && (idx = add_var(ctx, fd, name)) >= 0) {
      fd.arguments_var_idx = idx;
    }
    return idx;
  }


  static int add_arg(JSContext ctx, JSFunctionDef fd, JSAtom name)
  {
    JSVarDef vd = new JSVarDef();

    /* the local variable indexes are currently stored on 16 bits */
    if (fd.args.size() >= JS_MAX_LOCAL_VARS) {
      JS_ThrowInternalError(ctx, "too many arguments");
      return -1;
    }

    fd.args.add(vd);
    vd.var_name = name;
    return fd.args.size() - 1;
  }

  static int add_var_this(JSContext ctx, JSFunctionDef fd)
  {
    int idx;
    idx = add_var(ctx, fd, JS_ATOM_this.toJSAtom());
    if (idx >= 0 && fd.is_derived_class_constructor) {
      JSVarDef vd = fd.vars.get(idx);
      /* XXX: should have is_this flag or var type */
      vd.is_lexical = true; /* used to trigger 'uninitialized' checks
                               in a derived class constructor */
    }
    return idx;
  }
}
