package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.DynBuf.dbuf_put_u16;
import static com.craftinginterpreters.lox.DynBuf.dbuf_putc;
import static com.craftinginterpreters.lox.JSAtomEnum.*;
import static com.craftinginterpreters.lox.JSContext.JS_MAX_LOCAL_VARS;
import static com.craftinginterpreters.lox.JSThrower.*;
import static com.craftinginterpreters.lox.JSVarDef.*;
import static com.craftinginterpreters.lox.JSVarKindEnum.JS_VAR_NORMAL;
import static com.craftinginterpreters.lox.JUtils.*;
import static com.craftinginterpreters.lox.OPCodeEnum.*;

/**
 * @author benpeng.jiang
 * @title: ScopVarResolver
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/1712:01 PM
 */
public class ScopeVarResolver {
  static int resolve_scope_var(JSContext ctx, JSFunctionDef s,
                               JSAtom var_name, int scope_level, int op,
                               DynBuf bc, byte[] bc_buf,
                               LabelSlot ls, int pos_next, int arg_valid) {
    int idx, var_idx;
    boolean is_put;
    int label_done;
    boolean is_func_var = false;
    JSFunctionDef fd;
    JSVarDef vd;
    boolean is_pseudo_var;
    OPCodeEnum opCode = OPCodeEnum.values()[op];

    label_done = -1;

    /* XXX: could be simpler to use a specific function to
       resolve the pseudo variables */
    is_pseudo_var = (var_name == JS_ATOM_home_object.toJSAtom() ||
      var_name == JS_ATOM_this_active_func.toJSAtom() ||
      var_name == JS_ATOM_new_target.toJSAtom() ||
      var_name == JS_ATOM_this.toJSAtom());

    /* resolve local scoped variables */
    var_idx = -1;
    for (idx = s.scopes.get(scope_level).first; idx >= 0; ) {
      vd = s.vars.get(idx);
      if (vd.var_name == var_name) {
        if (op == OP_scope_put_var.ordinal() || op == OP_scope_make_ref.ordinal()) {
          if (vd.is_const) {
            bc.dbuf_putc(OP_throw_var);
            bc.dbuf_put_u32(var_name);
            bc.dbuf_putc(JS_THROW_VAR_RO);
            on_resolve_done(label_done, bc, s);
            return pos_next;
          }
          is_func_var = vd.is_func_var;
        }
        var_idx = idx;
        break;
      } else if (vd.var_name == JS_ATOM__with_.toJSAtom() && !is_pseudo_var) {
        bc.dbuf_putc(OP_get_loc);
        dbuf_put_u16(bc, idx);
        bc.dbuf_putc(get_with_scope_opcode(op));
        bc.dbuf_put_u32(var_name);
        label_done = s.new_label_fd(label_done);
        bc.dbuf_put_u32(label_done);
        bc.dbuf_putc(1);
        s.update_label(label_done, 1);
        s.jump_size++;
      }
      idx = vd.scope_next;
    }
    if (var_idx < 0) {
        /* XXX: scoping issues:
           should not resolve vars from the function body during argument parse,
           `arguments` and function-name should not be hidden by later vars.
         */
      var_idx = find_var(ctx, s, var_name);
      if (var_idx >= 0) {
        if (scope_level == 0
          && (var_idx & ARGUMENT_VAR_OFFSET) != 0
          && (var_idx - ARGUMENT_VAR_OFFSET) >= arg_valid) {
          /* referring to an uninitialized argument */
          bc.dbuf_putc(OP_throw_var);
          bc.dbuf_put_u32(var_name);
          bc.dbuf_putc(JS_THROW_VAR_UNINITIALIZED);
        }
        if ((var_idx & ARGUMENT_VAR_OFFSET) == 0)
          is_func_var = s.vars.get(var_idx).is_func_var;
      }

      if (var_idx < 0 && is_pseudo_var)
        var_idx = resolve_pseudo_var(ctx, s, var_name);

      if (var_idx < 0 && var_name == JS_ATOM_arguments.toJSAtom() &&
        s.has_arguments_binding) {
        /* 'arguments' pseudo variable */
        var_idx = add_arguments_var(ctx, s, var_name);
      }
      if (var_idx < 0 && s.is_func_expr && var_name == s.func_name) {
        /* add a new variable with the function name */
        var_idx = add_func_var(ctx, s, var_name);
        is_func_var = true;
      }
    }
    if (var_idx >= 0) {
        /* OP_scope_put_var_init is only used to initialize a
           lexical variable, so it is never used in a with or var object. It
           can be used with a closure (module global variable case). */
      switch (opCode) {
        case OP_scope_make_ref:
          if (is_func_var) {
            /* Create a dummy object reference for the func_var */
            bc.dbuf_putc(OP_object);
            bc.dbuf_putc(OP_get_loc);
            dbuf_put_u16(bc, var_idx);
            bc.dbuf_putc(OP_define_field);
            bc.dbuf_put_u32(var_name);
            bc.dbuf_putc(OP_push_atom_value);
            bc.dbuf_put_u32(var_name);
          } else if (label_done == -1 && can_opt_put_ref_value(bc_buf, ls.pos)) {
            int get_op;
            if ((var_idx & ARGUMENT_VAR_OFFSET) != 0) {
              get_op = OP_get_arg.ordinal();
              var_idx -= ARGUMENT_VAR_OFFSET;
            } else {
              if (s.vars.get(var_idx).is_lexical)
                get_op = OP_get_loc_check.ordinal();
              else
                get_op = OP_get_loc.ordinal();
            }
            pos_next = optimize_scope_make_ref(ctx, s, bc, bc_buf, ls,
              pos_next, get_op, var_idx);
          } else {
                /* Create a dummy object with a named slot that is
                   a reference to the local variable */
            if ((var_idx & ARGUMENT_VAR_OFFSET) != 0) {
              bc.dbuf_putc(OP_make_arg_ref);
              bc.dbuf_put_u32(var_name);
              dbuf_put_u16(bc, var_idx - ARGUMENT_VAR_OFFSET);
            } else {
              bc.dbuf_putc(OP_make_loc_ref);
              bc.dbuf_put_u32(var_name);
              dbuf_put_u16(bc, var_idx);
            }
          }
          break;
        case OP_scope_get_ref:
          bc.dbuf_putc(OP_undefined);
          /* fall thru */
        case OP_scope_get_var_undef:
        case OP_scope_get_var:
        case OP_scope_put_var:
        case OP_scope_put_var_init:
          is_put = (op == OP_scope_put_var.ordinal() || op == OP_scope_put_var_init.ordinal());
          if ((var_idx & ARGUMENT_VAR_OFFSET) != 0) {
            bc.dbuf_putc(OP_get_arg.ordinal() + (is_put ? 1 : 0));
            dbuf_put_u16(bc, var_idx - ARGUMENT_VAR_OFFSET);
            /* XXX: should test if argument reference needs TDZ check */
          } else {
            if (is_put) {
              if (s.vars.get(var_idx).is_lexical) {
                if (op == OP_scope_put_var_init.ordinal()) {
                  /* 'this' can only be initialized once */
                  if (var_name == JS_ATOM_this.toJSAtom())
                    bc.dbuf_putc(OP_put_loc_check_init);
                  else
                    bc.dbuf_putc(OP_put_loc);
                } else {
                  bc.dbuf_putc(OP_put_loc_check);
                }
              } else {
                bc.dbuf_putc(OP_put_loc);
              }
            } else {
              if (s.vars.get(var_idx).is_lexical) {
                bc.dbuf_putc(OP_get_loc_check);
              } else {
                bc.dbuf_putc(OP_get_loc);
              }
            }
            dbuf_put_u16(bc, var_idx);
          }
          break;
        case OP_scope_delete_var:
          bc.dbuf_putc(OP_push_false);
          break;
      }
      on_resolve_done(label_done, bc, s);
      return pos_next;
    }
    /* check eval object */
    if (s.var_object_idx >= 0 && !is_pseudo_var) {
      bc.dbuf_putc(OP_get_loc);
      dbuf_put_u16(bc, s.var_object_idx);
      bc.dbuf_putc(get_with_scope_opcode(op));
      bc.dbuf_put_u32(var_name);
      label_done = s.new_label_fd(label_done);
      bc.dbuf_put_u32(label_done);
      bc.dbuf_putc(0);
      s.update_label(label_done, 1);
      s.jump_size++;
    }
    /* check parent scopes */
    for (fd = s; fd.parent != null; ) {
      scope_level = fd.parent_scope_level;
      fd = fd.parent;
      if (scope_level == 0) {
            /* function is defined as part of the argument parsing: hide vars
               from the function body.
               XXX: variables created from argument destructuring might need
               to be visible, should refine this method.
             */
        var_idx = find_arg(ctx, fd, var_name);
      } else {
        for (idx = fd.scopes.get(scope_level).first; idx >= 0; ) {
          vd = fd.vars.get(idx);
          if (vd.var_name == var_name) {
            if (op == OP_scope_put_var.ordinal() || op == OP_scope_make_ref.ordinal()) {
              if (vd.is_const) {
                bc.dbuf_putc(OP_throw_var);
                bc.dbuf_put_u32(var_name);
                bc.dbuf_putc(JS_THROW_VAR_RO);
                on_resolve_done(label_done, bc, s);
                return pos_next;
              }
              is_func_var = vd.is_func_var;
            }
            var_idx = idx;
            break;
          } else if (vd.var_name == JS_ATOM__with_.toJSAtom() && !is_pseudo_var) {
            vd.is_captured = true;
            idx = get_closure_var(ctx, s, fd, false, idx, vd.var_name, false, false, JS_VAR_NORMAL);
            if (idx >= 0) {
              bc.dbuf_putc(OP_get_var_ref);
              dbuf_put_u16(bc, idx);
              bc.dbuf_putc(get_with_scope_opcode(op));
              bc.dbuf_put_u32(var_name);
              label_done = s.new_label_fd(label_done);
              bc.dbuf_put_u32(label_done);
              bc.dbuf_putc(1);
              s.update_label(label_done, 1);
              s.jump_size++;
            }
          }
          idx = vd.scope_next;
        }
        if (var_idx >= 0)
          break;

        var_idx = find_var(ctx, fd, var_name);
      }
      if (var_idx >= 0) {
        if ((var_idx & ARGUMENT_VAR_OFFSET) == 0)
          is_func_var = fd.vars.get(var_idx).is_func_var;
        break;
      }
      if (is_pseudo_var) {
        var_idx = resolve_pseudo_var(ctx, fd, var_name);
        if (var_idx >= 0)
          break;
      }
      if (var_name == JS_ATOM_arguments.toJSAtom() && fd.has_arguments_binding) {
        var_idx = add_arguments_var(ctx, fd, var_name);
        break;
      }
      if (fd.is_func_expr && fd.func_name == var_name) {
        /* add a new variable with the function name */
        var_idx = add_func_var(ctx, fd, var_name);
        is_func_var = true;
        break;
      }

      /* check eval object */
      if (fd.var_object_idx >= 0 && !is_pseudo_var) {
        fd.vars.get(fd.var_object_idx).is_captured = true;
        idx = get_closure_var(ctx, s, fd, false,
          fd.var_object_idx, JS_ATOM__var_.toJSAtom(),
          false, false, JS_VAR_NORMAL);
        bc.dbuf_putc(OP_get_var_ref);
        dbuf_put_u16(bc, idx);
        bc.dbuf_putc(get_with_scope_opcode(op));
        bc.dbuf_put_u32(var_name);
        label_done = s.new_label_fd(label_done);
        bc.dbuf_put_u32(label_done);
        bc.dbuf_putc(0);
        s.update_label(label_done, 1);
        s.jump_size++;
      }

      if (fd.is_eval)
        break; /* it it necessarily the top level function */
    }

    /* check direct eval scope (in the closure of the eval function
       which is necessarily at the top level) */
    if (fd != null)
      fd = s;
    if (var_idx < 0 && fd.is_eval) {
      int idx1;
      for (idx1 = 0; idx1 < fd.closure_var.size(); idx1++) {
        JSClosureVar cv = fd.closure_var.get(idx1);
        if (var_name == cv.var_name) {
          if (fd != s) {
            idx = get_closure_var2(ctx, s, fd,
              false,
              cv.is_arg, idx1,
              cv.var_name, cv.is_const,
              cv.is_lexical, cv.var_kind);
          } else {
            idx = idx1;
          }
          return on_has_idx(ctx, opCode, s, bc, var_name, idx, label_done, is_func_var, pos_next, bc_buf, ls);
        } else if ((cv.var_name == JS_ATOM__var_.toJSAtom() ||
          cv.var_name == JS_ATOM__with_.toJSAtom()) && !is_pseudo_var) {
          int is_with = (cv.var_name == JS_ATOM__with_.toJSAtom()) ? 1 : 0;
          if (fd != s) {
            idx = get_closure_var2(ctx, s, fd,
              false,
              cv.is_arg, idx1,
              cv.var_name, false, false,
              JS_VAR_NORMAL);
          } else {
            idx = idx1;
          }
          bc.dbuf_putc(OP_get_var_ref);
          dbuf_put_u16(bc, idx);
          bc.dbuf_putc(get_with_scope_opcode(op));
          bc.dbuf_put_u32(var_name);
          label_done = s.new_label_fd(label_done);
          bc.dbuf_put_u32(label_done);
          bc.dbuf_putc(is_with);
          s.update_label(label_done, 1);
          s.jump_size++;
        }
      }
    }

    if (var_idx >= 0) {
      /* find the corresponding closure variable */
      if ((var_idx & ARGUMENT_VAR_OFFSET) != 0) {
        fd.args.get(var_idx - ARGUMENT_VAR_OFFSET).is_captured = true;
        idx = get_closure_var(ctx, s, fd,
          true, var_idx - ARGUMENT_VAR_OFFSET,
          var_name, false, false, JS_VAR_NORMAL);
      } else {
        fd.vars.get(var_idx).is_captured = true;
        idx = get_closure_var(ctx, s, fd,
          false, var_idx,
          var_name,
          fd.vars.get(var_idx).is_const,
          fd.vars.get(var_idx).is_lexical,
          fd.vars.get(var_idx).var_kind);
      }
      if (idx >= 0) {
        return on_has_idx(ctx, opCode, s, bc, var_name, idx, label_done, is_func_var, pos_next, bc_buf, ls);
      }
    }

    /* global variable access */
    switch (opCode) {
      case OP_scope_make_ref:
        if (label_done == -1 && can_opt_put_global_ref_value(bc_buf, ls.pos)) {
          pos_next = optimize_scope_make_global_ref(ctx, s, bc, bc_buf, ls,
            pos_next, var_name);
        } else {
          bc.dbuf_putc(OP_make_var_ref);
          bc.dbuf_put_u32(var_name);
        }
        break;
      case OP_scope_get_ref:
        /* XXX: should create a dummy object with a named slot that is
           a reference to the global variable */
        bc.dbuf_putc(OP_undefined);
        bc.dbuf_putc(OP_get_var);
        bc.dbuf_put_u32(var_name);
        break;
      case OP_scope_get_var_undef:
      case OP_scope_get_var:
      case OP_scope_put_var:
        bc.dbuf_putc(OP_get_var_undef.ordinal() + (op - OP_scope_get_var_undef.ordinal()));
        bc.dbuf_put_u32(var_name);
        break;
      case OP_scope_put_var_init:
        bc.dbuf_putc(OP_put_var_init);
        bc.dbuf_put_u32(var_name);
        break;
      case OP_scope_delete_var:
        bc.dbuf_putc(OP_delete_var);
        bc.dbuf_put_u32(var_name);
        break;
    }
    on_resolve_done(label_done, bc, s);
    return pos_next;
  }

