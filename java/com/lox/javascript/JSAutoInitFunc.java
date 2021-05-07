package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSAutoInitFunc
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/5/610:37 PM
 */
public abstract class JSAutoInitFunc {
  abstract int JSAutoInitFunc(JSContext ctx, JSObject p, JSAtom atom, Object opaque);

  static int js_autoinit_get_id(JSProperty pr)
  {
    return pr.u.init.realm_and_id & 3;
  }


  static JSAutoInitFunc js_instantiate_prototype = new JSAutoInitFunc() {
    @Override
    int JSAutoInitFunc(JSContext ctx, JSObject p, JSAtom atom, Object opaque) {
      return 0;
    }
  };

  static JSAutoInitFunc js_module_ns_autoinit = new JSAutoInitFunc() {
    @Override
    int JSAutoInitFunc(JSContext ctx, JSObject p, JSAtom atom, Object opaque) {
      return 0;
    }
  };

  static JSAutoInitFunc JS_InstantiateFunctionListItem = new JSAutoInitFunc() {
    @Override
    int JSAutoInitFunc(JSContext ctx, JSObject p, JSAtom atom, Object opaque) {
      return 0;
    }
  };

  static JSAutoInitFunc[] js_autoinit_func_table = {
    js_instantiate_prototype, /* JS_AUTOINIT_ID_PROTOTYPE */
    js_module_ns_autoinit, /* JS_AUTOINIT_ID_MODULE_NS */
    JS_InstantiateFunctionListItem, /* JS_AUTOINIT_ID_PROP */
  };
}
