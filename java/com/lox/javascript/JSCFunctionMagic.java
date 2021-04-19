package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: CFunctionMagic
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/189:21 PM
 */
public interface JSCFunctionMagic {
  JSValue JSCFunctionMagic(JSContext ctx, final JSValue this_val, int argc, final JSValue[] argv, int magic);
}
