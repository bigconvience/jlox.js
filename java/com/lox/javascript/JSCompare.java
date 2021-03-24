package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSBinaryOperate
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/206:34 PM
 */
public class JSCompare {
  static int js_eq_slow(JSContext ctx, JSValue[] stack_buf,
                        int sp,
                                              boolean is_neq)
  {
    JSValue op1, op2, ret;
    int res = 0 ;
    JSTag tag1, tag2;

    op1 = stack_buf[sp-2];
    op2 = stack_buf[sp-1];
    while (true) {
      tag1 = JSValue.JS_VALUE_GET_NORM_TAG(op1);
      tag2 = JSValue.JS_VALUE_GET_NORM_TAG(op2);
      if (JSTag.tag_is_number(tag1) && JSTag.tag_is_number(tag2)) {
        if (tag1 == JSTag.JS_TAG_INT && tag2 == JSTag.JS_TAG_INT) {
          res = JSValue.JS_VALUE_GET_INT(op1) == JSValue.JS_VALUE_GET_INT(op2) ? 1 : 0;
          break;
        } else if ((tag1 == JSTag.JS_TAG_FLOAT64 &&
          (tag2 == JSTag.JS_TAG_INT || tag2 == JSTag.JS_TAG_FLOAT64)) ||
          (tag2 == JSTag.JS_TAG_FLOAT64 &&
            (tag1 == JSTag.JS_TAG_INT || tag1 == JSTag.JS_TAG_FLOAT64))) {
          double d1, d2;
          if (tag1 == JSTag.JS_TAG_FLOAT64) {
            d1 = JSValue.JS_VALUE_GET_FLOAT64(op1);
          } else {
            d1 = JSValue.JS_VALUE_GET_INT(op1);
          }
          if (tag2 == JSTag.JS_TAG_FLOAT64) {
            d2 = JSValue.JS_VALUE_GET_FLOAT64(op2);
          } else {
            d2 = JSValue.JS_VALUE_GET_INT(op2);
          }
          res = (d1 == d2) ? 1 : 0;
          break;
        } else if (tag1 == JSTag.JS_TAG_BIG_DECIMAL || tag2 == JSTag.JS_TAG_BIG_DECIMAL) {
//        res = ctx->rt->bigdecimal_ops.compare(ctx, OP_eq, op1, op2);
          if (res < 0) {
            on_exception(stack_buf, sp);
            return -1;
          }
        } else if (tag1 == JSTag.JS_TAG_BIG_FLOAT || tag2 == JSTag.JS_TAG_BIG_FLOAT) {
//        res = ctx->rt->bigfloat_ops.compare(ctx, OP_eq, op1, op2);
          if (res < 0) {
            on_exception(stack_buf, sp);
            return -1;
          }
        } else {
//        res = ctx->rt->bigint_ops.compare(ctx, OP_eq, op1, op2);
          if (res < 0) {
            on_exception(stack_buf, sp);
            return -1;
          }
        }
      } else if (tag1 == tag2) {
        if (tag1 == JSTag.JS_TAG_OBJECT) {
          /* try the fallback operator */
//        res = js_call_binary_op_fallback(ctx, ret, op1, op2,
//          is_neq ? OP_neq : OP_eq,
//          0, HINT_NONE);
//        if (res != 0) {
//          if (res < 0) {
//                    on_exception(stack_buf, sp);return -1;
//          } else {
//            stack_buf[sp-2] = ret;
//            return 0;
//          }
//        }
        }
        res = js_strict_eq2(ctx, op1, op2, JSStrictEqModeEnum.JS_EQ_STRICT) ? 1 : 0;
        break;
      } else if ((tag1 == JSTag.JS_TAG_NULL && tag2 == JSTag.JS_TAG_UNDEFINED) ||
        (tag2 == JSTag.JS_TAG_NULL && tag1 == JSTag.JS_TAG_UNDEFINED)) {
        res = 1;
        break;
//    }// else if ((tag1 == JS_TAG_STRING && tag_is_number(tag2)) ||
//      (tag2 == JS_TAG_STRING && tag_is_number(tag1))) {
//
//      if ((tag1 == JS_TAG_BIG_INT || tag2 == JS_TAG_BIG_INT) &&
//        !is_math_mode(ctx)) {
//        if (tag1 == JS_TAG_STRING) {
//          op1 = JS_StringToBigInt(ctx, op1);
//          if (JS_VALUE_GET_TAG(op1) != JS_TAG_BIG_INT)
//                    goto invalid_bigint_string;
//        }
//        if (tag2 == JS_TAG_STRING) {
//          op2 = JS_StringToBigInt(ctx, op2);
//          if (JS_VALUE_GET_TAG(op2) != JS_TAG_BIG_INT) {
//            invalid_bigint_string:
//            res = 0;
//            on_done(ctx, res, is_neq, stack_buf, sp);
//            return 0;
//          }
//        }
//      } else {
//        op1 = JS_ToNumericFree(ctx, op1);
//        if (JS_IsException(op1)) {
//          JS_FreeValue(ctx, op2);
//                on_exception(stack_buf, sp);return -1;
//        }
//        op2 = JS_ToNumericFree(ctx, op2);
//        if (JS_IsException(op2)) {
//          JS_FreeValue(ctx, op1);
//                on_exception(stack_buf, sp);return -1;
//        }
//      }
//      res = js_strict_eq(ctx, op1, op2);
      } else if (tag1 == JSTag.JS_TAG_BOOL) {
        op1 = JSValue.JS_NewInt32(ctx, JSValue.JS_VALUE_GET_INT(op1));
        continue;
      } else if (tag2 == JSTag.JS_TAG_BOOL) {
        op2 = JSValue.JS_NewInt32(ctx, JSValue.JS_VALUE_GET_INT(op2));
        continue;
      } //else if ((tag1 == JS_TAG_OBJECT &&
//      (tag_is_number(tag2) || tag2 == JS_TAG_STRING || tag2 == JS_TAG_SYMBOL)) ||
//      (tag2 == JS_TAG_OBJECT &&
//        (tag_is_number(tag1) || tag1 == JS_TAG_STRING || tag1 == JS_TAG_SYMBOL))) {
//
//      /* try the fallback operator */
//      res = js_call_binary_op_fallback(ctx, &ret, op1, op2,
//        is_neq ? OP_neq : OP_eq,
//        0, HINT_NONE);
//      if (res != 0) {
//        JS_FreeValue(ctx, op1);
//        JS_FreeValue(ctx, op2);
//        if (res < 0) {
//                on_exception(stack_buf, sp);return -1;
//        } else {
//          sp[-2] = ret;
//          return 0;
//        }
//      }
//
//      op1 = JS_ToPrimitiveFree(ctx, op1, HINT_NONE);
//      if (JS_IsException(op1)) {
//        JS_FreeValue(ctx, op2);
//            on_exception(stack_buf, sp);return -1;
//      }
//      op2 = JS_ToPrimitiveFree(ctx, op2, HINT_NONE);
//      if (JS_IsException(op2)) {
//        JS_FreeValue(ctx, op1);
//            on_exception(stack_buf, sp);
//      }
//        goto redo;
//    } else {
//      /* IsHTMLDDA object is equivalent to undefined for '==' and '!=' */
//      if ((JS_IsHTMLDDA(ctx, op1) &&
//        (tag2 == JS_TAG_NULL || tag2 == JS_TAG_UNDEFINED)) ||
//        (JS_IsHTMLDDA(ctx, op2) &&
//          (tag1 == JS_TAG_NULL || tag1 == JS_TAG_UNDEFINED))) {
//        res = 1;
//      } else {
//        res = 0;
//      }
//
//    }
    }
    on_done(ctx, res, is_neq, stack_buf, sp);
    return 0;
  }
  
