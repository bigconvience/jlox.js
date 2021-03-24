package com.lox.javascript;

import static com.lox.javascript.PutLValueEnum.*;
import static com.lox.javascript.TokenType.*;

/**
 * @author benpeng.jiang
 * @title: LValue
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/169:24 AM
 */
public class LValue {
  OPCodeEnum opcode;
  int scope;
  JSAtom name;
  int label;
  int depth;

 static LValue get_lvalue(Resolver s, DynBuf bc_buf, boolean keep, TokenType tok) {
    LValue lValue = new LValue();
   JSFunctionDef fd = s.cur_func;
   int scope, label, depth;
   JSAtom name;

   /* we check the last opcode to get the lvalue type */
   scope = 0;
   name = JSAtom.JS_ATOM_NULL;
   label = -1;
   depth = 0;
   
   
   OPCodeEnum opcode = fd.get_prev_code();
   switch(opcode) {
     case OP_scope_get_var:
       name = new JSAtom(JUtils.get_u32(fd.byte_code.buf, fd.last_opcode_pos + 1));
       scope = JUtils.get_u16(fd.byte_code.buf, fd.last_opcode_pos + 5);

       depth = 2;  /* will generate OP_get_ref_value */
       break;
     case OP_get_field:
       name = new JSAtom(JUtils.get_u32(bc_buf.buf, fd.last_opcode_pos + 1));
       depth = 1;
       break;
     case OP_scope_get_private_field:
       name = new JSAtom(JUtils.get_u32(bc_buf.buf, fd.last_opcode_pos + 1));
       scope = JUtils.get_u16(bc_buf.buf, fd.last_opcode_pos + 5);
       depth = 1;
       break;
     case OP_get_array_el:
       depth = 2;
       break;
     case OP_get_super_value:
       depth = 3;
       break;
     default:
      
       if (tok == TOK_FOR) {
          JSThrower.js_parse_error(s, "invalid for in/of left hand-side");
       } else if (tok == TOK_INC || tok == TOK_DEC) {
          JSThrower.js_parse_error(s, "invalid increment/decrement operand");
       } else if (tok == TOK_LEFT_BRACKET || tok == TOK_RIGHT_BRACKET) {
          JSThrower.js_parse_error(s, "invalid destructuring target");
       } else {
         JSThrower.js_parse_error(s, "invalid assignment left-hand side");
       }
   }
   /* remove the last opcode */
   fd.byte_code.size = fd.last_opcode_pos;
   fd.last_opcode_pos = -1;

   if (keep) {
     /* get the value but keep the object/fields on the stack */
     switch(opcode) {
       case OP_scope_get_var:
         label = fd.new_label_fd();
         s.emit_op(OPCodeEnum.OP_scope_make_ref);
         s.emit_u32(name);
         s.emit_u32(label);
         s.emit_u16(scope);
         fd.update_label(label, 1);
         s.emit_op(OPCodeEnum.OP_get_ref_value);
         opcode = OPCodeEnum.OP_get_ref_value;
         break;
       case OP_get_field:
         s.emit_op( OPCodeEnum.OP_get_field2);
         s.emit_u32(name);
         break;
       case OP_scope_get_private_field:
         s.emit_op( OPCodeEnum.OP_scope_get_private_field2);
         s.emit_u32(name);
         s.emit_u16( scope);
         break;
       case OP_get_array_el:
         /* XXX: replace by a single opcode ? */
         s.emit_op( OPCodeEnum.OP_to_propkey2);
         s.emit_op( OPCodeEnum.OP_dup2);
         s.emit_op( OPCodeEnum.OP_get_array_el);
         break;
       case OP_get_super_value:
         s.emit_op( OPCodeEnum.OP_to_propkey);
         s.emit_op( OPCodeEnum.OP_dup3);
         s.emit_op( OPCodeEnum.OP_get_super_value);
         break;
       default:
//         abort();
     }
   } else {
     switch(opcode) {
       case OP_scope_get_var:
         label = fd.new_label_fd();
         s.emit_op( OPCodeEnum.OP_scope_make_ref);
         s.emit_u32(name);
         s.emit_u32(label);
         s.emit_u16( scope);
         fd.update_label(label, 1);
         opcode = OPCodeEnum.OP_get_ref_value;
         break;
       case OP_get_array_el:
         s.emit_op( OPCodeEnum.OP_to_propkey2);
         break;
       case OP_get_super_value:
         s.emit_op( OPCodeEnum.OP_to_propkey);
         break;
     }
   }


    lValue.opcode = opcode;
    lValue.scope = scope;
    lValue.name = name;
    lValue.label = label;
    lValue.depth = depth;

    return lValue;
  }

