package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: quickjs指令数据格式定义
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/2411:27 PM
 */
public enum OPCodeFormat {
  none,
  none_int,
  none_loc,
  none_arg,
  none_var_ref,
  u8,
  i8,
  loc8,
  const8,
  label8,
  u16,
  i16,
  label16,
  npop,
  npopx,
  npop_u16,
  loc,
  arg,
  var_ref,
  u32,
  i32,
  Const,
  label,
  atom,
  atom_u8,
  atom_u16,
  atom_label_u8,
  atom_label_u16,
  label_u16,
}
