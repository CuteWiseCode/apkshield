package com.ck.plugin.ext;


/**
 * @author ck
 */
public class AndrolibException extends Exception {
  public AndrolibException() {
  }

  public AndrolibException(String message) {
    super(message);
  }

  public AndrolibException(String message, Throwable cause) {
    super(message, cause);
  }

  public AndrolibException(Throwable cause) {
    super(cause);
  }
}
