package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JS_PROP
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/910:45 PM
 */
public class JS_PROP {
  /* flags for object properties */
  static final int JS_PROP_CONFIGURABLE = (1 << 0);
  static final int JS_PROP_WRITABLE = (1 << 1);
  static final int JS_PROP_ENUMERABLE = (1 << 2);
  static final int JS_PROP_C_W_E = (JS_PROP_CONFIGURABLE | JS_PROP_WRITABLE | JS_PROP_ENUMERABLE);
  static final int JS_PROP_LENGTH = (1 << 3); /* used internally in Arrays */
  static final int JS_PROP_TMASK = (3 << 4); /* mask for NORMAL, GETSET, VARREF, AUTOINIT */
  static final int JS_PROP_NORMAL = (0 << 4);
  public static final int JS_PROP_GETSET = (1 << 4);
  static final int JS_PROP_VARREF = (2 << 4); /* used internally */
  static final int JS_PROP_AUTOINIT = (3 << 4); /* used internally */
  /* flags for JS_DefineProperty */
  static final int JS_PROP_HAS_SHIFT = 8;
  static final int JS_PROP_HAS_CONFIGURABLE = (1 << 8);
  static final int JS_PROP_HAS_WRITABLE = (1 << 9);
  static final int JS_PROP_HAS_ENUMERABLE = (1 << 10);
  static final int JS_PROP_HAS_GET = (1 << 11);
  static final int JS_PROP_HAS_SET = (1 << 12);
  static final int JS_PROP_HAS_VALUE = (1 << 13);

  /* throw an exception if false would be returned
   (JS_DefineProperty/JS_SetProperty) */
  static final int JS_PROP_THROW = (1 << 14);
  /* throw an exception if false would be returned in strict mode
     (JS_SetProperty) */
  static final int JS_PROP_THROW_STRICT = (1 << 15);

  static final int JS_PROP_NO_ADD = (1 << 16); /* internal use */
  static final int JS_PROP_NO_EXOTIC = (1 << 17); /* internal use */


  static int get_prop_flags(int flags, int def_flags)
  {
    int mask;
    mask = (flags >> JS_PROP_HAS_SHIFT) & JS_PROP_C_W_E;
    return (flags & mask) | (def_flags & ~mask);
  }
}
