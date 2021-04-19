package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: CFunctionData
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/189:21 PM
 */
public interface JSCFunctionData {
  JSValue JSCFunctionData(JSContext ctx, final JSValue this_val, int argc, final JSValue[] argv, int magic, JSValue func_data);

}