  private static int on_has_idx(JSContext ctx, OPCodeEnum opCode, JSFunctionDef s, DynBuf bc, JSAtom var_name,
                                int idx, int label_done, boolean is_func_var, int pos_next, byte[] bc_buf,
                                LabelSlot ls) {
    if ((opCode == OP_scope_put_var || opCode == OP_scope_make_ref) &&
      s.closure_var.get(idx).is_const) {
      bc.dbuf_putc(OP_throw_var);
      bc.dbuf_put_u32(var_name);
      bc.dbuf_putc(JS_THROW_VAR_RO);
      on_resolve_done(label_done, bc, s);
      return pos_next;
    }
    switch (opCode) {
      case OP_scope_make_ref:
        if (is_func_var) {
          /* Create a dummy object reference for the func_var */
          bc.dbuf_putc(OP_object);
          bc.dbuf_putc(OP_get_var_ref);
          dbuf_put_u16(bc, idx);
          bc.dbuf_putc(OP_define_field);
          bc.dbuf_put_u32(var_name);
          bc.dbuf_putc(OP_push_atom_value);
          bc.dbuf_put_u32(var_name);
        } else if (label_done == -1 &&
          can_opt_put_ref_value(bc_buf, ls.pos)) {
          int get_op;
          if (s.closure_var.get(idx).is_lexical)
            get_op = OP_get_var_ref_check.ordinal();
          else
            get_op = OP_get_var_ref.ordinal();
          pos_next = optimize_scope_make_ref(ctx, s, bc, bc_buf, ls,
            pos_next,
            get_op, idx);
        } else {
                    /* Create a dummy object with a named slot that is
                       a reference to the closure variable */
          bc.dbuf_putc(OP_make_var_ref_ref);
          bc.dbuf_put_u32(var_name);
          dbuf_put_u16(bc, idx);
        }
        break;
      case OP_scope_get_ref:
                /* XXX: should create a dummy object with a named slot that is
                   a reference to the closure variable */
        bc.dbuf_putc(OP_undefined);
        /* fall thru */
      case OP_scope_get_var_undef:
      case OP_scope_get_var:
      case OP_scope_put_var:
      case OP_scope_put_var_init:
        boolean is_put = (opCode == OP_scope_put_var ||
          opCode == OP_scope_put_var_init);
        if (is_put) {
          if (s.closure_var.get(idx).is_lexical) {
            if (opCode == OP_scope_put_var_init) {
              /* 'this' can only be initialized once */
              if (var_name == JS_ATOM_this.toJSAtom())
                bc.dbuf_putc(OP_put_var_ref_check_init);
              else
                bc.dbuf_putc(OP_put_var_ref);
            } else {
              bc.dbuf_putc(OP_put_var_ref_check);
            }
          } else {
            bc.dbuf_putc(OP_put_var_ref);
          }
        } else {
          if (s.closure_var.get(idx).is_lexical) {
            bc.dbuf_putc(OP_get_var_ref_check);
          } else {
            bc.dbuf_putc(OP_get_var_ref);
          }
        }
        dbuf_put_u16(bc, idx);
        break;
      case OP_scope_delete_var:
        bc.dbuf_putc(OP_push_false);
        break;
    }
    on_resolve_done(label_done, bc, s);
    return pos_next;
  }

