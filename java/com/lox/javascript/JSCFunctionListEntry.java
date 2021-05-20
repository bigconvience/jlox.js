package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSCFunctionListEntry
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/5/209:08 AM
 */
public class JSCFunctionListEntry {
  char[] name;
  int prop_flags;
  int def_type;
  int magic;
  U u;

  public JSCFunctionListEntry() {
    u = new U();
  }

  class U {
    Func func;
    Getset getset;
    Alias alias;
    Prop_list prop_list;
    char[] str;
    int i32;
    long i64;
    double f64;


    class Func {
      int length; /* XXX: should move outside union */
      int cproto; /* XXX: should move outside union */
      JSCFunctionType cfunc;
    }

    class Getset {
      JSCFunctionType get;
      JSCFunctionType set;
    }

    class Alias {
      char[] name;
      int base;
    }

    class Prop_list {
      JSCFunctionListEntry[] tab;
      int len;
    }
  }
}
