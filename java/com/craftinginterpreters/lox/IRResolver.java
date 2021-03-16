package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.JSAtom.JS_ATOM_NULL;
import static com.craftinginterpreters.lox.JSAtomEnum.JS_ATOM__var_;
import static com.craftinginterpreters.lox.JSAtomEnum.JS_ATOM_null;
import static com.craftinginterpreters.lox.JSContext.DEFINE_GLOBAL_FUNC_VAR;
import static com.craftinginterpreters.lox.JSContext.DEFINE_GLOBAL_LEX_VAR;
import static com.craftinginterpreters.lox.JSThrower.JS_THROW_VAR_REDECL;
import static com.craftinginterpreters.lox.JSVarKindEnum.*;
import static com.craftinginterpreters.lox.LoxJS.JS_EVAL_TYPE_DIRECT;
import static com.craftinginterpreters.lox.OPCodeEnum.*;
import static com.craftinginterpreters.lox.JUtils.*;
import static com.craftinginterpreters.lox.OPCodeInfo.*;

/**
 * @author benpeng.jiang
 * @title: IRResolver
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/1611:31 PM
 */
public class IRResolver {
JSFunctionDef fd;
JSContext ctx;

  public IRResolver(JSContext ctx, JSFunctionDef fd) {
    this.fd = fd;
    this.ctx = ctx;
  }  

