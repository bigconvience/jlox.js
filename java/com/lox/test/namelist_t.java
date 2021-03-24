package com.lox.test;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

import static com.lox.javascript.lib.cutils_h.has_suffix;
import static com.lox.test.Test262.perror_exit;
import static com.lox.test.Test262Utils.*;
import static com.lox.clibrary.cctype_h.*;

/**
 * @author benpeng.jiang
 * @title: NameListT
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/228:15 PM
 */
public class namelist_t {
  String[] array;
  int count;
  int size;
  boolean sorted = true;

  static int namelist_cmp(String str1, String str2) {
    /* compare strings in modified lexicographical order */
    int a = 0, b = 0;
    for (; ; ) {
      char ca = str1.charAt(a++);
      char cb = str2.charAt(b++);
      if (isdigit(ca) && isdigit(cb)) {
        int na = ca - '0';
        int nb = cb - '0';
        while (isdigit(ca = str1.charAt(a++)))
          na = na * 10 + ca - '0';
        while (isdigit(cb = str2.charAt(b++)))
          nb = nb * 10 + cb - '0';
        if (na < nb)
          return -1;
        if (na > nb)
          return +1;
      }
      if (ca < cb)
        return -1;
      if (ca > cb)
        return +1;
      if (ca == '\0')
        return 0;
    }
  }


  private static Comparator<String> namelist_cmp_indirect = new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      return namelist_cmp(o1, o2);
    }
  };

  void namelist_sort(namelist_t lp) {
    int i, count;
    if (lp.count > 1) {
      Arrays.sort(lp.array, namelist_cmp_indirect);
      /* remove duplicates */
      for (count = i = 1; i < lp.count; i++) {
        if (namelist_cmp(lp.array[count - 1], lp.array[i]) == 0) {
          lp.array[i] = null;
        } else {
          lp.array[count++] = lp.array[i];
        }
      }
      lp.count = count;
    }
    lp.sorted = true;
  }

  int namelist_find(namelist_t lp, String name) {
    int a, b, m, cmp;

    if (!lp.sorted) {
      namelist_sort(lp);
    }
    for (a = 0, b = lp.count; a < b; ) {
      m = a + (b - a) / 2;
      cmp = namelist_cmp(lp.array[m], name);
      if (cmp < 0)
        a = m + 1;
      else if (cmp > 0)
        b = m;
      else
        return m;
    }
    return -1;
  }

  static void namelist_load(namelist_t lp, String filename) {
    char[] buf = new char[1024];
    String base_name;
    File f;
    Reader reader = null;
    f = new File(filename);
    if (f == null) {
      perror_exit(1, filename);
    }
    base_name = get_basename(filename);

    try {
      reader = new InputStreamReader(new FileInputStream(f));
      if (f == null) {
        perror_exit(1, filename);
      }

      while (reader.read(buf) != -1) {
        char[] p = str_strip(buf);
        if (p[0] == '#' || p[0] == ';' || p[0] == '\0')
          continue;  /* line comment */

        namelist_add(lp, base_name, new String(p));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    }
  }


  static void namelist_add(namelist_t lp, final String base, final char[] name) {
    namelist_add(lp, base, new String(name));
  }
  static void namelist_add(namelist_t lp, final String base, final String name) {
    String s;

    s = compose_path(base, name);
    if (s == null) {
      fatal(1, "allocation failure\n");
    }
    if (lp.count == lp.size) {
      int newsize = lp.size + (lp.size >> 1) + 4;
      lp.array = Arrays.copyOf(lp.array, newsize);
      lp.size = newsize;
    }
    lp.array[lp.count] = s;
    lp.count++;
    return;
  }

  public static void namelist_add_from_error_file(namelist_t lp, String file)
  {

  }

  public static void namelist_free(namelist_t lp)
  {
    lp.size = 0;
  }

  static void enumerate_tests(namelist_t test_list, String path) {
    namelist_t lp = test_list;
    int start = lp.count;
    File file = new File(path);
    getDirectory(test_list, file);
    Arrays.sort(lp.array, namelist_cmp_indirect);
  }

  private static void getDirectory(namelist_t test_list, File file) {
    File flist[] = file.listFiles();
    if (flist == null || flist.length == 0) {
      return ;
    }
    for (File f : flist) {
      if (f.isDirectory()) {
        //里将列出所有的文件夹
        getDirectory(test_list, f);
      } else {
        add_test_file(test_list, f.getName());
      }
    }
  }

  static int add_test_file(namelist_t test_list, String filename) {
    namelist_t lp = test_list;
    if (has_suffix(filename, ".js") && !has_suffix(filename, "_FIXTURE.js"))
      namelist_add(lp, null, filename);
    return 0;
  }
}
