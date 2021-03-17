package com.craftinginterpreters.lox;


import static com.craftinginterpreters.lox.DynBuf.*;
import static com.craftinginterpreters.lox.JSAtom.*;
import static com.craftinginterpreters.lox.JSAtomEnum.*;
import static com.craftinginterpreters.lox.JSContext.*;
import static com.craftinginterpreters.lox.JS_PROP.*;
import static com.craftinginterpreters.lox.LoxJS.*;
import static com.craftinginterpreters.lox.OPCodeEnum.*;

/**
 * @author benpeng.jiang
 * @title: JSHoistedDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/1/411:57 PM
 */
public class JSHoistedDef extends JSVarDef {

  static void instantiate_hoisted_definitions(JSContext ctx, JSFunctionDef s, DynBuf bc) {
    int i, idx = 0, var_idx;

    /* add the hoisted functions and variables */
    for (i = 0; i < s.hoisted_def.size(); i++) {
      JSHoistedDef hf = s.hoisted_def.get(i);
      int has_closure = 0;
      boolean force_init = hf.force_init;
      if (s.is_global_var && hf.var_name != JS_ATOM_NULL) {
            /* we are in an eval, so the closure contains all the
               enclosing variables */
            /* If the outer function has a variable environment,
               create a property for the variable there */
        for (idx = 0; idx < s.closure_var.size(); idx++) {
          JSClosureVar cv = s.closure_var.get(idx);
          if (cv.var_name == hf.var_name) {
            has_closure = 2;
            force_init = false;
            break;
          }
          if (cv.var_name == JS_ATOM__var_.toJSAtom()) {
            dbuf_putc(bc, OP_get_var_ref);
            dbuf_put_u16(bc, idx);
            has_closure = 1;
            force_init = true;
            break;
          }
        }
        if (has_closure == 0) {
          int flags;

          flags = 0;
          if (s.eval_type != JS_EVAL_TYPE_GLOBAL)
            flags |= JS_PROP_CONFIGURABLE;
          if (hf.cpool_idx >= 0 && !hf.is_lexical) {
            /* global function definitions need a specific handling */
            dbuf_putc(bc, OP_fclosure);
            dbuf_put_u32(bc, hf.cpool_idx);

            dbuf_putc(bc, OP_define_func);
            dbuf_put_u32(bc, hf.var_name);
            dbuf_putc(bc, flags);

            break;
          } else {
            if (hf.is_lexical) {
              flags |= DEFINE_GLOBAL_LEX_VAR;
              if (!hf.is_const)
                flags |= JS_PROP_WRITABLE;
            }
            dbuf_putc(bc, OP_define_var);
            dbuf_put_u32(bc, hf.var_name);
            dbuf_putc(bc, flags);
          }
        }
      }
      if (hf.cpool_idx >= 0 || force_init) {
        if (hf.cpool_idx >= 0) {
          dbuf_putc(bc, OP_fclosure);
          dbuf_put_u32(bc, hf.cpool_idx);
          if (hf.var_name == JS_ATOM__default_.toJSAtom()) {
            /* set default export function name */
            dbuf_putc(bc, OP_set_name);
            dbuf_put_u32(bc, JS_ATOM_default);
          }
        } else {
          dbuf_putc(bc, OP_undefined);
        }
        if (s.is_global_var) {
          if (has_closure == 2) {
            dbuf_putc(bc, OP_put_var_ref);
            dbuf_put_u16(bc, idx);
          } else if (has_closure == 1) {
            dbuf_putc(bc, OP_define_field);
            dbuf_put_u32(bc, hf.var_name);
            dbuf_putc(bc, OP_drop);
          } else {
            /* XXX: Check if variable is writable and enumerable */
            dbuf_putc(bc, OP_put_var);
            dbuf_put_u32(bc, hf.var_name);
          }
        } else {
          var_idx = hf.var_idx;
          if ((var_idx & ARGUMENT_VAR_OFFSET) != 0) {
            dbuf_putc(bc, OP_put_arg);
            dbuf_put_u16(bc, var_idx - ARGUMENT_VAR_OFFSET);
          } else {
            dbuf_putc(bc, OP_put_loc);
            dbuf_put_u16(bc, var_idx);
          }
        }
      }
    }

    s.hoisted_def.clear();
  }

}