  private static void on_resolve_done(int label_done, DynBuf bc, JSFunctionDef s) {
    if (label_done >= 0) {
      bc.dbuf_putc(OP_label);
      bc.dbuf_put_u32(label_done);
      s.label_slots.get(label_done).pos2 = bc.size;
    }
  }

  static int add_closure_var(JSContext ctx, JSFunctionDef s,
                             boolean is_local, boolean is_arg,
                             int var_idx, JSAtom var_name,
                             boolean is_const, boolean is_lexical,
                             JSVarKindEnum var_kind) {
    JSClosureVar cv = new JSClosureVar();

    /* the closure variable indexes are currently stored on 16 bits */
    if (s.closure_var.size() >= JS_MAX_LOCAL_VARS) {
      JS_ThrowInternalError(ctx, "too many closure variables");
      return -1;
    }

    s.closure_var.add(cv);
    cv.is_local = is_local;
    cv.is_arg = is_arg;
    cv.is_const = is_const;
    cv.is_lexical = is_lexical;
    cv.var_kind = var_kind;
    cv.var_idx = var_idx;
    cv.var_name = var_name;

    return s.closure_var.size() - 1;
  }

  static int find_closure_var(JSContext ctx, JSFunctionDef s,
                              JSAtom var_name) {
    int i;
    for (i = 0; i < s.closure_var.size(); i++) {
      JSClosureVar cv = s.closure_var.get(i);
      if (cv.var_name == var_name)
        return i;
    }
    return -1;
  }

