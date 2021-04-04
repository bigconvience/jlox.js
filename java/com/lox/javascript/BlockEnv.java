package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: BlockEnv
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/210:20 AM
 */
public class BlockEnv {
  BlockEnv prev;
  JSAtom label_name; /* JS_ATOM_NULL if none */
  int label_break; /* -1 if none */
  int label_cont; /* -1 if none */
  int drop_count; /* number of stack elements to drop */
  int label_finally; /* -1 if none */
  int scope_level;
  int has_iterator;
}