  private static void on_done(JSContext ctx, int res, boolean is_neq, JSValue[] stack_buf, int sp) {
    stack_buf[sp -2] = JSValue.JS_NewBool(ctx, res ^ (is_neq ? 1 : 0));
  }
  
  private static void on_exception(JSValue[] stack_buf, int sp) {
    stack_buf[sp -2] = JSValue.JS_UNDEFINED;
    stack_buf[sp -1] = JSValue.JS_UNDEFINED;
  }
  /* XXX: Should take final JSValue arguments */
  static boolean js_strict_eq2(JSContext ctx, JSValue op1, JSValue op2,
                            JSStrictEqModeEnum eq_mode)
  {
    boolean res;
    JSTag tag1, tag2;
    double d1, d2;

    tag1 = JSValue.JS_VALUE_GET_NORM_TAG(op1);
    tag2 = JSValue.JS_VALUE_GET_NORM_TAG(op2);
    switch(tag1) {
      case JS_TAG_BOOL:
        if (tag1 != tag2) {
          res = false;
        } else {
          res = JSValue.JS_VALUE_GET_INT(op1) == JSValue.JS_VALUE_GET_INT(op2);

        }
        break;
      case JS_TAG_NULL:
      case JS_TAG_UNDEFINED:
        res = (tag1 == tag2);
        break;
      case JS_TAG_STRING:
      {
        JSString p1, p2;
        if (tag1 != tag2) {
          res = false;
        } else {
          p1 = JSValue.JS_VALUE_GET_STRING(op1);
          p2 = JSValue.JS_VALUE_GET_STRING(op2);
          res = (JSString.js_string_compare(ctx, p1, p2) == 0);
        }
      }
      break;
      case JS_TAG_SYMBOL:
      {
        JSAtom p1, p2;
        if (tag1 != tag2) {
          res = false;
        } else {
          p1 = (JSAtom) JSValue.JS_VALUE_GET_PTR(op1);
          p2 = (JSAtom) JSValue.JS_VALUE_GET_PTR(op2);
          res = (p1 == p2);
        }
      }
      break;
      case JS_TAG_OBJECT:
        if (tag1 != tag2)
          res = false;
        else
          res = JSValue.JS_VALUE_GET_OBJ(op1) == JSValue.JS_VALUE_GET_OBJ(op2);
        break;
      case JS_TAG_INT:
        d1 = JSValue.JS_VALUE_GET_INT(op1);
        if (tag2 == JSTag.JS_TAG_INT) {
          d2 = JSValue.JS_VALUE_GET_INT(op2);
          res = number_test(eq_mode, d1, d2);
        } else if (tag2 == JSTag.JS_TAG_FLOAT64) {
          d2 = JSValue.JS_VALUE_GET_FLOAT64(op2);
          res = number_test(eq_mode, d1, d2);
        } else {
          res = false;
        }
        break;
      case JS_TAG_FLOAT64:
        d1 = JSValue.JS_VALUE_GET_FLOAT64(op1);
        if (tag2 == JSTag.JS_TAG_FLOAT64) {
          d2 = JSValue.JS_VALUE_GET_FLOAT64(op2);
        } else if (tag2 == JSTag.JS_TAG_INT) {
          d2 = JSValue.JS_VALUE_GET_INT(op2);
        } else {
          res = false;
          break;
        }
        res = number_test(eq_mode, d1, d2);
        break;

      default:
        res = false;
        break;
    }

    return res;
  }