  /* 'fd' must be a parent of 's'. Create in 's' a closure referencing a
   local variable (is_local = TRUE) or a closure (is_local = FALSE) in
   'fd' */
  static int get_closure_var2(JSContext ctx, JSFunctionDef s,
                              JSFunctionDef fd, boolean is_local,
                              boolean is_arg, int var_idx, JSAtom var_name,
                              boolean is_const, boolean is_lexical,
                              JSVarKindEnum var_kind) {
    int i;

    if (fd != s.parent) {
      var_idx = get_closure_var2(ctx, s.parent, fd, is_local,
        is_arg, var_idx, var_name,
        is_const, is_lexical, var_kind);
      if (var_idx < 0)
        return -1;
      is_local = false;
    }
    for (i = 0; i < s.closure_var.size(); i++) {
      JSClosureVar cv = s.closure_var.get(i);
      if (cv.var_idx == var_idx && cv.is_arg == is_arg &&
        cv.is_local == is_local)
        return i;
    }
    return add_closure_var(ctx, s, is_local, is_arg, var_idx, var_name,
      is_const, is_lexical, var_kind);
  }


  static int get_closure_var(JSContext ctx, JSFunctionDef s,
                             JSFunctionDef fd, boolean is_arg,
                             int var_idx, JSAtom var_name,
                             boolean is_const, boolean is_lexical,
                             JSVarKindEnum var_kind) {
    return get_closure_var2(ctx, s, fd, true, is_arg,
      var_idx, var_name, is_const, is_lexical,
      var_kind);
  }


