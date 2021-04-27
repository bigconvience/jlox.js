package com.lox.javascript;

/**
 * @author benpeng.jiang
 * @title: JSTypedArray
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/4/191:56 PM
 */
public class JSTypedArray {
  JSTypedArray link; /* link to arraybuffer */
  JSObject obj; /* back pointer to the TypedArray/DataView object */
  JSObject buffer; /* based array buffer */
  int offset; /* offset in the array buffer */
  int length; /* length in the array buffer */
}