  /* convert global variable accesses to local variables or closure
   variables when necessary */
  int resolve_variables()
  {
    JSFunctionDef s = fd;
    int pos, pos_next, bc_len, op, len, i, idx, arg_valid, line_num;
    byte[] bc_buf;
    JSAtom var_name;
    DynBuf bc_out;
    CodeContext cc = new CodeContext();
    int scope;

    cc.bc_buf = bc_buf = s.byte_code.buf;
    cc.bc_len = bc_len = s.byte_code.size;
   bc_out = new DynBuf();

    /* first pass for runtime checks (must be done before the
       variables are created) */
    if (s.is_global_var) {
      for(i = 0; i < s.hoisted_def.size() ; i++) {
        JSHoistedDef hf = s.hoisted_def.get(i);
        int flags;

        if (hf.var_name != JS_ATOM_NULL) {
          /* check if global variable (XXX: simplify) */
          for(idx = 0; idx < s.closure_var.size(); idx++) {
            JSClosureVar cv = s.closure_var.get(i);
            if (cv.var_name == hf.var_name) {
              if (s.eval_type == JS_EVAL_TYPE_DIRECT &&
                cv.is_lexical) {
                            /* Check if a lexical variable is
                               redefined as 'var'. XXX: Could abort
                               compilation here, but for consistency
                               with the other checks, we delay the
                               error generation. */
                bc_out.dbuf_putc(OP_throw_var);
                bc_out.dbuf_put_u32(hf.var_name);
                bc_out.dbuf_putc(JS_THROW_VAR_REDECL);
              }
                      break;
            }
            if (cv.var_name.getVal() == JS_ATOM__var_.ordinal()) {
                       break; 
            }
          }

          bc_out.dbuf_putc(OP_check_define_var);
          bc_out.dbuf_put_u32(hf.var_name);
          flags = 0;
          if (hf.is_lexical)
            flags |= DEFINE_GLOBAL_LEX_VAR;
          if (hf.cpool_idx >= 0)
            flags |= DEFINE_GLOBAL_FUNC_VAR;
          bc_out.dbuf_putc(flags);
        }

      }
    }

    arg_valid = 0;
    line_num = 0; /* avoid warning */
    for (pos = 0; pos < bc_len; pos = pos_next) {
      op = Byte.toUnsignedInt(bc_buf[pos]);
      len = opcode_info.get(op).size;
      pos_next = pos + len;
      switch(OPCodeEnum.values()[op]) {
        case OP_line_num:
          line_num = get_u32(bc_buf, pos + 1);
          s.line_number_size++;
           on_on_change(pos, len,  bc_buf, bc_out);

        case OP_set_arg_valid_upto:
          arg_valid = get_u16(bc_buf, pos + 1);
          break;
        case OP_eval: /* convert scope index to adjusted variable index */
        {
          int call_argc = get_u16(bc_buf, pos + 1);
          scope = get_u16(bc_buf, pos + 1 + 2);
          mark_eval_captured_variables(ctx, s, scope);
          bc_out.dbuf_putc(op);
          bc_out.dbuf_put_u16(call_argc);
          bc_out.dbuf_put_u16(s.scopes.get(scope).first + 1);
        }
        break;
        case OP_apply_eval: /* convert scope index to adjusted variable index */
          scope = get_u16(bc_buf, pos + 1);
          mark_eval_captured_variables(ctx, s, scope);
          bc_out.dbuf_putc(op);
          bc_out.dbuf_put_u16(s.scopes.get(scope).first + 1);
          break;
        case OP_scope_get_var_undef:
        case OP_scope_get_var:
        case OP_scope_put_var:
        case OP_scope_delete_var:
        case OP_scope_get_ref:
        case OP_scope_put_var_init:
          var_name = new JSAtom(get_u32(bc_buf, pos + 1));
          scope = get_u16(bc_buf, pos + 5);
          pos_next = resolve_scope_var(ctx, s, var_name, scope, op, bc_out,
            null, null, pos_next, arg_valid);

          break;
        case OP_scope_make_ref:
        {
          int label;
          LabelSlot ls;
          var_name = get_atom(bc_buf, pos + 1);
          label = get_u32(bc_buf, pos + 5);
          scope = get_u16(bc_buf, pos + 9);
          ls = s.label_slots.get(label);
          ls.ref_count--;  /* always remove label reference */
          pos_next = resolve_scope_var(ctx, s, var_name, scope, op, bc_out,
            bc_buf, ls, pos_next, arg_valid);
          
        }
        break;
        case OP_scope_get_private_field:
        case OP_scope_get_private_field2:
        case OP_scope_put_private_field:
        {
          int ret;
          var_name = get_atom(bc_buf, pos + 1);
          scope = get_u16(bc_buf, pos + 5);
          //todo
//          ret = resolve_scope_private_field(ctx, s, var_name, scope, op, bc_out);
//          if (ret < 0) {
//                  on_resolve_fail(pos, bc_len, bc_buf, bc_out);
//                  break;
//          }
        }
        break;
        case OP_gosub:
          s.jump_size++;

           on_on_change(pos, len,  bc_buf, bc_out);
break;
        case OP_drop:
  
break;
        case OP_insert3:

           on_on_change(pos, len,  bc_buf, bc_out);
break;

        case OP_goto:
          s.jump_size++;
          /* fall thru */
        case OP_tail_call:
        case OP_tail_call_method:
        case OP_return:
        case OP_return_undef:
        case OP_throw:
        case OP_throw_var:
        case OP_ret:

           on_on_change(pos, len,  bc_buf, bc_out);
break;

        case OP_label:
        {
          int label;
          LabelSlot ls;

          label = get_u32(bc_buf, pos + 1);
          assert(label >= 0 && label < s.label_slots.size());
          ls = s.label_slots.get(label);
          ls.pos2 = bc_out.size + opcode_info.get(op).size;
        }
           on_on_change(pos, len,  bc_buf, bc_out);
break;

        case OP_enter_scope:
        {
          int scope_idx;
          scope = get_u16(bc_buf, pos + 1);

          if (scope == 1) {
            s.instantiate_hoisted_definitions(bc_out);
          }

          for(scope_idx = s.scopes.get(scope).first; scope_idx >= 0;) {
            JSVarDef vd = s.vars.get(scope_idx);
            if (vd.scope_level == scope) {
              if (vd.var_kind == JS_VAR_FUNCTION_DECL ||
                vd.var_kind == JS_VAR_NEW_FUNCTION_DECL) {
                /* Initialize lexical variable upon entering scope */
                bc_out.dbuf_putc(OP_fclosure);
                bc_out.dbuf_put_u32(vd.func_pool_or_scope_idx);
                bc_out.dbuf_putc(OP_put_loc);
                bc_out.dbuf_put_u16(scope_idx);
              } else {
                            /* XXX: should check if variable can be used
                               before initialization */
                bc_out.dbuf_putc(OP_set_loc_uninitialized);
                bc_out.dbuf_put_u16(scope_idx);
              }
              scope_idx = vd.scope_next;
            } else {
              break;
            }
          }
        }
        break;

        case OP_leave_scope:
        {
          int scope_idx;
          scope = get_u16(bc_buf, pos + 1);

          for(scope_idx = s.scopes.get(scope).first; scope_idx >= 0;) {
            JSVarDef vd = s.vars.get(scope_idx);
            if (vd.scope_level == scope) {
              if (vd.is_captured) {
                bc_out.dbuf_putc(OP_close_loc);
                bc_out.dbuf_put_u16(scope_idx);
              }
              scope_idx = vd.scope_next;
            } else {
              break;
            }
          }
        }
        break;

        case OP_set_name:
        {
          /* remove dummy set_name opcodes */
          JSAtom name = get_atom(bc_buf, pos + 1);
          if (name == JS_ATOM_null.toJSAtom())
            break;
        }
           on_on_change(pos, len,  bc_buf, bc_out);
break;

        case OP_if_false:
        case OP_if_true:
        case OP_catch:
          s.jump_size++;
           on_on_change(pos, len,  bc_buf, bc_out);
break;

        case OP_dup:

           on_on_change(pos, len,  bc_buf, bc_out);
break;

        case OP_nop:
          /* remove erased code */
          break;
        case OP_set_class_name:
          /* only used during parsing */
          break;

        default:
          no_change:
          bc_out.dbuf_put(bc_buf, pos, len);
          break;
      }
    }

    /* set the new byte code */
    s.byte_code = bc_out;
    return 0;
  }

