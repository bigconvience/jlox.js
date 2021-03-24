package com.lox.test;

import static com.lox.clibrary.string_h.strlen;
import static com.lox.clibrary.string_h.strncmp;

/**
 * @author benpeng.jiang
 * @title: TextUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/228:22 PM
 */
public class TextUtils {
  /**
   * Returns true if the string is null or 0-length.
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  public static boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }

  public static boolean has_prefix(final char[] str, final char[] prefix) {
    return strncmp(str, prefix, strlen(prefix)) == 0;
  }

  public static String skip_prefix(final String str, final String prefix) {
    if (str.startsWith(prefix)) {
      return str.substring(prefix.length());
    }
    return str;
  }
}
