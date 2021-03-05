package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: LabelSlot
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/52:26 PM
 */
public class LabelSlot {
  int ref_count;
  int pos;    /* phase 1 address, -1 means not resolved yet */
  int pos2;   /* phase 2 address, -1 means not resolved yet */
  int addr;   /* phase 3 address, -1 means not resolved yet */
}
