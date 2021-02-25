package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

import  static com.craftinginterpreters.lox.OPCodeEnum.*;
import  static com.craftinginterpreters.lox.OPCodeFormat.*;
public class JSOpCode {
  String id;
  int size;
  int n_pop;
  int n_push;
  OPCodeFormat f;

  public JSOpCode(String id, int size, int n_pop, int n_push, OPCodeFormat f) {
    this.id = id;
    this.size = size;
    this.n_pop = n_pop;
    this.n_push = n_push;
    this.f = f;
  }


  public static Map<Integer, JSOpCode> opcode_info;

  {
    opcode_info = new HashMap<>();
    opcode_info.put(
      OP_push_i32.ordinal(),
      new JSOpCode("push_i32",5,0,1,i32));
    opcode_info.put(
      OP_push_const.ordinal(),
      new JSOpCode("push_const",5,0,1,Const));
    opcode_info.put(
      OP_fclosure.ordinal(),
      new JSOpCode("fclosure",5,0,1,Const));
    opcode_info.put(
      OP_private_symbol.ordinal(),
      new JSOpCode("private_symbol",5,0,1,atom));
    opcode_info.put(
      OP_undefined.ordinal(),
      new JSOpCode("undefined",1,0,1,none));
    opcode_info.put(
      OP_null.ordinal(),
      new JSOpCode("null",1,0,1,none));
    opcode_info.put(
      OP_push_this.ordinal(),
      new JSOpCode("push_this",1,0,1,none));
    opcode_info.put(
      OP_push_false.ordinal(),
      new JSOpCode("push_false",1,0,1,none));
    opcode_info.put(
      OP_push_true.ordinal(),
      new JSOpCode("push_true",1,0,1,none));
    opcode_info.put(
      OP_object.ordinal(),
      new JSOpCode("object",1,0,1,none));
    opcode_info.put(
      OP_special_object.ordinal(),
      new JSOpCode("special_object",2,0,1,u8));
    opcode_info.put(
      OP_rest.ordinal(),
      new JSOpCode("rest",3,0,1,u16));
    opcode_info.put(
      OP_drop.ordinal(),
      new JSOpCode("drop",1,1,0,none));
    opcode_info.put(
      OP_nip.ordinal(),
      new JSOpCode("nip",1,2,1,none));
    opcode_info.put(
      OP_nip1.ordinal(),
      new JSOpCode("nip1",1,3,2,none));
    opcode_info.put(
      OP_dup.ordinal(),
      new JSOpCode("dup",1,1,2,none));
    opcode_info.put(
      OP_dup1.ordinal(),
      new JSOpCode("dup1",1,2,3,none));
    opcode_info.put(
      OP_dup2.ordinal(),
      new JSOpCode("dup2",1,2,4,none));
    opcode_info.put(
      OP_dup3.ordinal(),
      new JSOpCode("dup3",1,3,6,none));
    opcode_info.put(
      OP_insert2.ordinal(),
      new JSOpCode("insert2",1,2,3,none));
    opcode_info.put(
      OP_insert3.ordinal(),
      new JSOpCode("insert3",1,3,4,none));
    opcode_info.put(
      OP_insert4.ordinal(),
      new JSOpCode("insert4",1,4,5,none));
    opcode_info.put(
      OP_perm3.ordinal(),
      new JSOpCode("perm3",1,3,3,none));
    opcode_info.put(
      OP_perm4.ordinal(),
      new JSOpCode("perm4",1,4,4,none));
    opcode_info.put(
      OP_perm5.ordinal(),
      new JSOpCode("perm5",1,5,5,none));
    opcode_info.put(
      OP_swap.ordinal(),
      new JSOpCode("swap",1,2,2,none));
    opcode_info.put(
      OP_swap2.ordinal(),
      new JSOpCode("swap2",1,4,4,none));
    opcode_info.put(
      OP_rot3l.ordinal(),
      new JSOpCode("rot3l",1,3,3,none));
    opcode_info.put(
      OP_rot3r.ordinal(),
      new JSOpCode("rot3r",1,3,3,none));
    opcode_info.put(
      OP_rot4l.ordinal(),
      new JSOpCode("rot4l",1,4,4,none));
    opcode_info.put(
      OP_rot5l.ordinal(),
      new JSOpCode("rot5l",1,5,5,none));
    opcode_info.put(
      OP_call.ordinal(),
      new JSOpCode("call",3,1,1,npop));
    opcode_info.put(
      OP_tail_call.ordinal(),
      new JSOpCode("tail_call",3,1,0,npop));
    opcode_info.put(
      OP_call_method.ordinal(),
      new JSOpCode("call_method",3,2,1,npop));
    opcode_info.put(
      OP_array_from.ordinal(),
      new JSOpCode("array_from",3,0,1,npop));
    opcode_info.put(
      OP_apply.ordinal(),
      new JSOpCode("apply",3,3,1,u16));
    opcode_info.put(
      OP_return.ordinal(),
      new JSOpCode("return",1,1,0,none));
    opcode_info.put(
      OP_return_undef.ordinal(),
      new JSOpCode("return_undef",1,0,0,none));
    opcode_info.put(
      OP_check_ctor.ordinal(),
      new JSOpCode("check_ctor",1,0,0,none));
    opcode_info.put(
      OP_check_brand.ordinal(),
      new JSOpCode("check_brand",1,2,2,none));
    opcode_info.put(
      OP_add_brand.ordinal(),
      new JSOpCode("add_brand",1,2,0,none));
    opcode_info.put(
      OP_return_async.ordinal(),
      new JSOpCode("return_async",1,1,0,none));
    opcode_info.put(
      OP_throw.ordinal(),
      new JSOpCode("throw",1,1,0,none));
    opcode_info.put(
      OP_throw_var.ordinal(),
      new JSOpCode("throw_var",6,0,0,atom_u8));
    opcode_info.put(
      OP_eval.ordinal(),
      new JSOpCode("eval",5,1,1,npop_u16));
    opcode_info.put(
      OP_apply_eval.ordinal(),
      new JSOpCode("apply_eval",3,2,1,u16));
    opcode_info.put(
      OP_regexp.ordinal(),
      new JSOpCode("regexp",1,2,1,none));
    opcode_info.put(
      OP_get_super.ordinal(),
      new JSOpCode("get_super",1,1,1,none));
    opcode_info.put(
      OP_import.ordinal(),
      new JSOpCode("import",1,1,1,none));
    opcode_info.put(
      OP_check_var.ordinal(),
      new JSOpCode("check_var",5,0,1,atom));
    opcode_info.put(
      OP_get_var_undef.ordinal(),
      new JSOpCode("get_var_undef",5,0,1,atom));
    opcode_info.put(
      OP_get_var.ordinal(),
      new JSOpCode("get_var",5,0,1,atom));
    opcode_info.put(
      OP_put_var.ordinal(),
      new JSOpCode("put_var",5,1,0,atom));
    opcode_info.put(
      OP_put_var_init.ordinal(),
      new JSOpCode("put_var_init",5,1,0,atom));
    opcode_info.put(
      OP_put_var_strict.ordinal(),
      new JSOpCode("put_var_strict",5,2,0,atom));
    opcode_info.put(
      OP_get_ref_value.ordinal(),
      new JSOpCode("get_ref_value",1,2,3,none));
    opcode_info.put(
      OP_put_ref_value.ordinal(),
      new JSOpCode("put_ref_value",1,3,0,none));
    opcode_info.put(
      OP_define_var.ordinal(),
      new JSOpCode("define_var",6,0,0,atom_u8));
    opcode_info.put(
      OP_define_func.ordinal(),
      new JSOpCode("define_func",6,1,0,atom_u8));
    opcode_info.put(
      OP_get_field.ordinal(),
      new JSOpCode("get_field",5,1,1,atom));
    opcode_info.put(
      OP_get_field2.ordinal(),
      new JSOpCode("get_field2",5,1,2,atom));
    opcode_info.put(
      OP_put_field.ordinal(),
      new JSOpCode("put_field",5,2,0,atom));
    opcode_info.put(
      OP_get_private_field.ordinal(),
      new JSOpCode("get_private_field",1,2,1,none));
    opcode_info.put(
      OP_put_private_field.ordinal(),
      new JSOpCode("put_private_field",1,3,0,none));
    opcode_info.put(
      OP_get_array_el.ordinal(),
      new JSOpCode("get_array_el",1,2,1,none));
    opcode_info.put(
      OP_get_array_el2.ordinal(),
      new JSOpCode("get_array_el2",1,2,2,none));
    opcode_info.put(
      OP_put_array_el.ordinal(),
      new JSOpCode("put_array_el",1,3,0,none));
    opcode_info.put(
      OP_define_field.ordinal(),
      new JSOpCode("define_field",5,2,1,atom));
    opcode_info.put(
      OP_set_name.ordinal(),
      new JSOpCode("set_name",5,1,1,atom));
    opcode_info.put(
      OP_set_proto.ordinal(),
      new JSOpCode("set_proto",1,2,1,none));
    opcode_info.put(
      OP_append.ordinal(),
      new JSOpCode("append",1,3,2,none));
    opcode_info.put(
      OP_define_method.ordinal(),
      new JSOpCode("define_method",6,2,1,atom_u8));
    opcode_info.put(
      OP_define_class.ordinal(),
      new JSOpCode("define_class",6,2,2,atom_u8));
    opcode_info.put(
      OP_define_class_computed.ordinal(),
      new JSOpCode("define_class_computed",6,3,3,atom_u8));
    opcode_info.put(
      OP_get_loc.ordinal(),
      new JSOpCode("get_loc",3,0,1,loc));
    opcode_info.put(
      OP_put_loc.ordinal(),
      new JSOpCode("put_loc",3,1,0,loc));
    opcode_info.put(
      OP_set_loc.ordinal(),
      new JSOpCode("set_loc",3,1,1,loc));
    opcode_info.put(
      OP_get_arg.ordinal(),
      new JSOpCode("get_arg",3,0,1,arg));
    opcode_info.put(
      OP_put_arg.ordinal(),
      new JSOpCode("put_arg",3,1,0,arg));
    opcode_info.put(
      OP_set_arg.ordinal(),
      new JSOpCode("set_arg",3,1,1,arg));
    opcode_info.put(
      OP_get_var_ref.ordinal(),
      new JSOpCode("get_var_ref",3,0,1,var_ref));
    opcode_info.put(
      OP_put_var_ref.ordinal(),
      new JSOpCode("put_var_ref",3,1,0,var_ref));
    opcode_info.put(
      OP_set_var_ref.ordinal(),
      new JSOpCode("set_var_ref",3,1,1,var_ref));
    opcode_info.put(
      OP_get_loc_check.ordinal(),
      new JSOpCode("get_loc_check",3,0,1,loc));
    opcode_info.put(
      OP_put_loc_check.ordinal(),
      new JSOpCode("put_loc_check",3,1,0,loc));
    opcode_info.put(
      OP_put_loc_check_init.ordinal(),
      new JSOpCode("put_loc_check_init",3,1,0,loc));
    opcode_info.put(
      OP_close_loc.ordinal(),
      new JSOpCode("close_loc",3,0,0,loc));
    opcode_info.put(
      OP_if_false.ordinal(),
      new JSOpCode("if_false",5,1,0,label));
    opcode_info.put(
      OP_if_true.ordinal(),
      new JSOpCode("if_true",5,1,0,label));
    opcode_info.put(
      OP_goto.ordinal(),
      new JSOpCode("goto",5,0,0,label));
    opcode_info.put(
      OP_catch.ordinal(),
      new JSOpCode("catch",5,0,1,label));
    opcode_info.put(
      OP_gosub.ordinal(),
      new JSOpCode("gosub",5,0,0,label));
    opcode_info.put(
      OP_ret.ordinal(),
      new JSOpCode("ret",1,1,0,none));
    opcode_info.put(
      OP_to_object.ordinal(),
      new JSOpCode("to_object",1,1,1,none));
    opcode_info.put(
      OP_to_propkey.ordinal(),
      new JSOpCode("to_propkey",1,1,1,none));
    opcode_info.put(
      OP_to_propkey2.ordinal(),
      new JSOpCode("to_propkey2",1,2,2,none));
    opcode_info.put(
      OP_with_get_var.ordinal(),
      new JSOpCode("with_get_var",10,1,0,atom_label_u8));
    opcode_info.put(
      OP_with_put_var.ordinal(),
      new JSOpCode("with_put_var",10,2,1,atom_label_u8));
    opcode_info.put(
      OP_with_make_ref.ordinal(),
      new JSOpCode("with_make_ref",10,1,0,atom_label_u8));
    opcode_info.put(
      OP_with_get_ref.ordinal(),
      new JSOpCode("with_get_ref",10,1,0,atom_label_u8));
    opcode_info.put(
      OP_make_loc_ref.ordinal(),
      new JSOpCode("make_loc_ref",7,0,2,atom_u16));
    opcode_info.put(
      OP_make_arg_ref.ordinal(),
      new JSOpCode("make_arg_ref",7,0,2,atom_u16));
    opcode_info.put(
      OP_make_var_ref.ordinal(),
      new JSOpCode("make_var_ref",5,0,2,atom));
    opcode_info.put(
      OP_for_in_start.ordinal(),
      new JSOpCode("for_in_start",1,1,1,none));
    opcode_info.put(
      OP_for_of_start.ordinal(),
      new JSOpCode("for_of_start",1,1,3,none));
    opcode_info.put(
      OP_for_in_next.ordinal(),
      new JSOpCode("for_in_next",1,1,3,none));
    opcode_info.put(
      OP_for_of_next.ordinal(),
      new JSOpCode("for_of_next",2,3,5,u8));
    opcode_info.put(
      OP_iterator_close.ordinal(),
      new JSOpCode("iterator_close",1,3,0,none));
    opcode_info.put(
      OP_initial_yield.ordinal(),
      new JSOpCode("initial_yield",1,0,0,none));
    opcode_info.put(
      OP_yield.ordinal(),
      new JSOpCode("yield",1,1,2,none));
    opcode_info.put(
      OP_yield_star.ordinal(),
      new JSOpCode("yield_star",1,2,2,none));
    opcode_info.put(
      OP_await.ordinal(),
      new JSOpCode("await",1,1,1,none));
    opcode_info.put(
      OP_neg.ordinal(),
      new JSOpCode("neg",1,1,1,none));
    opcode_info.put(
      OP_plus.ordinal(),
      new JSOpCode("plus",1,1,1,none));
    opcode_info.put(
      OP_dec.ordinal(),
      new JSOpCode("dec",1,1,1,none));
    opcode_info.put(
      OP_inc.ordinal(),
      new JSOpCode("inc",1,1,1,none));
    opcode_info.put(
      OP_post_dec.ordinal(),
      new JSOpCode("post_dec",1,1,2,none));
    opcode_info.put(
      OP_post_inc.ordinal(),
      new JSOpCode("post_inc",1,1,2,none));
    opcode_info.put(
      OP_dec_loc.ordinal(),
      new JSOpCode("dec_loc",2,0,0,loc8));
    opcode_info.put(
      OP_inc_loc.ordinal(),
      new JSOpCode("inc_loc",2,0,0,loc8));
    opcode_info.put(
      OP_add_loc.ordinal(),
      new JSOpCode("add_loc",2,1,0,loc8));
    opcode_info.put(
      OP_not.ordinal(),
      new JSOpCode("not",1,1,1,none));
    opcode_info.put(
      OP_lnot.ordinal(),
      new JSOpCode("lnot",1,1,1,none));
    opcode_info.put(
      OP_typeof.ordinal(),
      new JSOpCode("typeof",1,1,1,none));
    opcode_info.put(
      OP_delete.ordinal(),
      new JSOpCode("delete",1,2,1,none));
    opcode_info.put(
      OP_delete_var.ordinal(),
      new JSOpCode("delete_var",5,0,1,atom));
    opcode_info.put(
      OP_mul.ordinal(),
      new JSOpCode("mul",1,2,1,none));
    opcode_info.put(
      OP_div.ordinal(),
      new JSOpCode("div",1,2,1,none));
    opcode_info.put(
      OP_mod.ordinal(),
      new JSOpCode("mod",1,2,1,none));
    opcode_info.put(
      OP_add.ordinal(),
      new JSOpCode("add",1,2,1,none));
    opcode_info.put(
      OP_sub.ordinal(),
      new JSOpCode("sub",1,2,1,none));
    opcode_info.put(
      OP_pow.ordinal(),
      new JSOpCode("pow",1,2,1,none));
    opcode_info.put(
      OP_shl.ordinal(),
      new JSOpCode("shl",1,2,1,none));
    opcode_info.put(
      OP_sar.ordinal(),
      new JSOpCode("sar",1,2,1,none));
    opcode_info.put(
      OP_shr.ordinal(),
      new JSOpCode("shr",1,2,1,none));
    opcode_info.put(
      OP_lt.ordinal(),
      new JSOpCode("lt",1,2,1,none));
    opcode_info.put(
      OP_lte.ordinal(),
      new JSOpCode("lte",1,2,1,none));
    opcode_info.put(
      OP_gt.ordinal(),
      new JSOpCode("gt",1,2,1,none));
    opcode_info.put(
      OP_gte.ordinal(),
      new JSOpCode("gte",1,2,1,none));
    opcode_info.put(
      OP_instanceof.ordinal(),
      new JSOpCode("instanceof",1,2,1,none));
    opcode_info.put(
      OP_in.ordinal(),
      new JSOpCode("in",1,2,1,none));
    opcode_info.put(
      OP_eq.ordinal(),
      new JSOpCode("eq",1,2,1,none));
    opcode_info.put(
      OP_neq.ordinal(),
      new JSOpCode("neq",1,2,1,none));
    opcode_info.put(
      OP_strict_eq.ordinal(),
      new JSOpCode("strict_eq",1,2,1,none));
    opcode_info.put(
      OP_strict_neq.ordinal(),
      new JSOpCode("strict_neq",1,2,1,none));
    opcode_info.put(
      OP_and.ordinal(),
      new JSOpCode("and",1,2,1,none));
    opcode_info.put(
      OP_xor.ordinal(),
      new JSOpCode("xor",1,2,1,none));
    opcode_info.put(
      OP_or.ordinal(),
      new JSOpCode("or",1,2,1,none));
    opcode_info.put(
      OP_mul_pow10.ordinal(),
      new JSOpCode("mul_pow10",1,2,1,none));
    opcode_info.put(
      OP_math_mod.ordinal(),
      new JSOpCode("math_mod",1,2,1,none));
    opcode_info.put(
      OP_nop.ordinal(),
      new JSOpCode("nop",1,0,0,none));
    opcode_info.put(
      OP_push_minus1.ordinal(),
      new JSOpCode("push_minus1",1,0,1,none_int));
    opcode_info.put(
      OP_push_0.ordinal(),
      new JSOpCode("push_0",1,0,1,none_int));
    opcode_info.put(
      OP_push_1.ordinal(),
      new JSOpCode("push_1",1,0,1,none_int));
    opcode_info.put(
      OP_push_2.ordinal(),
      new JSOpCode("push_2",1,0,1,none_int));
    opcode_info.put(
      OP_push_3.ordinal(),
      new JSOpCode("push_3",1,0,1,none_int));
    opcode_info.put(
      OP_push_4.ordinal(),
      new JSOpCode("push_4",1,0,1,none_int));
    opcode_info.put(
      OP_push_5.ordinal(),
      new JSOpCode("push_5",1,0,1,none_int));
    opcode_info.put(
      OP_push_6.ordinal(),
      new JSOpCode("push_6",1,0,1,none_int));
    opcode_info.put(
      OP_push_7.ordinal(),
      new JSOpCode("push_7",1,0,1,none_int));
    opcode_info.put(
      OP_push_i8.ordinal(),
      new JSOpCode("push_i8",2,0,1,i8));
    opcode_info.put(
      OP_push_i16.ordinal(),
      new JSOpCode("push_i16",3,0,1,i16));
    opcode_info.put(
      OP_push_const8.ordinal(),
      new JSOpCode("push_const8",2,0,1,const8));
    opcode_info.put(
      OP_fclosure8.ordinal(),
      new JSOpCode("fclosure8",2,0,1,const8));
    opcode_info.put(
      OP_get_loc8.ordinal(),
      new JSOpCode("get_loc8",2,0,1,loc8));
    opcode_info.put(
      OP_put_loc8.ordinal(),
      new JSOpCode("put_loc8",2,1,0,loc8));
    opcode_info.put(
      OP_set_loc8.ordinal(),
      new JSOpCode("set_loc8",2,1,1,loc8));
    opcode_info.put(
      OP_get_loc0.ordinal(),
      new JSOpCode("get_loc0",1,0,1,none_loc));
    opcode_info.put(
      OP_get_loc1.ordinal(),
      new JSOpCode("get_loc1",1,0,1,none_loc));
    opcode_info.put(
      OP_get_loc2.ordinal(),
      new JSOpCode("get_loc2",1,0,1,none_loc));
    opcode_info.put(
      OP_get_loc3.ordinal(),
      new JSOpCode("get_loc3",1,0,1,none_loc));
    opcode_info.put(
      OP_put_loc0.ordinal(),
      new JSOpCode("put_loc0",1,1,0,none_loc));
    opcode_info.put(
      OP_put_loc1.ordinal(),
      new JSOpCode("put_loc1",1,1,0,none_loc));
    opcode_info.put(
      OP_put_loc2.ordinal(),
      new JSOpCode("put_loc2",1,1,0,none_loc));
    opcode_info.put(
      OP_put_loc3.ordinal(),
      new JSOpCode("put_loc3",1,1,0,none_loc));
    opcode_info.put(
      OP_set_loc0.ordinal(),
      new JSOpCode("set_loc0",1,1,1,none_loc));
    opcode_info.put(
      OP_set_loc1.ordinal(),
      new JSOpCode("set_loc1",1,1,1,none_loc));
    opcode_info.put(
      OP_set_loc2.ordinal(),
      new JSOpCode("set_loc2",1,1,1,none_loc));
    opcode_info.put(
      OP_set_loc3.ordinal(),
      new JSOpCode("set_loc3",1,1,1,none_loc));
    opcode_info.put(
      OP_get_arg0.ordinal(),
      new JSOpCode("get_arg0",1,0,1,none_arg));
    opcode_info.put(
      OP_get_arg1.ordinal(),
      new JSOpCode("get_arg1",1,0,1,none_arg));
    opcode_info.put(
      OP_get_arg2.ordinal(),
      new JSOpCode("get_arg2",1,0,1,none_arg));
    opcode_info.put(
      OP_get_arg3.ordinal(),
      new JSOpCode("get_arg3",1,0,1,none_arg));
    opcode_info.put(
      OP_put_arg0.ordinal(),
      new JSOpCode("put_arg0",1,1,0,none_arg));
    opcode_info.put(
      OP_put_arg1.ordinal(),
      new JSOpCode("put_arg1",1,1,0,none_arg));
    opcode_info.put(
      OP_put_arg2.ordinal(),
      new JSOpCode("put_arg2",1,1,0,none_arg));
    opcode_info.put(
      OP_put_arg3.ordinal(),
      new JSOpCode("put_arg3",1,1,0,none_arg));
    opcode_info.put(
      OP_set_arg0.ordinal(),
      new JSOpCode("set_arg0",1,1,1,none_arg));
    opcode_info.put(
      OP_set_arg1.ordinal(),
      new JSOpCode("set_arg1",1,1,1,none_arg));
    opcode_info.put(
      OP_set_arg2.ordinal(),
      new JSOpCode("set_arg2",1,1,1,none_arg));
    opcode_info.put(
      OP_set_arg3.ordinal(),
      new JSOpCode("set_arg3",1,1,1,none_arg));
    opcode_info.put(
      OP_get_var_ref0.ordinal(),
      new JSOpCode("get_var_ref0",1,0,1,none_var_ref));
    opcode_info.put(
      OP_get_var_ref1.ordinal(),
      new JSOpCode("get_var_ref1",1,0,1,none_var_ref));
    opcode_info.put(
      OP_get_var_ref2.ordinal(),
      new JSOpCode("get_var_ref2",1,0,1,none_var_ref));
    opcode_info.put(
      OP_get_var_ref3.ordinal(),
      new JSOpCode("get_var_ref3",1,0,1,none_var_ref));
    opcode_info.put(
      OP_put_var_ref0.ordinal(),
      new JSOpCode("put_var_ref0",1,1,0,none_var_ref));
    opcode_info.put(
      OP_put_var_ref1.ordinal(),
      new JSOpCode("put_var_ref1",1,1,0,none_var_ref));
    opcode_info.put(
      OP_put_var_ref2.ordinal(),
      new JSOpCode("put_var_ref2",1,1,0,none_var_ref));
    opcode_info.put(
      OP_put_var_ref3.ordinal(),
      new JSOpCode("put_var_ref3",1,1,0,none_var_ref));
    opcode_info.put(
      OP_set_var_ref0.ordinal(),
      new JSOpCode("set_var_ref0",1,1,1,none_var_ref));
    opcode_info.put(
      OP_set_var_ref1.ordinal(),
      new JSOpCode("set_var_ref1",1,1,1,none_var_ref));
    opcode_info.put(
      OP_set_var_ref2.ordinal(),
      new JSOpCode("set_var_ref2",1,1,1,none_var_ref));
    opcode_info.put(
      OP_set_var_ref3.ordinal(),
      new JSOpCode("set_var_ref3",1,1,1,none_var_ref));
    opcode_info.put(
      OP_get_length.ordinal(),
      new JSOpCode("get_length",1,1,1,none));
    opcode_info.put(
      OP_if_false8.ordinal(),
      new JSOpCode("if_false8",2,1,0,label8));
    opcode_info.put(
      OP_if_true8.ordinal(),
      new JSOpCode("if_true8",2,1,0,label8));
    opcode_info.put(
      OP_goto8.ordinal(),
      new JSOpCode("goto8",2,0,0,label8));
    opcode_info.put(
      OP_goto16.ordinal(),
      new JSOpCode("goto16",3,0,0,label16));
    opcode_info.put(
      OP_call0.ordinal(),
      new JSOpCode("call0",1,1,1,npopx));
    opcode_info.put(
      OP_call1.ordinal(),
      new JSOpCode("call1",1,1,1,npopx));
    opcode_info.put(
      OP_call2.ordinal(),
      new JSOpCode("call2",1,1,1,npopx));
    opcode_info.put(
      OP_call3.ordinal(),
      new JSOpCode("call3",1,1,1,npopx));
    opcode_info.put(
      OP_is_undefined.ordinal(),
      new JSOpCode("is_undefined",1,1,1,none));
    opcode_info.put(
      OP_is_null.ordinal(),
      new JSOpCode("is_null",1,1,1,none));
    opcode_info.put(
      OP_typeof_is_function.ordinal(),
      new JSOpCode("typeof_is_function",1,1,1,none));
 }
}