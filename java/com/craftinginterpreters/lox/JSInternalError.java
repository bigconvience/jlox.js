package com.craftinginterpreters.lox;

/**
 * @author benpeng.jiang
 * @title: JSInternalError
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/51:56 PM
 */
public class JSInternalError extends Error{
  public JSInternalError() {
  }

  public JSInternalError(String message) {
    super(message);
  }

  public JSInternalError(String message, Throwable cause) {
    super(message, cause);
  }

  public JSInternalError(Throwable cause) {
    super(cause);
  }

  public JSInternalError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
