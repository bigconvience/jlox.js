package com.lox.javascript;

import static com.lox.javascript.JSGCObjectTypeEnum.JS_GC_OBJ_TYPE_VAR_REF;

/**
 * @author benpeng.jiang
 * @title: JSGCObjectHeader
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/241:44 AM
 */
public class JSGCObjectHeader {
  int ref_count; /* must come first, 32-bit */
  JSGCObjectTypeEnum gc_obj_type  = JS_GC_OBJ_TYPE_VAR_REF;
  int mark = 4; /* used by the GC */
  int dummy1; /* not used by the GC */
  int dummy2; /* not used by the GC */
  JSGCObjectHeader link;
}
