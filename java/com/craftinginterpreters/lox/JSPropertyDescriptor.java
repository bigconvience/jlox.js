package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSPropertyDescriptor
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/1011:16 PM
 */
public class JSPropertyDescriptor {
  int flags;
  JSValue value;
  JSValue getter;
  JSValue setter;
}