  private void on_on_change(int pos, int len, byte[] bc_buf, DynBuf bc_out) {
    bc_out.dbuf_put(bc_buf,  pos, len);
  }
  
  private void on_resolve_fail(int pos,  int bc_len, byte[] bc_buf, DynBuf bc_out) {
  JSFunctionDef s = fd;
    /* continue the copy to keep the atom refcounts consistent */
    /* XXX: find a better solution ? */
    int pos_next, op, len;
    for (; pos < bc_len; pos = pos_next) {
      op = Byte.toUnsignedInt(bc_buf[pos]);
      len = opcode_info.get(op).size;
      pos_next = pos + len;
      bc_out.dbuf_put(bc_buf, pos, len);
    }

    s.byte_code = bc_out;
  }


  static int resolve_scope_var(JSContext ctx, JSFunctionDef s,
                               JSAtom var_name, int scope_level, int op,
                               DynBuf bc, byte[] bc_buf,
                               LabelSlot ls, int pos_next, int arg_valid) {
    int var_idx = -1;
    JSFunctionDef fd = s;
    JSVarDef vd;
    for (int idx = fd.scopes.get(scope_level).first; idx >= 0; ) {
      vd = fd.vars.get(idx);
      if (vd.var_name.equals(var_name)) {

        var_idx = idx;
        break;
      } else {

      }
      idx = vd.scope_next;
    }

    OPCodeEnum opCodeEnum = OPCodeEnum.values()[op];

    /* global variable access */
    switch (opCodeEnum) {
      case OP_scope_make_ref:
        optimize_scope_make_global_ref(ctx, s, bc, bc_buf, ls, pos_next, var_name);
        break;
      case OP_scope_get_var_undef:
      case OP_scope_get_var:
      case OP_scope_put_var:
        bc.dbuf_putc(OPCodeEnum.values()[OP_get_var_undef.ordinal() + (op - OP_scope_get_var_undef.ordinal())]);
        bc.put_atom(var_name);
        break;
      case OP_scope_put_var_init:
        bc.dbuf_putc(OP_put_var_init);
        bc.put_atom(var_name);
        break;
      default:
        break;
    }

    return var_idx;
  }



  static int optimize_scope_make_global_ref(JSContext ctx, JSFunctionDef s,
                                            DynBuf bc, byte[] bc_buf,
                                            LabelSlot ls, int pos_next,
                                            JSAtom var_name)
  {
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
    assert(get_opcode(bc_buf, pos) == OP_label);
    end_pos = label_pos + 2;
    op = get_opcode(bc_buf, label_pos);
    if (is_strict) {
      if (op != OP_nop) {
        switch(op) {
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


  void mark_eval_captured_variables(JSContext ctx, JSFunctionDef s,
                                           int scope_level)
  {
    int idx;
    JSVarDef vd;

    for (idx = s.scopes.get(scope_level).first; idx >= 0;) {
      vd = s.vars.get(idx);
      vd.is_captured = true;
      idx = vd.scope_next;
    }
  }
}