  static int get_with_scope_opcode(int op) {
    if (op == OP_scope_get_var_undef.ordinal())
      return OP_with_get_var.ordinal();
    else
      return OP_with_get_var.ordinal() + (op - OP_scope_get_var.ordinal());
  }

  static boolean can_opt_put_ref_value(final byte[] bc_buf, int pos) {
    OPCodeEnum opcode = JUtils.get_opcode(bc_buf, pos);
    return (JUtils.get_opcode(bc_buf, pos + 1) == OP_put_ref_value &&
      (opcode == OP_insert3 ||
        opcode == OP_perm4 ||
        opcode == OP_nop ||
        opcode == OP_rot3l));
  }

  static boolean can_opt_put_global_ref_value(final byte[] bc_buf, int pos) {
    OPCodeEnum opcode = JUtils.get_opcode(bc_buf, pos);
    return (JUtils.get_opcode(bc_buf, pos + 1) == OP_put_ref_value &&
      (opcode == OP_insert3 ||
        opcode == OP_perm4 ||
        opcode == OP_nop ||
        opcode == OP_rot3l));
  }

  static int optimize_scope_make_ref(JSContext ctx, JSFunctionDef s,
                                     DynBuf bc, byte[] bc_buf,
                                     LabelSlot ls, int pos_next,
                                     int get_op, int var_idx) {
    int label_pos, end_pos, pos;

    /* XXX: should optimize `loc(a) += expr` as `expr add_loc(a)`
       but only if expr does not modify `a`.
       should scan the code between pos_next and label_pos
       for operations that can potentially change `a`:
       OP_scope_make_ref(a), function calls, jumps and gosub.
     */
    /* replace the reference get/put with normal variable
       accesses */
    if (get_opcode(bc_buf, pos_next) == OP_get_ref_value) {
      dbuf_putc(bc, get_op);
      dbuf_put_u16(bc, var_idx);
      pos_next++;
    }
    /* remove the OP_label to make room for replacement */
    /* label should have a refcount of 0 anyway */
    /* XXX: should avoid this patch by inserting nops in phase 1 */
    label_pos = ls.pos;
    pos = label_pos - 5;
    /* label points to an instruction pair:
       - insert3 / put_ref_value
       - perm4 / put_ref_value
       - rot3l / put_ref_value
       - nop / put_ref_value
     */
    end_pos = label_pos + 2;
    if (get_opcode(bc_buf, label_pos) == OP_insert3)
      put_u8(bc_buf, pos++, OP_dup);
    put_u8(bc_buf, pos, get_op + 1);
    put_u16(bc_buf, pos + 1, var_idx);
    pos += 3;
    /* pad with OP_nop */
    while (pos < end_pos)
      put_u8(bc_buf, pos++, OP_nop);
    return pos_next;
  }