  static void put_lvalue(Resolver s,  LValue lValue, PutLValueEnum special, boolean is_let) {
   JSFunctionDef fd = s.cur_func;
    OPCodeEnum opcode = lValue.opcode;
    JSAtom name = lValue.name;
    int scope = lValue.scope;
    int label = lValue.label;
    switch(opcode) {
      case OP_get_field:
      case OP_scope_get_private_field:
        /* depth = 1 */
        switch(special) {
          case PUT_LVALUE_NOKEEP:
          case PUT_LVALUE_NOKEEP_DEPTH:
            break;
          case PUT_LVALUE_KEEP_TOP:
            s.emit_op(OPCodeEnum.OP_insert2); /* obj v -> v obj v */
            break;
          case PUT_LVALUE_KEEP_SECOND:
            s.emit_op(OPCodeEnum.OP_perm3); /* obj v0 v -> v0 obj v */
            break;
          case PUT_LVALUE_NOKEEP_BOTTOM:
            s.emit_op(OPCodeEnum.OP_swap);
            break;
          default:
            break;
        }
        break;
      case OP_get_array_el:
      case OP_get_ref_value:
        /* depth = 2 */
        if (opcode == OPCodeEnum.OP_get_ref_value) {
          s.emit_label(label);
        }
        switch(special) {
          case PUT_LVALUE_NOKEEP:
            s.emit_op(OPCodeEnum.OP_nop); /* will trigger optimization */
            break;
          case PUT_LVALUE_NOKEEP_DEPTH:
            break;
          case PUT_LVALUE_KEEP_TOP:
            s.emit_op(OPCodeEnum.OP_insert3); /* obj prop v -> v obj prop v */
            break;
          case PUT_LVALUE_KEEP_SECOND:
            s.emit_op(OPCodeEnum.OP_perm4); /* obj prop v0 v -> v0 obj prop v */
            break;
          case PUT_LVALUE_NOKEEP_BOTTOM:
            s.emit_op(OPCodeEnum.OP_rot3l);
            break;
          default:
        }
        break;
      case OP_get_super_value:
        /* depth = 3 */
        switch(special) {
          case PUT_LVALUE_NOKEEP:
          case PUT_LVALUE_NOKEEP_DEPTH:
            break;
          case PUT_LVALUE_KEEP_TOP:
            s.emit_op(OPCodeEnum.OP_insert4); /* this obj prop v -> v this obj prop v */
            break;
          case PUT_LVALUE_KEEP_SECOND:
            s.emit_op(OPCodeEnum.OP_perm5); /* this obj prop v0 v -> v0 this obj prop v */
            break;
          case PUT_LVALUE_NOKEEP_BOTTOM:
            s.emit_op(OPCodeEnum.OP_rot4l);
            break;
          default:

        }
        break;
      default:
        break;
    }

    switch(opcode) {
      case OP_scope_get_var:  /* val -- */
        assert(special == PUT_LVALUE_NOKEEP ||
          special == PUT_LVALUE_NOKEEP_DEPTH);
        s.emit_op(is_let ? OPCodeEnum.OP_scope_put_var_init : OPCodeEnum.OP_scope_put_var);
        s.emit_u32(name);  /* has refcount */
        s.emit_u16(scope);
        break;
      case OP_get_field:
        s.emit_op(OPCodeEnum.OP_put_field);
        s.emit_u32(name);  /* name has refcount */
        break;
      case OP_scope_get_private_field:
        s.emit_op(OPCodeEnum.OP_scope_put_private_field);
        s.emit_u32(name);  /* name has refcount */
        s.emit_u16(scope);
        break;
      case OP_get_array_el:
        s.emit_op(OPCodeEnum.OP_put_array_el);
        break;
      case OP_get_ref_value:
        s.emit_op(OPCodeEnum.OP_put_ref_value);
        break;
      case OP_get_super_value:
        s.emit_op(OPCodeEnum.OP_put_super_value);
        break;
      default:
    }
  }

}
