package com.lox.javascript;

import java.util.ArrayList;
import java.util.List;

/**
 * @author benpeng.jiang
 * @title: JSShape
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/42:22 PM
 */
public class JSShape {
  final JSRefCountHeader header = new JSRefCountHeader();

  /* true if the shape is inserted in the shape hash table. If not,
   JSShape.hash is not valid */
  boolean is_hashed;
  /* If true, the shape may have small array index properties 'n' with 0
     <= n <= 2^31-1. If false, the shape is guaranteed not to have
     small array index properties */
  boolean has_small_array_index;
  int hash; /* current hash value */
  int prop_hash_mask;
  int prop_size; /* allocated properties */
  int prop_count; /* include deleted properties */
  int deleted_prop_count;
  JSObject proto;
  JSShapeProperty[] prop; /* prop_size elements */
  int[] hash_array;

  public JSShape() {

  }

  static JSShape js_dup_shape(JSShape sh)
  {
    sh.header.ref_count++;
    return sh;
  }

  static JSShapeProperty[] get_shape_prop(JSShape sh)
  {
    return sh.prop;
  }

  static int[] prop_hash_end(JSShape sh) {
    return sh.hash_array;
  }

  static void js_shape_hash_unlink(JSRuntime rt, JSShape sh)
  {

  }

  static void js_free_shape(JSRuntime rt, JSShape sh)
  {
    if ((--sh.header.ref_count <= 0)) {
      js_free_shape0(rt, sh);
    }
  }

  static void js_free_shape0(JSRuntime rt, JSShape sh)
  {

  }

  static JSShape js_clone_shape(JSContext ctx, JSShape sh1)
  {
    return sh1;
  }
}
