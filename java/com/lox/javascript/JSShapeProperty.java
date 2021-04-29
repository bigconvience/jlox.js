package com.lox.javascript;

import static com.lox.javascript.JSShape.*;
import static java.lang.Boolean.*;

/**
 * @author benpeng.jiang
 * @title: JSShapeProperty
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/91:51 PM
 */
public class JSShapeProperty {
  int flags;
  JSAtom atom;
  int hash_next;

  static int js_shape_prepare_update(JSContext ctx, JSObject p,
                                     JSShapeProperty pprs)
  {
    JSShape sh;
//    uint32_t idx = 0;    /* prevent warning */

    sh = p.shape;
    if (sh.is_hashed) {
      if (sh.header.ref_count != 1) {
        if (pprs != null) {
//          idx = pprs - get_shape_prop(sh);
        }
        /* clone the shape (the resulting one is no longer hashed) */
        sh = js_clone_shape(ctx, sh);
        if (sh == null)
          return -1;
        js_free_shape(ctx.rt, p.shape);
        p.shape = sh;
        if (pprs != null) {
//                pprs.val = get_shape_prop(sh)[idx];
        }
      } else {
        js_shape_hash_unlink(ctx.rt, sh);
        sh.is_hashed = FALSE;
      }
    }
    return 0;
  }
}
