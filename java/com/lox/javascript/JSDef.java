package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/5/209:14 AM
 */
public class JSDef {
  static final int JS_DEF_CFUNC          = 0;
    static final int JS_DEF_CGETSET        =1;
    static final int JS_DEF_CGETSET_MAGIC  =2;
    static final int JS_DEF_PROP_STRING    =3;
    static final int JS_DEF_PROP_INT32     =4;
    static final int JS_DEF_PROP_INT64     =5;
    static final int JS_DEF_PROP_DOUBLE    =6;
    static final int JS_DEF_PROP_UNDEFINED =7;
    static final int JS_DEF_OBJECT         =8;
    static final int JS_DEF_ALIAS          =9;
}
