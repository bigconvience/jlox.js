package com.lox.javascript;

import java.lang.reflect.Array;

/**
 * @author benpeng.jiang
 * @title: JSRuntimeUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/5/45:21 PM
 */
public class JSRuntimeUtils {
    static <T> T[] js_realloc_rt(JSRuntime rt, Class<T> componentType, T[] ptr,  int size)
  {
    T[] ret = (T[])Array.newInstance(componentType, size);

    int start = ptr == null ? 0 : ptr.length;
    if (start > 0) {
      System.arraycopy(ptr, 0, ret, 0, ptr.length);
    }
    int delta = size - start;

    for (int i = 0; i < delta; i++) {
      try {
        ret[start + i] = componentType.newInstance();
      }catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return ret;
  }
}