  private static boolean number_test(JSStrictEqModeEnum eq_mode,
                                     double d1, double d2) {
    boolean res;
    if (eq_mode.ordinal() >= JSStrictEqModeEnum.JS_EQ_SAME_VALUE.ordinal()) {
      /* NaN is not always normalized, so this test is necessary */
      if (math.isnan(d1) || math.isnan(d2)) {
        res = math.isnan(d1) == math.isnan(d2);
      } else if (eq_mode == JSStrictEqModeEnum.JS_EQ_SAME_VALUE_ZERO) {
        res = (d1 == d2); /* +0 == -0 */
      } else {
        res =( d1 == d2); /* +0 != -0 */
      }
    } else {
      res = (d1 == d2); /* if NaN return false and +0 == -0 */
    }
    return res;
  }

  static boolean js_strict_eq(JSContext ctx, JSValue op1, JSValue op2)
  {
    return js_strict_eq2(ctx, op1, op2, JSStrictEqModeEnum.JS_EQ_STRICT);
  }

  static boolean js_same_value(JSContext ctx, final JSValue op1, final JSValue op2)
  {
    return js_strict_eq2(ctx,
      op1, op2,
      JSStrictEqModeEnum.JS_EQ_SAME_VALUE);
  }

  static boolean js_same_value_zero(JSContext ctx, final JSValue op1, final JSValue op2)
  {
    return js_strict_eq2(ctx,
      op1, op2,
      JSStrictEqModeEnum.JS_EQ_SAME_VALUE_ZERO);
  }

  static  int js_strict_eq_slow(JSContext ctx, JSValue[] stack_buf,
                                int sp,
                                         boolean is_neq)
  {
    boolean res;
    res = js_strict_eq(ctx, stack_buf[sp-2], stack_buf[sp-1]);
    stack_buf[sp-2] = JSValue.JS_NewBool(ctx, res ^ is_neq);
    return 0;
  }
}
