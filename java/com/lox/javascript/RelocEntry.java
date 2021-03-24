package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: RelocEntry
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/188:19 AM
 */
public class RelocEntry {
  RelocEntry next;
  int addr; /* address to patch */
  int size;   /* address size: 1, 2 or 4 bytes */
}
