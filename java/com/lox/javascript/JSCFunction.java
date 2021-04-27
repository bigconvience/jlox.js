package com.lox.javascript;

import static com.lox.javascript.JSAtom.*;
import static com.lox.javascript.JSAtomEnum.*;
import static com.lox.javascript.JSCFunctionEnum.*;
import static com.lox.javascript.JSClassID.*;
import static com.lox.javascript.JSContext.*;
import static com.lox.javascript.JSProperty.*;
import static com.lox.javascript.JSValue.*;
import static com.lox.javascript.JS_PROP.*;

/**
 * @author benpeng.jiang
 * @title: CFunction
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/189:20 PM
 */
public abstract class JSCFunction {
   public static JSCFunction js_print = new JSCFunction() {
      @Override
      public JSValue JSCFunction(JSContext ctx, JSValue this_val, int argc, JSValue[] argv) {
         return null;
      }
   };

   public static JSCFunction js_loadScript = new JSCFunction() {
      @Override
      public JSValue JSCFunction(JSContext ctx, JSValue this_val, int argc, JSValue[] argv) {
         return null;
      }
   };

   public static JSCFunction js_function_proto = new JSCFunction() {
      @Override
      JSValue JSCFunction(JSContext ctx, JSValue this_val, int argc, JSValue[] argv) {
         return JS_UNDEFINED;
      }
   };


   abstract JSValue JSCFunction(JSContext ctx, final JSValue this_val, int argc, final JSValue[] argv);

   static JSValue JS_NewCFunction(JSContext ctx, JSCFunction func, final String name,
                                  int length)
   {
      return JS_NewCFunction(ctx, func, name.toCharArray(), length);
   }

   static JSValue JS_NewCFunction(JSContext ctx, JSCFunction func, final char[] name,
                                         int length)
   {
      return JS_NewCFunction2(ctx, func, name, length, JS_CFUNC_generic, 0);
   }

   static JSValue JS_NewCFunction2(JSContext ctx, JSCFunction func,
                            final char[] name,
                            int length, JSCFunctionEnum cproto, int magic)
   {
      return JS_NewCFunction3(ctx, func, name, length, cproto, magic,
        ctx.function_proto);
   }

   static JSValue JS_NewCFunction3(JSContext ctx, JSCFunction func, String name,
                                   int length, JSCFunctionEnum cproto, int magic,
                                   final JSValue proto_val)
   {
      return JS_NewCFunction3(ctx, func, name.toCharArray(), length, cproto, magic, proto_val);
   }

   static JSValue JS_NewCFunction3(JSContext ctx, JSCFunction func, char[] name,
                                   int length, JSCFunctionEnum cproto, int magic,
                                   final JSValue proto_val)
   {
      JSValue func_obj;
      JSObject p;
      JSAtom name_atom;

      func_obj = JS_NewObjectProtoClass(ctx, proto_val, JS_CLASS_C_FUNCTION);
      if (JS_IsException(func_obj))
         return func_obj;
      p = JS_VALUE_GET_OBJ(func_obj);
      p.u.cfunc.realm = JS_DupContext(ctx);
      p.u.cfunc.c_function.generic = func;
      p.u.cfunc.length = length;
      p.u.cfunc.cproto = cproto;
      p.u.cfunc.magic = magic;
      p.is_constructor = (cproto == JS_CFUNC_constructor ||
        cproto == JS_CFUNC_constructor_magic ||
        cproto == JS_CFUNC_constructor_or_func ||
        cproto == JS_CFUNC_constructor_or_func_magic);
      if (name != null)
         name = new char[0];
      name_atom = JS_NewAtom(ctx, name);
      js_function_set_properties(ctx, func_obj, name_atom, length);
      JS_FreeAtom(ctx, name_atom);
      return func_obj;
   }

   static void js_function_set_properties(JSContext ctx, final JSValue func_obj,
                                          JSAtom name, int len)
   {
      /* ES6 feature non compatible with ES5.1: length is configurable */
      JS_DefinePropertyValue(ctx, func_obj, JS_ATOM_length.toJSAtom(), JS_NewInt32(ctx, len),
        JS_PROP_CONFIGURABLE);
      JS_DefinePropertyValue(ctx, func_obj, JS_ATOM_name.toJSAtom(),
        JS_AtomToString(ctx, name), JS_PROP_CONFIGURABLE);
   }
}
