package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSClassShortDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/98:47 PM
 */
public class JSClassShortDef {
  final JSAtom class_name;
  final JSClassFinalizer finalizer;
  final JSClassGCMark gc_mark;

  public JSClassShortDef(JSAtom class_name, JSClassFinalizer finalizer, JSClassGCMark gc_mark) {
    this.class_name = class_name;
    this.finalizer = finalizer;
    this.gc_mark = gc_mark;
  }

}
