package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.JSStrictEqModeEnum.*;
import static com.craftinginterpreters.lox.JSString.js_string_compare;
import static com.craftinginterpreters.lox.JSTag.*;
import static com.craftinginterpreters.lox.JSValue.*;
import static com.craftinginterpreters.lox.math.isnan;

/**
 * @author benpeng.jiang
 * @title: JSBinaryOperate
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/206:34 PM
 */
public class Compare {
  /* XXX: Should take final JSValue arguments */
  static boolean js_strict_eq2(JSContext ctx, JSValue op1, JSValue op2,
                            JSStrictEqModeEnum eq_mode)
  {
    boolean res;
    JSTag tag1, tag2;
    double d1, d2;

    tag1 = JS_VALUE_GET_NORM_TAG(op1);
    tag2 = JS_VALUE_GET_NORM_TAG(op2);
    switch(tag1) {
      case JS_TAG_BOOL:
        if (tag1 != tag2) {
          res = false;
        } else {
          res = JS_VALUE_GET_INT(op1) == JS_VALUE_GET_INT(op2);

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
          p1 = JS_VALUE_GET_STRING(op1);
          p2 = JS_VALUE_GET_STRING(op2);
          res = (js_string_compare(ctx, p1, p2) == 0);
        }
      }
      break;
      case JS_TAG_SYMBOL:
      {
        JSAtom p1, p2;
        if (tag1 != tag2) {
          res = false;
        } else {
          p1 = (JSAtom) JS_VALUE_GET_PTR(op1);
          p2 = (JSAtom) JS_VALUE_GET_PTR(op2);
          res = (p1 == p2);
        }
      }
      break;
      case JS_TAG_OBJECT:
        if (tag1 != tag2)
          res = false;
        else
          res = JS_VALUE_GET_OBJ(op1) == JS_VALUE_GET_OBJ(op2);
        break;
      case JS_TAG_INT:
        d1 = JS_VALUE_GET_INT(op1);
        if (tag2 == JS_TAG_INT) {
          d2 = JS_VALUE_GET_INT(op2);
          res = number_test(eq_mode, d1, d2);
        } else if (tag2 == JS_TAG_FLOAT64) {
          d2 = JS_VALUE_GET_FLOAT64(op2);
          res = number_test(eq_mode, d1, d2);
        } else {
          res = false;
        }
        break;
      case JS_TAG_FLOAT64:
        d1 = JS_VALUE_GET_FLOAT64(op1);
        if (tag2 == JS_TAG_FLOAT64) {
          d2 = JS_VALUE_GET_FLOAT64(op2);
        } else if (tag2 == JS_TAG_INT) {
          d2 = JS_VALUE_GET_INT(op2);
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
    if (eq_mode.ordinal() >= JS_EQ_SAME_VALUE.ordinal()) {
      /* NaN is not always normalized, so this test is necessary */
      if (isnan(d1) || isnan(d2)) {
        res = isnan(d1) == isnan(d2);
      } else if (eq_mode == JS_EQ_SAME_VALUE_ZERO) {
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
    return js_strict_eq2(ctx, op1, op2, JS_EQ_STRICT);
  }

  static boolean js_same_value(JSContext ctx, final JSValue op1, final JSValue op2)
  {
    return js_strict_eq2(ctx,
      op1, op2,
      JS_EQ_SAME_VALUE);
  }

  static boolean js_same_value_zero(JSContext ctx, final JSValue op1, final JSValue op2)
  {
    return js_strict_eq2(ctx,
      op1, op2,
      JS_EQ_SAME_VALUE_ZERO);
  }

  static  int js_strict_eq_slow(JSContext ctx, JSValue[] stack_buf,
                                int sp,
                                         boolean is_neq)
  {
    boolean res;
    res = js_strict_eq(ctx, stack_buf[sp-2], stack_buf[sp-1]);
    stack_buf[sp-2] = JS_NewBool(ctx, res ^ is_neq);
    return 0;
  }
}
