package com.craftinginterpreters.lox;


/**
 * @author benpeng.jiang
 * @title: string
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/229:03 PM
 */
public class string_h {
  public static char[] strcpy(final char[] destination, final char[] source) {
    System.arraycopy(source, 0, destination, 0,
      Math.min(source.length, destination.length));
    return destination;
  }


  public static int strlen(char[] a) {
    return a == null ? 0 : a.length;
  }

  public static int strcmp(char v1[], char v2[]) {
    int len1 = v1.length;
    int len2 = v2.length;
    int lim = Math.min(len1, len2);
    int k = 0;
    while (k < lim) {
      char c1 = v1[k];
      char c2 = v2[k];
      if (c1 != c2) {
        return c1 - c2;
      }
      k++;
    }
    return len1 - len2;
  }

  public static int strncmp(final char[] v1, final char[] v2, int num) {
    int len1 = v1.length;
    int len2 = v2.length;
    int lim = Math.min(len1, len2);
    lim = Math.min(lim, num);
    int k = 0;
    while (k < lim) {
      char c1 = v1[k];
      char c2 = v2[k];
      if (c1 != c2) {
        return c1 - c2;
      }
      k++;
    }
    return len1 - len2;
  }


  // https://blog.csdn.net/jerry9032/article/details/6551900#:~:text=strcspn%20%28%29%20%E6%98%AF%E7%94%A8%E6%9D%A5%E9%A1%BA%E5%BA%8F%E5%9C%A8%E5%AD%97%E7%AC%A6%E4%B8%B2%20s1%20%E4%B8%AD%E6%90%9C%E5%AF%BB%E4%B8%8E%E5%AD%97%E7%AC%A6%E4%B8%B2%20s2%20%E4%B8%AD%E5%AD%97%E7%AC%A6%E7%9A%84%E7%AC%AC%E4%B8%80%E4%B8%AA%E7%9B%B8%E5%90%8C%E5%AD%97%E7%AC%A6%EF%BC%8C%E8%BF%94%E5%9B%9E%E8%BF%99%E4%B8%AA%E5%AD%97%E7%AC%A6%E5%9C%A8%20s1,%2Apstr%2C%20const%20char%20%2AstrCharset%29%20%E5%8F%A6%E5%A4%96%E6%9C%89%E4%B8%80%E4%B8%AA%E7%B1%BB%E4%BC%BC%E7%9A%84%E5%87%BD%E6%95%B0%20strpbrk%20%28%29%20%E7%9B%B8%E5%90%8C%E5%8A%9F%E8%83%BD%EF%BC%8C%E4%BD%86%E6%98%AF%E8%BF%94%E5%9B%9E%E7%9A%84%E6%98%AF%E5%AD%97%E7%AC%A6%E4%B8%B2%E6%8C%87%E9%92%88%E3%80%82
  static int strcspn ( final char[] pStr, final char[] pStrSet ) {

    // map有32个字节的大小，也就是256个bit，可把map看成一个2维数组[32][8]
    char[] map = new char[32];
    // 第一部预处理：每个ASCII码(设为c)有8bit，把它分成2部分，低3位构成下标j(通过c&7(2进制为111)),
    // 高5位构成下标i(通过c>>3得到)。这样在map[i][j]中置1表示字符存在
    int i = 0;
    while(i < pStrSet.length)
    {
      map[pStrSet[i] >> 3] |= (1 << (pStrSet[i] & 7));
      i++;
    }
    map[0] |= 1;
    int count = 0;
    while((map[pStr[count] >> 3] & (1 << (pStr[count] & 7))) == 0)
    {
      count++;
    }
    return count;
  }

  static char[] strchr(final char[] str, final char b) {
    String a = new String(str);
    int idx = a.indexOf(b);
    if (idx < 0) {
      return null;
    }
    return a.substring(idx).toCharArray();
  }
}
