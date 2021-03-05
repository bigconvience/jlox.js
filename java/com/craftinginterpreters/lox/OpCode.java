package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

import  static com.craftinginterpreters.lox.OPCodeEnum.*;
import  static com.craftinginterpreters.lox.OPCodeFormat.*;
public class OpCode {
  String id;
  int size;
  int n_pop;
  int n_push;
  OPCodeFormat f;

  public OpCode(String id, int size, int n_pop, int n_push, OPCodeFormat f) {
    this.id = id;
    this.size = size;
    this.n_pop = n_pop;
    this.n_push = n_push;
    this.f = f;
  }


  public static Map<Integer, OpCode> opcode_info;
  public static Map<Integer, OPCodeEnum> opcode_enum;

  static {
    opcode_enum = new HashMap<>();
    for (OPCodeEnum codeEnum: OPCodeEnum.values()) {
      opcode_enum.put(codeEnum.ordinal(), codeEnum);
    }

    opcode_info = new HashMap<>();
    opcode_info.put(
      OP_print.ordinal(),
      new OpCode("print",1,0,1,none));
    opcode_info.put(
      OP_push_i32.ordinal(),
      new OpCode("push_i32",5,0,1,i32));
    opcode_info.put(
      OP_push_const.ordinal(),
      new OpCode("push_const",5,0,1,Const));
    opcode_info.put(
      OP_fclosure.ordinal(),
      new OpCode("fclosure",5,0,1,Const));
    opcode_info.put(
      OP_push_atom_value.ordinal(),
      new OpCode("push_atom_value",5,0,1,atom));
    opcode_info.put(
      OP_private_symbol.ordinal(),
      new OpCode("private_symbol",5,0,1,atom));
    opcode_info.put(
      OP_undefined.ordinal(),
      new OpCode("undefined",1,0,1,none));
    opcode_info.put(
      OP_null.ordinal(),
      new OpCode("null",1,0,1,none));
    opcode_info.put(
      OP_push_this.ordinal(),
      new OpCode("push_this",1,0,1,none));
    opcode_info.put(
      OP_push_false.ordinal(),
      new OpCode("push_false",1,0,1,none));
    opcode_info.put(
      OP_push_true.ordinal(),
      new OpCode("push_true",1,0,1,none));
    opcode_info.put(
      OP_object.ordinal(),
      new OpCode("object",1,0,1,none));
    opcode_info.put(
      OP_special_object.ordinal(),
      new OpCode("special_object",2,0,1,u8));
    opcode_info.put(
      OP_rest.ordinal(),
      new OpCode("rest",3,0,1,u16));
    opcode_info.put(
      OP_drop.ordinal(),
      new OpCode("drop",1,1,0,none));
    opcode_info.put(
      OP_nip.ordinal(),
      new OpCode("nip",1,2,1,none));
    opcode_info.put(
      OP_nip1.ordinal(),
      new OpCode("nip1",1,3,2,none));
    opcode_info.put(
      OP_dup.ordinal(),
      new OpCode("dup",1,1,2,none));
    opcode_info.put(
      OP_dup1.ordinal(),
      new OpCode("dup1",1,2,3,none));
    opcode_info.put(
      OP_dup2.ordinal(),
      new OpCode("dup2",1,2,4,none));
    opcode_info.put(
      OP_dup3.ordinal(),
      new OpCode("dup3",1,3,6,none));
    opcode_info.put(
      OP_insert2.ordinal(),
      new OpCode("insert2",1,2,3,none));
    opcode_info.put(
      OP_insert3.ordinal(),
      new OpCode("insert3",1,3,4,none));
    opcode_info.put(
      OP_insert4.ordinal(),
      new OpCode("insert4",1,4,5,none));
    opcode_info.put(
      OP_perm3.ordinal(),
      new OpCode("perm3",1,3,3,none));
    opcode_info.put(
      OP_perm4.ordinal(),
      new OpCode("perm4",1,4,4,none));
    opcode_info.put(
      OP_perm5.ordinal(),
      new OpCode("perm5",1,5,5,none));
    opcode_info.put(
      OP_swap.ordinal(),
      new OpCode("swap",1,2,2,none));
    opcode_info.put(
      OP_swap2.ordinal(),
      new OpCode("swap2",1,4,4,none));
    opcode_info.put(
      OP_rot3l.ordinal(),
      new OpCode("rot3l",1,3,3,none));
    opcode_info.put(
      OP_rot3r.ordinal(),
      new OpCode("rot3r",1,3,3,none));
    opcode_info.put(
      OP_rot4l.ordinal(),
      new OpCode("rot4l",1,4,4,none));
    opcode_info.put(
      OP_rot5l.ordinal(),
      new OpCode("rot5l",1,5,5,none));
    opcode_info.put(
      OP_call.ordinal(),
      new OpCode("call",3,1,1,npop));
    opcode_info.put(
      OP_tail_call.ordinal(),
      new OpCode("tail_call",3,1,0,npop));
    opcode_info.put(
      OP_call_method.ordinal(),
      new OpCode("call_method",3,2,1,npop));
    opcode_info.put(
      OP_array_from.ordinal(),
      new OpCode("array_from",3,0,1,npop));
    opcode_info.put(
      OP_apply.ordinal(),
      new OpCode("apply",3,3,1,u16));
    opcode_info.put(
      OP_return.ordinal(),
      new OpCode("return",1,1,0,none));
    opcode_info.put(
      OP_return_undef.ordinal(),
      new OpCode("return_undef",1,0,0,none));
    opcode_info.put(
      OP_check_ctor.ordinal(),
      new OpCode("check_ctor",1,0,0,none));
    opcode_info.put(
      OP_check_brand.ordinal(),
      new OpCode("check_brand",1,2,2,none));
    opcode_info.put(
      OP_add_brand.ordinal(),
      new OpCode("add_brand",1,2,0,none));
    opcode_info.put(
      OP_return_async.ordinal(),
      new OpCode("return_async",1,1,0,none));
    opcode_info.put(
      OP_throw.ordinal(),
      new OpCode("throw",1,1,0,none));
    opcode_info.put(
      OP_throw_var.ordinal(),
      new OpCode("throw_var",6,0,0,atom_u8));
    opcode_info.put(
      OP_eval.ordinal(),
      new OpCode("eval",5,1,1,npop_u16));
    opcode_info.put(
      OP_apply_eval.ordinal(),
      new OpCode("apply_eval",3,2,1,u16));
    opcode_info.put(
      OP_regexp.ordinal(),
      new OpCode("regexp",1,2,1,none));
    opcode_info.put(
      OP_get_super.ordinal(),
      new OpCode("get_super",1,1,1,none));
    opcode_info.put(
      OP_import.ordinal(),
      new OpCode("import",1,1,1,none));
    opcode_info.put(
      OP_check_var.ordinal(),
      new OpCode("check_var",5,0,1,atom));
    opcode_info.put(
      OP_get_var_undef.ordinal(),
      new OpCode("get_var_undef",5,0,1,atom));
    opcode_info.put(
      OP_get_var.ordinal(),
      new OpCode("get_var",5,0,1,atom));
    opcode_info.put(
      OP_put_var.ordinal(),
      new OpCode("put_var",5,1,0,atom));
    opcode_info.put(
      OP_put_var_init.ordinal(),
      new OpCode("put_var_init",5,1,0,atom));
    opcode_info.put(
      OP_put_var_strict.ordinal(),
      new OpCode("put_var_strict",5,2,0,atom));
    opcode_info.put(
      OP_get_ref_value.ordinal(),
      new OpCode("get_ref_value",1,2,3,none));
    opcode_info.put(
      OP_put_ref_value.ordinal(),
      new OpCode("put_ref_value",1,3,0,none));
    opcode_info.put(
      OP_define_var.ordinal(),
      new OpCode("define_var",6,0,0,atom_u8));
    opcode_info.put(
      OP_check_define_var.ordinal(),
      new OpCode("check_define_var",6,0,0,atom_u8));
    opcode_info.put(
      OP_define_func.ordinal(),
      new OpCode("define_func",6,1,0,atom_u8));
    opcode_info.put(
      OP_get_field.ordinal(),
      new OpCode("get_field",5,1,1,atom));
    opcode_info.put(
      OP_get_field2.ordinal(),
      new OpCode("get_field2",5,1,2,atom));
    opcode_info.put(
      OP_put_field.ordinal(),
      new OpCode("put_field",5,2,0,atom));
    opcode_info.put(
      OP_get_private_field.ordinal(),
      new OpCode("get_private_field",1,2,1,none));
    opcode_info.put(
      OP_put_private_field.ordinal(),
      new OpCode("put_private_field",1,3,0,none));
    opcode_info.put(
      OP_get_array_el.ordinal(),
      new OpCode("get_array_el",1,2,1,none));
    opcode_info.put(
      OP_get_array_el2.ordinal(),
      new OpCode("get_array_el2",1,2,2,none));
    opcode_info.put(
      OP_put_array_el.ordinal(),
      new OpCode("put_array_el",1,3,0,none));
    opcode_info.put(
      OP_define_field.ordinal(),
      new OpCode("define_field",5,2,1,atom));
    opcode_info.put(
      OP_set_name.ordinal(),
      new OpCode("set_name",5,1,1,atom));
    opcode_info.put(
      OP_set_proto.ordinal(),
      new OpCode("set_proto",1,2,1,none));
    opcode_info.put(
      OP_append.ordinal(),
      new OpCode("append",1,3,2,none));
    opcode_info.put(
      OP_define_method.ordinal(),
      new OpCode("define_method",6,2,1,atom_u8));
    opcode_info.put(
      OP_define_class.ordinal(),
      new OpCode("define_class",6,2,2,atom_u8));
    opcode_info.put(
      OP_define_class_computed.ordinal(),
      new OpCode("define_class_computed",6,3,3,atom_u8));
    opcode_info.put(
      OP_get_loc.ordinal(),
      new OpCode("get_loc",3,0,1,loc));
    opcode_info.put(
      OP_put_loc.ordinal(),
      new OpCode("put_loc",3,1,0,loc));
    opcode_info.put(
      OP_set_loc.ordinal(),
      new OpCode("set_loc",3,1,1,loc));
    opcode_info.put(
      OP_get_arg.ordinal(),
      new OpCode("get_arg",3,0,1,arg));
    opcode_info.put(
      OP_put_arg.ordinal(),
      new OpCode("put_arg",3,1,0,arg));
    opcode_info.put(
      OP_set_arg.ordinal(),
      new OpCode("set_arg",3,1,1,arg));
    opcode_info.put(
      OP_get_var_ref.ordinal(),
      new OpCode("get_var_ref",3,0,1,var_ref));
    opcode_info.put(
      OP_put_var_ref.ordinal(),
      new OpCode("put_var_ref",3,1,0,var_ref));
    opcode_info.put(
      OP_set_var_ref.ordinal(),
      new OpCode("set_var_ref",3,1,1,var_ref));
    opcode_info.put(
      OP_get_loc_check.ordinal(),
      new OpCode("get_loc_check",3,0,1,loc));
    opcode_info.put(
      OP_put_loc_check.ordinal(),
      new OpCode("put_loc_check",3,1,0,loc));
    opcode_info.put(
      OP_put_loc_check_init.ordinal(),
      new OpCode("put_loc_check_init",3,1,0,loc));
    opcode_info.put(
      OP_close_loc.ordinal(),
      new OpCode("close_loc",3,0,0,loc));
    opcode_info.put(
      OP_if_false.ordinal(),
      new OpCode("if_false",5,1,0,label));
    opcode_info.put(
      OP_if_true.ordinal(),
      new OpCode("if_true",5,1,0,label));
    opcode_info.put(
      OP_goto.ordinal(),
      new OpCode("goto",5,0,0,label));
    opcode_info.put(
      OP_catch.ordinal(),
      new OpCode("catch",5,0,1,label));
    opcode_info.put(
      OP_gosub.ordinal(),
      new OpCode("gosub",5,0,0,label));
    opcode_info.put(
      OP_ret.ordinal(),
      new OpCode("ret",1,1,0,none));
    opcode_info.put(
      OP_to_object.ordinal(),
      new OpCode("to_object",1,1,1,none));
    opcode_info.put(
      OP_to_propkey.ordinal(),
      new OpCode("to_propkey",1,1,1,none));
    opcode_info.put(
      OP_to_propkey2.ordinal(),
      new OpCode("to_propkey2",1,2,2,none));
    opcode_info.put(
      OP_with_get_var.ordinal(),
      new OpCode("with_get_var",10,1,0,atom_label_u8));
    opcode_info.put(
      OP_with_put_var.ordinal(),
      new OpCode("with_put_var",10,2,1,atom_label_u8));
    opcode_info.put(
      OP_with_make_ref.ordinal(),
      new OpCode("with_make_ref",10,1,0,atom_label_u8));
    opcode_info.put(
      OP_with_get_ref.ordinal(),
      new OpCode("with_get_ref",10,1,0,atom_label_u8));
    opcode_info.put(
      OP_make_loc_ref.ordinal(),
      new OpCode("make_loc_ref",7,0,2,atom_u16));
    opcode_info.put(
      OP_make_arg_ref.ordinal(),
      new OpCode("make_arg_ref",7,0,2,atom_u16));
    opcode_info.put(
      OP_make_var_ref.ordinal(),
      new OpCode("make_var_ref",5,0,2,atom));
    opcode_info.put(
      OP_for_in_start.ordinal(),
      new OpCode("for_in_start",1,1,1,none));
    opcode_info.put(
      OP_for_of_start.ordinal(),
      new OpCode("for_of_start",1,1,3,none));
    opcode_info.put(
      OP_for_in_next.ordinal(),
      new OpCode("for_in_next",1,1,3,none));
    opcode_info.put(
      OP_for_of_next.ordinal(),
      new OpCode("for_of_next",2,3,5,u8));
    opcode_info.put(
      OP_iterator_close.ordinal(),
      new OpCode("iterator_close",1,3,0,none));
    opcode_info.put(
      OP_initial_yield.ordinal(),
      new OpCode("initial_yield",1,0,0,none));
    opcode_info.put(
      OP_yield.ordinal(),
      new OpCode("yield",1,1,2,none));
    opcode_info.put(
      OP_yield_star.ordinal(),
      new OpCode("yield_star",1,2,2,none));
    opcode_info.put(
      OP_await.ordinal(),
      new OpCode("await",1,1,1,none));
    opcode_info.put(
      OP_neg.ordinal(),
      new OpCode("neg",1,1,1,none));
    opcode_info.put(
      OP_plus.ordinal(),
      new OpCode("plus",1,1,1,none));
    opcode_info.put(
      OP_dec.ordinal(),
      new OpCode("dec",1,1,1,none));
    opcode_info.put(
      OP_inc.ordinal(),
      new OpCode("inc",1,1,1,none));
    opcode_info.put(
      OP_post_dec.ordinal(),
      new OpCode("post_dec",1,1,2,none));
    opcode_info.put(
      OP_post_inc.ordinal(),
      new OpCode("post_inc",1,1,2,none));
    opcode_info.put(
      OP_dec_loc.ordinal(),
      new OpCode("dec_loc",2,0,0,loc8));
    opcode_info.put(
      OP_inc_loc.ordinal(),
      new OpCode("inc_loc",2,0,0,loc8));
    opcode_info.put(
      OP_add_loc.ordinal(),
      new OpCode("add_loc",2,1,0,loc8));
    opcode_info.put(
      OP_not.ordinal(),
      new OpCode("not",1,1,1,none));
    opcode_info.put(
      OP_lnot.ordinal(),
      new OpCode("lnot",1,1,1,none));
    opcode_info.put(
      OP_typeof.ordinal(),
      new OpCode("typeof",1,1,1,none));
    opcode_info.put(
      OP_delete.ordinal(),
      new OpCode("delete",1,2,1,none));
    opcode_info.put(
      OP_delete_var.ordinal(),
      new OpCode("delete_var",5,0,1,atom));
    opcode_info.put(
      OP_mul.ordinal(),
      new OpCode("mul",1,2,1,none));
    opcode_info.put(
      OP_div.ordinal(),
      new OpCode("div",1,2,1,none));
    opcode_info.put(
      OP_mod.ordinal(),
      new OpCode("mod",1,2,1,none));
    opcode_info.put(
      OP_add.ordinal(),
      new OpCode("add",1,2,1,none));
    opcode_info.put(
      OP_sub.ordinal(),
      new OpCode("sub",1,2,1,none));
    opcode_info.put(
      OP_pow.ordinal(),
      new OpCode("pow",1,2,1,none));
    opcode_info.put(
      OP_shl.ordinal(),
      new OpCode("shl",1,2,1,none));
    opcode_info.put(
      OP_sar.ordinal(),
      new OpCode("sar",1,2,1,none));
    opcode_info.put(
      OP_shr.ordinal(),
      new OpCode("shr",1,2,1,none));
    opcode_info.put(
      OP_lt.ordinal(),
      new OpCode("lt",1,2,1,none));
    opcode_info.put(
      OP_lte.ordinal(),
      new OpCode("lte",1,2,1,none));
    opcode_info.put(
      OP_gt.ordinal(),
      new OpCode("gt",1,2,1,none));
    opcode_info.put(
      OP_gte.ordinal(),
      new OpCode("gte",1,2,1,none));
    opcode_info.put(
      OP_instanceof.ordinal(),
      new OpCode("instanceof",1,2,1,none));
    opcode_info.put(
      OP_in.ordinal(),
      new OpCode("in",1,2,1,none));
    opcode_info.put(
      OP_eq.ordinal(),
      new OpCode("eq",1,2,1,none));
    opcode_info.put(
      OP_neq.ordinal(),
      new OpCode("neq",1,2,1,none));
    opcode_info.put(
      OP_strict_eq.ordinal(),
      new OpCode("strict_eq",1,2,1,none));
    opcode_info.put(
      OP_strict_neq.ordinal(),
      new OpCode("strict_neq",1,2,1,none));
    opcode_info.put(
      OP_and.ordinal(),
      new OpCode("and",1,2,1,none));
    opcode_info.put(
      OP_xor.ordinal(),
      new OpCode("xor",1,2,1,none));
    opcode_info.put(
      OP_or.ordinal(),
      new OpCode("or",1,2,1,none));
    opcode_info.put(
      OP_mul_pow10.ordinal(),
      new OpCode("mul_pow10",1,2,1,none));
    opcode_info.put(
      OP_math_mod.ordinal(),
      new OpCode("math_mod",1,2,1,none));
    opcode_info.put(
      OP_nop.ordinal(),
      new OpCode("nop",1,0,0,none));
    opcode_info.put(
      OP_push_minus1.ordinal(),
      new OpCode("push_minus1",1,0,1,none_int));
    opcode_info.put(
      OP_push_0.ordinal(),
      new OpCode("push_0",1,0,1,none_int));
    opcode_info.put(
      OP_push_1.ordinal(),
      new OpCode("push_1",1,0,1,none_int));
    opcode_info.put(
      OP_push_2.ordinal(),
      new OpCode("push_2",1,0,1,none_int));
    opcode_info.put(
      OP_push_3.ordinal(),
      new OpCode("push_3",1,0,1,none_int));
    opcode_info.put(
      OP_push_4.ordinal(),
      new OpCode("push_4",1,0,1,none_int));
    opcode_info.put(
      OP_push_5.ordinal(),
      new OpCode("push_5",1,0,1,none_int));
    opcode_info.put(
      OP_push_6.ordinal(),
      new OpCode("push_6",1,0,1,none_int));
    opcode_info.put(
      OP_push_7.ordinal(),
      new OpCode("push_7",1,0,1,none_int));
    opcode_info.put(
      OP_push_i8.ordinal(),
      new OpCode("push_i8",2,0,1,i8));
    opcode_info.put(
      OP_push_i16.ordinal(),
      new OpCode("push_i16",3,0,1,i16));
    opcode_info.put(
      OP_push_const8.ordinal(),
      new OpCode("push_const8",2,0,1,const8));
    opcode_info.put(
      OP_fclosure8.ordinal(),
      new OpCode("fclosure8",2,0,1,const8));
    opcode_info.put(
      OP_get_loc8.ordinal(),
      new OpCode("get_loc8",2,0,1,loc8));
    opcode_info.put(
      OP_put_loc8.ordinal(),
      new OpCode("put_loc8",2,1,0,loc8));
    opcode_info.put(
      OP_set_loc8.ordinal(),
      new OpCode("set_loc8",2,1,1,loc8));
    opcode_info.put(
      OP_get_loc0.ordinal(),
      new OpCode("get_loc0",1,0,1,none_loc));
    opcode_info.put(
      OP_get_loc1.ordinal(),
      new OpCode("get_loc1",1,0,1,none_loc));
    opcode_info.put(
      OP_get_loc2.ordinal(),
      new OpCode("get_loc2",1,0,1,none_loc));
    opcode_info.put(
      OP_get_loc3.ordinal(),
      new OpCode("get_loc3",1,0,1,none_loc));
    opcode_info.put(
      OP_put_loc0.ordinal(),
      new OpCode("put_loc0",1,1,0,none_loc));
    opcode_info.put(
      OP_put_loc1.ordinal(),
      new OpCode("put_loc1",1,1,0,none_loc));
    opcode_info.put(
      OP_put_loc2.ordinal(),
      new OpCode("put_loc2",1,1,0,none_loc));
    opcode_info.put(
      OP_put_loc3.ordinal(),
      new OpCode("put_loc3",1,1,0,none_loc));
    opcode_info.put(
      OP_set_loc0.ordinal(),
      new OpCode("set_loc0",1,1,1,none_loc));
    opcode_info.put(
      OP_set_loc1.ordinal(),
      new OpCode("set_loc1",1,1,1,none_loc));
    opcode_info.put(
      OP_set_loc2.ordinal(),
      new OpCode("set_loc2",1,1,1,none_loc));
    opcode_info.put(
      OP_set_loc3.ordinal(),
      new OpCode("set_loc3",1,1,1,none_loc));
    opcode_info.put(
      OP_get_arg0.ordinal(),
      new OpCode("get_arg0",1,0,1,none_arg));
    opcode_info.put(
      OP_get_arg1.ordinal(),
      new OpCode("get_arg1",1,0,1,none_arg));
    opcode_info.put(
      OP_get_arg2.ordinal(),
      new OpCode("get_arg2",1,0,1,none_arg));
    opcode_info.put(
      OP_get_arg3.ordinal(),
      new OpCode("get_arg3",1,0,1,none_arg));
    opcode_info.put(
      OP_put_arg0.ordinal(),
      new OpCode("put_arg0",1,1,0,none_arg));
    opcode_info.put(
      OP_put_arg1.ordinal(),
      new OpCode("put_arg1",1,1,0,none_arg));
    opcode_info.put(
      OP_put_arg2.ordinal(),
      new OpCode("put_arg2",1,1,0,none_arg));
    opcode_info.put(
      OP_put_arg3.ordinal(),
      new OpCode("put_arg3",1,1,0,none_arg));
    opcode_info.put(
      OP_set_arg0.ordinal(),
      new OpCode("set_arg0",1,1,1,none_arg));
    opcode_info.put(
      OP_set_arg1.ordinal(),
      new OpCode("set_arg1",1,1,1,none_arg));
    opcode_info.put(
      OP_set_arg2.ordinal(),
      new OpCode("set_arg2",1,1,1,none_arg));
    opcode_info.put(
      OP_set_arg3.ordinal(),
      new OpCode("set_arg3",1,1,1,none_arg));
    opcode_info.put(
      OP_get_var_ref0.ordinal(),
      new OpCode("get_var_ref0",1,0,1,none_var_ref));
    opcode_info.put(
      OP_get_var_ref1.ordinal(),
      new OpCode("get_var_ref1",1,0,1,none_var_ref));
    opcode_info.put(
      OP_get_var_ref2.ordinal(),
      new OpCode("get_var_ref2",1,0,1,none_var_ref));
    opcode_info.put(
      OP_get_var_ref3.ordinal(),
      new OpCode("get_var_ref3",1,0,1,none_var_ref));
    opcode_info.put(
      OP_put_var_ref0.ordinal(),
      new OpCode("put_var_ref0",1,1,0,none_var_ref));
    opcode_info.put(
      OP_put_var_ref1.ordinal(),
      new OpCode("put_var_ref1",1,1,0,none_var_ref));
    opcode_info.put(
      OP_put_var_ref2.ordinal(),
      new OpCode("put_var_ref2",1,1,0,none_var_ref));
    opcode_info.put(
      OP_put_var_ref3.ordinal(),
      new OpCode("put_var_ref3",1,1,0,none_var_ref));
    opcode_info.put(
      OP_set_var_ref0.ordinal(),
      new OpCode("set_var_ref0",1,1,1,none_var_ref));
    opcode_info.put(
      OP_set_var_ref1.ordinal(),
      new OpCode("set_var_ref1",1,1,1,none_var_ref));
    opcode_info.put(
      OP_set_var_ref2.ordinal(),
      new OpCode("set_var_ref2",1,1,1,none_var_ref));
    opcode_info.put(
      OP_set_var_ref3.ordinal(),
      new OpCode("set_var_ref3",1,1,1,none_var_ref));
    opcode_info.put(
      OP_get_length.ordinal(),
      new OpCode("get_length",1,1,1,none));
    opcode_info.put(
      OP_if_false8.ordinal(),
      new OpCode("if_false8",2,1,0,label8));
    opcode_info.put(
      OP_if_true8.ordinal(),
      new OpCode("if_true8",2,1,0,label8));
    opcode_info.put(
      OP_goto8.ordinal(),
      new OpCode("goto8",2,0,0,label8));
    opcode_info.put(
      OP_goto16.ordinal(),
      new OpCode("goto16",3,0,0,label16));
    opcode_info.put(
      OP_call0.ordinal(),
      new OpCode("call0",1,1,1,npopx));
    opcode_info.put(
      OP_call1.ordinal(),
      new OpCode("call1",1,1,1,npopx));
    opcode_info.put(
      OP_call2.ordinal(),
      new OpCode("call2",1,1,1,npopx));
    opcode_info.put(
      OP_call3.ordinal(),
      new OpCode("call3",1,1,1,npopx));
    opcode_info.put(
      OP_is_undefined.ordinal(),
      new OpCode("is_undefined",1,1,1,none));
    opcode_info.put(
      OP_is_null.ordinal(),
      new OpCode("is_null",1,1,1,none));
    opcode_info.put(
      OP_typeof_is_function.ordinal(),
      new OpCode("typeof_is_function",1,1,1,none));
 }
}