  static int optimize_scope_make_global_ref(JSContext ctx, JSFunctionDef s,
                                            DynBuf bc, byte[] bc_buf,
                                            LabelSlot ls, int pos_next,
                                            JSAtom var_name) {
    int label_pos, end_pos, pos;
    OPCodeEnum op;
    boolean is_strict;
    is_strict = false;

    /* replace the reference get/put with normal variable
       accesses */
    if (is_strict) {
        /* need to check if the variable exists before evaluating the right
           expression */
      /* XXX: need an extra OP_true if destructuring an array */
      bc.dbuf_putc(OP_check_var);
      bc.dbuf_put_u32(var_name);
    } else {
      /* XXX: need 2 extra OP_true if destructuring an array */
    }
    if (get_opcode(bc_buf, pos_next) == OP_get_ref_value) {
      bc.dbuf_putc(OP_get_var);
      bc.dbuf_put_u32(var_name);
      pos_next++;
    }
    /* remove the OP_label to make room for replacement */
    /* label should have a refcount of 0 anyway */
    /* XXX: should have emitted several OP_nop to avoid this kludge */
    label_pos = ls.pos;
    pos = label_pos - 5;
    assert (get_opcode(bc_buf, pos) == OP_label);
    end_pos = label_pos + 2;
    op = get_opcode(bc_buf, label_pos);
    if (is_strict) {
      if (op != OP_nop) {
        switch (op) {
          case OP_insert3:
            op = OP_insert2;
            break;
          case OP_perm4:
            op = OP_perm3;
            break;
          case OP_rot3l:
            op = OP_swap;
            break;
          default:
            break;
        }
        put_u8(bc_buf, pos++, op);
      }
    } else {
      if (op == OP_insert3)
        put_u8(bc_buf, pos++, OP_dup);
    }
    if (is_strict) {
      put_u8(bc_buf, pos++, OP_put_var_strict);
      /* XXX: need 1 extra OP_drop if destructuring an array */
    } else {
      put_u8(bc_buf, pos++, OP_put_var);
      /* XXX: need 2 extra OP_drop if destructuring an array */
    }
    put_u32(bc_buf, pos + 1, var_name);
    pos += 5;
    /* pad with OP_nop */
    while (pos < end_pos)
      put_u8(bc_buf, pos++, OP_nop);
    return pos_next;
  }

  static int resolve_pseudo_var(JSContext ctx, JSFunctionDef s,
                                JSAtom var_name) {
    int var_idx;

    if (!s.has_this_binding)
      return -1;
    JSAtomEnum atomEnum = JSAtomEnum.values()[var_name.getVal()];
    switch (atomEnum) {
      case JS_ATOM_home_object:
        /* 'home_object' pseudo variable */
        var_idx = s.home_object_var_idx = add_var(ctx, s, var_name);
        break;
      case JS_ATOM_this_active_func:
        /* 'this.active_func' pseudo variable */
        var_idx = s.this_active_func_var_idx = add_var(ctx, s, var_name);
        break;
      case JS_ATOM_new_target:
        /* 'new.target' pseudo variable */
        var_idx = s.new_target_var_idx = add_var(ctx, s, var_name);
        break;
      case JS_ATOM_this:
        /* 'this' pseudo variable */
        var_idx = s.this_var_idx = add_var_this(ctx, s);
        break;
      default:
        var_idx = -1;
        break;
    }
    return var_idx;
  }
}
