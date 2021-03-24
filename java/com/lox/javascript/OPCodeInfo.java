package com.lox.javascript;

import java.util.HashMap;
import java.util.Map;

public class OPCodeInfo {
  public static Map<Integer, JSOpCode> opcode_info;
  public static Map<Integer, OPCodeEnum> opcode_enum;

  static {
    opcode_enum = new HashMap<>();
    for (OPCodeEnum codeEnum: OPCodeEnum.values()) {
      opcode_enum.put(codeEnum.ordinal(), codeEnum);
    }
    
    opcode_info = new HashMap<>();
    opcode_info.put(
      OPCodeEnum.OP_invalid.ordinal(),
      new JSOpCode("invalid",1,0,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_push_i32.ordinal(),
      new JSOpCode("push_i32",5,0,1, OPCodeFormat.i32));
    opcode_info.put(
      OPCodeEnum.OP_push_const.ordinal(),
      new JSOpCode("push_const",5,0,1, OPCodeFormat.Const));
    opcode_info.put(
      OPCodeEnum.OP_fclosure.ordinal(),
      new JSOpCode("fclosure",5,0,1, OPCodeFormat.Const));
    opcode_info.put(
      OPCodeEnum.OP_push_atom_value.ordinal(),
      new JSOpCode("push_atom_value",5,0,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_private_symbol.ordinal(),
      new JSOpCode("private_symbol",5,0,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_undefined.ordinal(),
      new JSOpCode("undefined",1,0,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_null.ordinal(),
      new JSOpCode("null",1,0,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_push_this.ordinal(),
      new JSOpCode("push_this",1,0,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_push_false.ordinal(),
      new JSOpCode("push_false",1,0,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_push_true.ordinal(),
      new JSOpCode("push_true",1,0,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_object.ordinal(),
      new JSOpCode("object",1,0,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_special_object.ordinal(),
      new JSOpCode("special_object",2,0,1, OPCodeFormat.u8));
    opcode_info.put(
      OPCodeEnum.OP_rest.ordinal(),
      new JSOpCode("rest",3,0,1, OPCodeFormat.u16));
    opcode_info.put(
      OPCodeEnum.OP_drop.ordinal(),
      new JSOpCode("drop",1,1,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_nip.ordinal(),
      new JSOpCode("nip",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_nip1.ordinal(),
      new JSOpCode("nip1",1,3,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_dup.ordinal(),
      new JSOpCode("dup",1,1,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_dup1.ordinal(),
      new JSOpCode("dup1",1,2,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_dup2.ordinal(),
      new JSOpCode("dup2",1,2,4, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_dup3.ordinal(),
      new JSOpCode("dup3",1,3,6, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_insert2.ordinal(),
      new JSOpCode("insert2",1,2,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_insert3.ordinal(),
      new JSOpCode("insert3",1,3,4, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_insert4.ordinal(),
      new JSOpCode("insert4",1,4,5, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_perm3.ordinal(),
      new JSOpCode("perm3",1,3,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_perm4.ordinal(),
      new JSOpCode("perm4",1,4,4, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_perm5.ordinal(),
      new JSOpCode("perm5",1,5,5, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_swap.ordinal(),
      new JSOpCode("swap",1,2,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_swap2.ordinal(),
      new JSOpCode("swap2",1,4,4, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_rot3l.ordinal(),
      new JSOpCode("rot3l",1,3,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_rot3r.ordinal(),
      new JSOpCode("rot3r",1,3,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_rot4l.ordinal(),
      new JSOpCode("rot4l",1,4,4, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_rot5l.ordinal(),
      new JSOpCode("rot5l",1,5,5, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_call_constructor.ordinal(),
      new JSOpCode("call_constructor",3,2,1, OPCodeFormat.npop));
    opcode_info.put(
      OPCodeEnum.OP_call.ordinal(),
      new JSOpCode("call",3,1,1, OPCodeFormat.npop));
    opcode_info.put(
      OPCodeEnum.OP_tail_call.ordinal(),
      new JSOpCode("tail_call",3,1,0, OPCodeFormat.npop));
    opcode_info.put(
      OPCodeEnum.OP_call_method.ordinal(),
      new JSOpCode("call_method",3,2,1, OPCodeFormat.npop));
    opcode_info.put(
      OPCodeEnum.OP_tail_call_method.ordinal(),
      new JSOpCode("tail_call_method",3,2,0, OPCodeFormat.npop));
    opcode_info.put(
      OPCodeEnum.OP_array_from.ordinal(),
      new JSOpCode("array_from",3,0,1, OPCodeFormat.npop));
    opcode_info.put(
      OPCodeEnum.OP_apply.ordinal(),
      new JSOpCode("apply",3,3,1, OPCodeFormat.u16));
    opcode_info.put(
      OPCodeEnum.OP_return.ordinal(),
      new JSOpCode("return",1,1,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_return_undef.ordinal(),
      new JSOpCode("return_undef",1,0,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_check_ctor_return.ordinal(),
      new JSOpCode("check_ctor_return",1,1,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_check_ctor.ordinal(),
      new JSOpCode("check_ctor",1,0,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_check_brand.ordinal(),
      new JSOpCode("check_brand",1,2,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_add_brand.ordinal(),
      new JSOpCode("add_brand",1,2,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_return_async.ordinal(),
      new JSOpCode("return_async",1,1,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_throw.ordinal(),
      new JSOpCode("throw",1,1,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_throw_var.ordinal(),
      new JSOpCode("throw_var",6,0,0, OPCodeFormat.atom_u8));
    opcode_info.put(
      OPCodeEnum.OP_eval.ordinal(),
      new JSOpCode("eval",5,1,1, OPCodeFormat.npop_u16));
    opcode_info.put(
      OPCodeEnum.OP_apply_eval.ordinal(),
      new JSOpCode("apply_eval",3,2,1, OPCodeFormat.u16));
    opcode_info.put(
      OPCodeEnum.OP_regexp.ordinal(),
      new JSOpCode("regexp",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_get_super.ordinal(),
      new JSOpCode("get_super",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_import.ordinal(),
      new JSOpCode("import",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_check_var.ordinal(),
      new JSOpCode("check_var",5,0,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_get_var_undef.ordinal(),
      new JSOpCode("get_var_undef",5,0,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_get_var.ordinal(),
      new JSOpCode("get_var",5,0,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_put_var.ordinal(),
      new JSOpCode("put_var",5,1,0, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_put_var_init.ordinal(),
      new JSOpCode("put_var_init",5,1,0, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_put_var_strict.ordinal(),
      new JSOpCode("put_var_strict",5,2,0, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_get_ref_value.ordinal(),
      new JSOpCode("get_ref_value",1,2,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_put_ref_value.ordinal(),
      new JSOpCode("put_ref_value",1,3,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_define_var.ordinal(),
      new JSOpCode("define_var",6,0,0, OPCodeFormat.atom_u8));
    opcode_info.put(
      OPCodeEnum.OP_check_define_var.ordinal(),
      new JSOpCode("check_define_var",6,0,0, OPCodeFormat.atom_u8));
    opcode_info.put(
      OPCodeEnum.OP_define_func.ordinal(),
      new JSOpCode("define_func",6,1,0, OPCodeFormat.atom_u8));
    opcode_info.put(
      OPCodeEnum.OP_get_field.ordinal(),
      new JSOpCode("get_field",5,1,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_get_field2.ordinal(),
      new JSOpCode("get_field2",5,1,2, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_put_field.ordinal(),
      new JSOpCode("put_field",5,2,0, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_get_private_field.ordinal(),
      new JSOpCode("get_private_field",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_put_private_field.ordinal(),
      new JSOpCode("put_private_field",1,3,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_define_private_field.ordinal(),
      new JSOpCode("define_private_field",1,3,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_get_array_el.ordinal(),
      new JSOpCode("get_array_el",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_get_array_el2.ordinal(),
      new JSOpCode("get_array_el2",1,2,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_put_array_el.ordinal(),
      new JSOpCode("put_array_el",1,3,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_get_super_value.ordinal(),
      new JSOpCode("get_super_value",1,3,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_put_super_value.ordinal(),
      new JSOpCode("put_super_value",1,4,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_define_field.ordinal(),
      new JSOpCode("define_field",5,2,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_set_name.ordinal(),
      new JSOpCode("set_name",5,1,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_set_name_computed.ordinal(),
      new JSOpCode("set_name_computed",1,2,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_set_proto.ordinal(),
      new JSOpCode("set_proto",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_set_home_object.ordinal(),
      new JSOpCode("set_home_object",1,2,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_define_array_el.ordinal(),
      new JSOpCode("define_array_el",1,3,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_append.ordinal(),
      new JSOpCode("append",1,3,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_copy_data_properties.ordinal(),
      new JSOpCode("copy_data_properties",2,3,3, OPCodeFormat.u8));
    opcode_info.put(
      OPCodeEnum.OP_define_method.ordinal(),
      new JSOpCode("define_method",6,2,1, OPCodeFormat.atom_u8));
    opcode_info.put(
      OPCodeEnum.OP_define_method_computed.ordinal(),
      new JSOpCode("define_method_computed",2,3,1, OPCodeFormat.u8));
    opcode_info.put(
      OPCodeEnum.OP_define_class.ordinal(),
      new JSOpCode("define_class",6,2,2, OPCodeFormat.atom_u8));
    opcode_info.put(
      OPCodeEnum.OP_define_class_computed.ordinal(),
      new JSOpCode("define_class_computed",6,3,3, OPCodeFormat.atom_u8));
    opcode_info.put(
      OPCodeEnum.OP_get_loc.ordinal(),
      new JSOpCode("get_loc",3,0,1, OPCodeFormat.loc));
    opcode_info.put(
      OPCodeEnum.OP_put_loc.ordinal(),
      new JSOpCode("put_loc",3,1,0, OPCodeFormat.loc));
    opcode_info.put(
      OPCodeEnum.OP_set_loc.ordinal(),
      new JSOpCode("set_loc",3,1,1, OPCodeFormat.loc));
    opcode_info.put(
      OPCodeEnum.OP_get_arg.ordinal(),
      new JSOpCode("get_arg",3,0,1, OPCodeFormat.arg));
    opcode_info.put(
      OPCodeEnum.OP_put_arg.ordinal(),
      new JSOpCode("put_arg",3,1,0, OPCodeFormat.arg));
    opcode_info.put(
      OPCodeEnum.OP_set_arg.ordinal(),
      new JSOpCode("set_arg",3,1,1, OPCodeFormat.arg));
    opcode_info.put(
      OPCodeEnum.OP_get_var_ref.ordinal(),
      new JSOpCode("get_var_ref",3,0,1, OPCodeFormat.var_ref));
    opcode_info.put(
      OPCodeEnum.OP_put_var_ref.ordinal(),
      new JSOpCode("put_var_ref",3,1,0, OPCodeFormat.var_ref));
    opcode_info.put(
      OPCodeEnum.OP_set_var_ref.ordinal(),
      new JSOpCode("set_var_ref",3,1,1, OPCodeFormat.var_ref));
    opcode_info.put(
      OPCodeEnum.OP_set_loc_uninitialized.ordinal(),
      new JSOpCode("set_loc_uninitialized",3,0,0, OPCodeFormat.loc));
    opcode_info.put(
      OPCodeEnum.OP_get_loc_check.ordinal(),
      new JSOpCode("get_loc_check",3,0,1, OPCodeFormat.loc));
    opcode_info.put(
      OPCodeEnum.OP_put_loc_check.ordinal(),
      new JSOpCode("put_loc_check",3,1,0, OPCodeFormat.loc));
    opcode_info.put(
      OPCodeEnum.OP_put_loc_check_init.ordinal(),
      new JSOpCode("put_loc_check_init",3,1,0, OPCodeFormat.loc));
    opcode_info.put(
      OPCodeEnum.OP_get_var_ref_check.ordinal(),
      new JSOpCode("get_var_ref_check",3,0,1, OPCodeFormat.var_ref));
    opcode_info.put(
      OPCodeEnum.OP_put_var_ref_check.ordinal(),
      new JSOpCode("put_var_ref_check",3,1,0, OPCodeFormat.var_ref));
    opcode_info.put(
      OPCodeEnum.OP_put_var_ref_check_init.ordinal(),
      new JSOpCode("put_var_ref_check_init",3,1,0, OPCodeFormat.var_ref));
    opcode_info.put(
      OPCodeEnum.OP_close_loc.ordinal(),
      new JSOpCode("close_loc",3,0,0, OPCodeFormat.loc));
    opcode_info.put(
      OPCodeEnum.OP_if_false.ordinal(),
      new JSOpCode("if_false",5,1,0, OPCodeFormat.label));
    opcode_info.put(
      OPCodeEnum.OP_if_true.ordinal(),
      new JSOpCode("if_true",5,1,0, OPCodeFormat.label));
    opcode_info.put(
      OPCodeEnum.OP_goto.ordinal(),
      new JSOpCode("goto",5,0,0, OPCodeFormat.label));
    opcode_info.put(
      OPCodeEnum.OP_catch.ordinal(),
      new JSOpCode("catch",5,0,1, OPCodeFormat.label));
    opcode_info.put(
      OPCodeEnum.OP_gosub.ordinal(),
      new JSOpCode("gosub",5,0,0, OPCodeFormat.label));
    opcode_info.put(
      OPCodeEnum.OP_ret.ordinal(),
      new JSOpCode("ret",1,1,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_to_object.ordinal(),
      new JSOpCode("to_object",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_to_propkey.ordinal(),
      new JSOpCode("to_propkey",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_to_propkey2.ordinal(),
      new JSOpCode("to_propkey2",1,2,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_with_get_var.ordinal(),
      new JSOpCode("with_get_var",10,1,0, OPCodeFormat.atom_label_u8));
    opcode_info.put(
      OPCodeEnum.OP_with_put_var.ordinal(),
      new JSOpCode("with_put_var",10,2,1, OPCodeFormat.atom_label_u8));
    opcode_info.put(
      OPCodeEnum.OP_with_delete_var.ordinal(),
      new JSOpCode("with_delete_var",10,1,0, OPCodeFormat.atom_label_u8));
    opcode_info.put(
      OPCodeEnum.OP_with_make_ref.ordinal(),
      new JSOpCode("with_make_ref",10,1,0, OPCodeFormat.atom_label_u8));
    opcode_info.put(
      OPCodeEnum.OP_with_get_ref.ordinal(),
      new JSOpCode("with_get_ref",10,1,0, OPCodeFormat.atom_label_u8));
    opcode_info.put(
      OPCodeEnum.OP_with_get_ref_undef.ordinal(),
      new JSOpCode("with_get_ref_undef",10,1,0, OPCodeFormat.atom_label_u8));
    opcode_info.put(
      OPCodeEnum.OP_make_loc_ref.ordinal(),
      new JSOpCode("make_loc_ref",7,0,2, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_make_arg_ref.ordinal(),
      new JSOpCode("make_arg_ref",7,0,2, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_make_var_ref_ref.ordinal(),
      new JSOpCode("make_var_ref_ref",7,0,2, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_make_var_ref.ordinal(),
      new JSOpCode("make_var_ref",5,0,2, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_for_in_start.ordinal(),
      new JSOpCode("for_in_start",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_for_of_start.ordinal(),
      new JSOpCode("for_of_start",1,1,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_for_await_of_start.ordinal(),
      new JSOpCode("for_await_of_start",1,1,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_for_in_next.ordinal(),
      new JSOpCode("for_in_next",1,1,3, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_for_of_next.ordinal(),
      new JSOpCode("for_of_next",2,3,5, OPCodeFormat.u8));
    opcode_info.put(
      OPCodeEnum.OP_for_await_of_next.ordinal(),
      new JSOpCode("for_await_of_next",1,3,4, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_iterator_get_value_done.ordinal(),
      new JSOpCode("iterator_get_value_done",1,1,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_iterator_close.ordinal(),
      new JSOpCode("iterator_close",1,3,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_iterator_close_return.ordinal(),
      new JSOpCode("iterator_close_return",1,4,4, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_async_iterator_close.ordinal(),
      new JSOpCode("async_iterator_close",1,3,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_async_iterator_next.ordinal(),
      new JSOpCode("async_iterator_next",1,4,4, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_async_iterator_get.ordinal(),
      new JSOpCode("async_iterator_get",2,4,5, OPCodeFormat.u8));
    opcode_info.put(
      OPCodeEnum.OP_initial_yield.ordinal(),
      new JSOpCode("initial_yield",1,0,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_yield.ordinal(),
      new JSOpCode("yield",1,1,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_yield_star.ordinal(),
      new JSOpCode("yield_star",1,2,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_async_yield_star.ordinal(),
      new JSOpCode("async_yield_star",1,1,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_await.ordinal(),
      new JSOpCode("await",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_neg.ordinal(),
      new JSOpCode("neg",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_plus.ordinal(),
      new JSOpCode("plus",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_dec.ordinal(),
      new JSOpCode("dec",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_inc.ordinal(),
      new JSOpCode("inc",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_post_dec.ordinal(),
      new JSOpCode("post_dec",1,1,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_post_inc.ordinal(),
      new JSOpCode("post_inc",1,1,2, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_dec_loc.ordinal(),
      new JSOpCode("dec_loc",2,0,0, OPCodeFormat.loc8));
    opcode_info.put(
      OPCodeEnum.OP_inc_loc.ordinal(),
      new JSOpCode("inc_loc",2,0,0, OPCodeFormat.loc8));
    opcode_info.put(
      OPCodeEnum.OP_add_loc.ordinal(),
      new JSOpCode("add_loc",2,1,0, OPCodeFormat.loc8));
    opcode_info.put(
      OPCodeEnum.OP_not.ordinal(),
      new JSOpCode("not",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_lnot.ordinal(),
      new JSOpCode("lnot",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_typeof.ordinal(),
      new JSOpCode("typeof",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_delete.ordinal(),
      new JSOpCode("delete",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_delete_var.ordinal(),
      new JSOpCode("delete_var",5,0,1, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_mul.ordinal(),
      new JSOpCode("mul",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_div.ordinal(),
      new JSOpCode("div",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_mod.ordinal(),
      new JSOpCode("mod",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_add.ordinal(),
      new JSOpCode("add",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_sub.ordinal(),
      new JSOpCode("sub",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_pow.ordinal(),
      new JSOpCode("pow",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_shl.ordinal(),
      new JSOpCode("shl",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_sar.ordinal(),
      new JSOpCode("sar",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_shr.ordinal(),
      new JSOpCode("shr",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_lt.ordinal(),
      new JSOpCode("lt",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_lte.ordinal(),
      new JSOpCode("lte",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_gt.ordinal(),
      new JSOpCode("gt",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_gte.ordinal(),
      new JSOpCode("gte",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_instanceof.ordinal(),
      new JSOpCode("instanceof",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_in.ordinal(),
      new JSOpCode("in",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_eq.ordinal(),
      new JSOpCode("eq",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_neq.ordinal(),
      new JSOpCode("neq",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_strict_eq.ordinal(),
      new JSOpCode("strict_eq",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_strict_neq.ordinal(),
      new JSOpCode("strict_neq",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_and.ordinal(),
      new JSOpCode("and",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_xor.ordinal(),
      new JSOpCode("xor",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_or.ordinal(),
      new JSOpCode("or",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_is_undefined_or_null.ordinal(),
      new JSOpCode("is_undefined_or_null",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_mul_pow10.ordinal(),
      new JSOpCode("mul_pow10",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_math_mod.ordinal(),
      new JSOpCode("math_mod",1,2,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_nop.ordinal(),
      new JSOpCode("nop",1,0,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_print.ordinal(),
      new JSOpCode("print",1,1,0, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_set_arg_valid_upto.ordinal(),
      new JSOpCode("set_arg_valid_upto",3,0,0, OPCodeFormat.arg));
    opcode_info.put(
      OPCodeEnum.OP_enter_scope.ordinal(),
      new JSOpCode("enter_scope",3,0,0, OPCodeFormat.u16));
    opcode_info.put(
      OPCodeEnum.OP_leave_scope.ordinal(),
      new JSOpCode("leave_scope",3,0,0, OPCodeFormat.u16));
    opcode_info.put(
      OPCodeEnum.OP_label.ordinal(),
      new JSOpCode("label",5,0,0, OPCodeFormat.label));
    opcode_info.put(
      OPCodeEnum.OP_scope_get_var_undef.ordinal(),
      new JSOpCode("scope_get_var_undef",7,0,1, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_get_var.ordinal(),
      new JSOpCode("scope_get_var",7,0,1, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_put_var.ordinal(),
      new JSOpCode("scope_put_var",7,1,0, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_delete_var.ordinal(),
      new JSOpCode("scope_delete_var",7,0,1, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_make_ref.ordinal(),
      new JSOpCode("scope_make_ref",11,0,2, OPCodeFormat.atom_label_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_get_ref.ordinal(),
      new JSOpCode("scope_get_ref",7,0,2, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_put_var_init.ordinal(),
      new JSOpCode("scope_put_var_init",7,0,2, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_get_private_field.ordinal(),
      new JSOpCode("scope_get_private_field",7,1,1, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_get_private_field2.ordinal(),
      new JSOpCode("scope_get_private_field2",7,1,2, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_scope_put_private_field.ordinal(),
      new JSOpCode("scope_put_private_field",7,1,1, OPCodeFormat.atom_u16));
    opcode_info.put(
      OPCodeEnum.OP_set_class_name.ordinal(),
      new JSOpCode("set_class_name",5,1,1, OPCodeFormat.u32));
    opcode_info.put(
      OPCodeEnum.OP_line_num.ordinal(),
      new JSOpCode("line_num",5,0,0, OPCodeFormat.u32));
    opcode_info.put(
      OPCodeEnum.OP_push_minus1.ordinal(),
      new JSOpCode("push_minus1",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_0.ordinal(),
      new JSOpCode("push_0",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_1.ordinal(),
      new JSOpCode("push_1",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_2.ordinal(),
      new JSOpCode("push_2",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_3.ordinal(),
      new JSOpCode("push_3",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_4.ordinal(),
      new JSOpCode("push_4",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_5.ordinal(),
      new JSOpCode("push_5",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_6.ordinal(),
      new JSOpCode("push_6",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_7.ordinal(),
      new JSOpCode("push_7",1,0,1, OPCodeFormat.none_int));
    opcode_info.put(
      OPCodeEnum.OP_push_i8.ordinal(),
      new JSOpCode("push_i8",2,0,1, OPCodeFormat.i8));
    opcode_info.put(
      OPCodeEnum.OP_push_i16.ordinal(),
      new JSOpCode("push_i16",3,0,1, OPCodeFormat.i16));
    opcode_info.put(
      OPCodeEnum.OP_push_const8.ordinal(),
      new JSOpCode("push_const8",2,0,1, OPCodeFormat.const8));
    opcode_info.put(
      OPCodeEnum.OP_fclosure8.ordinal(),
      new JSOpCode("fclosure8",2,0,1, OPCodeFormat.const8));
    opcode_info.put(
      OPCodeEnum.OP_push_empty_string.ordinal(),
      new JSOpCode("push_empty_string",1,0,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_get_loc8.ordinal(),
      new JSOpCode("get_loc8",2,0,1, OPCodeFormat.loc8));
    opcode_info.put(
      OPCodeEnum.OP_put_loc8.ordinal(),
      new JSOpCode("put_loc8",2,1,0, OPCodeFormat.loc8));
    opcode_info.put(
      OPCodeEnum.OP_set_loc8.ordinal(),
      new JSOpCode("set_loc8",2,1,1, OPCodeFormat.loc8));
    opcode_info.put(
      OPCodeEnum.OP_get_loc0.ordinal(),
      new JSOpCode("get_loc0",1,0,1, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_get_loc1.ordinal(),
      new JSOpCode("get_loc1",1,0,1, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_get_loc2.ordinal(),
      new JSOpCode("get_loc2",1,0,1, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_get_loc3.ordinal(),
      new JSOpCode("get_loc3",1,0,1, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_put_loc0.ordinal(),
      new JSOpCode("put_loc0",1,1,0, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_put_loc1.ordinal(),
      new JSOpCode("put_loc1",1,1,0, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_put_loc2.ordinal(),
      new JSOpCode("put_loc2",1,1,0, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_put_loc3.ordinal(),
      new JSOpCode("put_loc3",1,1,0, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_set_loc0.ordinal(),
      new JSOpCode("set_loc0",1,1,1, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_set_loc1.ordinal(),
      new JSOpCode("set_loc1",1,1,1, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_set_loc2.ordinal(),
      new JSOpCode("set_loc2",1,1,1, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_set_loc3.ordinal(),
      new JSOpCode("set_loc3",1,1,1, OPCodeFormat.none_loc));
    opcode_info.put(
      OPCodeEnum.OP_get_arg0.ordinal(),
      new JSOpCode("get_arg0",1,0,1, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_get_arg1.ordinal(),
      new JSOpCode("get_arg1",1,0,1, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_get_arg2.ordinal(),
      new JSOpCode("get_arg2",1,0,1, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_get_arg3.ordinal(),
      new JSOpCode("get_arg3",1,0,1, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_put_arg0.ordinal(),
      new JSOpCode("put_arg0",1,1,0, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_put_arg1.ordinal(),
      new JSOpCode("put_arg1",1,1,0, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_put_arg2.ordinal(),
      new JSOpCode("put_arg2",1,1,0, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_put_arg3.ordinal(),
      new JSOpCode("put_arg3",1,1,0, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_set_arg0.ordinal(),
      new JSOpCode("set_arg0",1,1,1, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_set_arg1.ordinal(),
      new JSOpCode("set_arg1",1,1,1, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_set_arg2.ordinal(),
      new JSOpCode("set_arg2",1,1,1, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_set_arg3.ordinal(),
      new JSOpCode("set_arg3",1,1,1, OPCodeFormat.none_arg));
    opcode_info.put(
      OPCodeEnum.OP_get_var_ref0.ordinal(),
      new JSOpCode("get_var_ref0",1,0,1, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_get_var_ref1.ordinal(),
      new JSOpCode("get_var_ref1",1,0,1, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_get_var_ref2.ordinal(),
      new JSOpCode("get_var_ref2",1,0,1, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_get_var_ref3.ordinal(),
      new JSOpCode("get_var_ref3",1,0,1, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_put_var_ref0.ordinal(),
      new JSOpCode("put_var_ref0",1,1,0, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_put_var_ref1.ordinal(),
      new JSOpCode("put_var_ref1",1,1,0, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_put_var_ref2.ordinal(),
      new JSOpCode("put_var_ref2",1,1,0, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_put_var_ref3.ordinal(),
      new JSOpCode("put_var_ref3",1,1,0, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_set_var_ref0.ordinal(),
      new JSOpCode("set_var_ref0",1,1,1, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_set_var_ref1.ordinal(),
      new JSOpCode("set_var_ref1",1,1,1, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_set_var_ref2.ordinal(),
      new JSOpCode("set_var_ref2",1,1,1, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_set_var_ref3.ordinal(),
      new JSOpCode("set_var_ref3",1,1,1, OPCodeFormat.none_var_ref));
    opcode_info.put(
      OPCodeEnum.OP_get_length.ordinal(),
      new JSOpCode("get_length",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_if_false8.ordinal(),
      new JSOpCode("if_false8",2,1,0, OPCodeFormat.label8));
    opcode_info.put(
      OPCodeEnum.OP_if_true8.ordinal(),
      new JSOpCode("if_true8",2,1,0, OPCodeFormat.label8));
    opcode_info.put(
      OPCodeEnum.OP_goto8.ordinal(),
      new JSOpCode("goto8",2,0,0, OPCodeFormat.label8));
    opcode_info.put(
      OPCodeEnum.OP_goto16.ordinal(),
      new JSOpCode("goto16",3,0,0, OPCodeFormat.label16));
    opcode_info.put(
      OPCodeEnum.OP_call0.ordinal(),
      new JSOpCode("call0",1,1,1, OPCodeFormat.npopx));
    opcode_info.put(
      OPCodeEnum.OP_call1.ordinal(),
      new JSOpCode("call1",1,1,1, OPCodeFormat.npopx));
    opcode_info.put(
      OPCodeEnum.OP_call2.ordinal(),
      new JSOpCode("call2",1,1,1, OPCodeFormat.npopx));
    opcode_info.put(
      OPCodeEnum.OP_call3.ordinal(),
      new JSOpCode("call3",1,1,1, OPCodeFormat.npopx));
    opcode_info.put(
      OPCodeEnum.OP_is_undefined.ordinal(),
      new JSOpCode("is_undefined",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_is_null.ordinal(),
      new JSOpCode("is_null",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_typeof_is_undefined.ordinal(),
      new JSOpCode("typeof_is_undefined",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_typeof_is_function.ordinal(),
      new JSOpCode("typeof_is_function",1,1,1, OPCodeFormat.none));
    opcode_info.put(
      OPCodeEnum.OP_COUNT.ordinal(),
      new JSOpCode("COUNT",1,1,0, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_temp_start.ordinal(),
      new JSOpCode("temp_start",1,1,0, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP___dummy.ordinal(),
      new JSOpCode("__dummy",1,1,0, OPCodeFormat.atom));
    opcode_info.put(
      OPCodeEnum.OP_TEMP_END.ordinal(),
      new JSOpCode("TEMP_END",1,1,0, OPCodeFormat.atom));
 }
}