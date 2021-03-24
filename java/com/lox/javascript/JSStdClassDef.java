package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSStdClassDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/98:56 PM
 */
public class JSStdClassDef {

  static final JSClassShortDef[] js_std_class_def;

  static {
    js_std_class_def = new JSClassShortDef[]{
      new JSClassShortDef(JSAtomEnum.JS_ATOM_Object.toJSAtom(), null, null)
    };

  }
}
