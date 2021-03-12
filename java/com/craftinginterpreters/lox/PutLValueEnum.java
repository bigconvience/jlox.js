package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: PutLValueEnum
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/1211:39 AM
 */
public enum PutLValueEnum {
  PUT_LVALUE_NOKEEP, /* [depth] v -> */
  PUT_LVALUE_NOKEEP_DEPTH, /* [depth] v -> , keep depth (currently
                                just disable optimizations) */
  PUT_LVALUE_KEEP_TOP,  /* [depth] v -> v */
  PUT_LVALUE_KEEP_SECOND, /* [depth] v0 v -> v0 */
  PUT_LVALUE_NOKEEP_BOTTOM, /* v [depth] -> */
}
