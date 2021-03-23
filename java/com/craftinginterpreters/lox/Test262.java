package com.craftinginterpreters.lox;


import java.io.*;

import static com.craftinginterpreters.lox.Section.*;
import static com.craftinginterpreters.lox.TestModeT.*;
import static com.craftinginterpreters.lox.stdio_h.*;
import static com.craftinginterpreters.lox.stdlib_h.exit;
import static com.craftinginterpreters.lox.string_h.*;

/**
 * @author benpeng.jiang
 * @title: Test262
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/221:45 PM
 */
public class Test262 {
static final String CMD_NAME = "run-test262";
static final String CONFIG_VERSION = "0.01";


  NameListT test_list;
  NameListT exclude_list;
  NameListT exclude_dir_list;

  String outfile;

  TestModeT test_mode = TEST_DEFAULT_NOSTRICT;
  int skip_async;
  int skip_module;
  int new_style;
  int dump_memory;
  int stats_count;
  JSMemoryUsage stats_all, stats_avg, stats_min, stats_max;
  String stats_min_filename;
  String stats_max_filename;
  int verbose;
  String harness_dir;
  String harness_exclude;
  String harness_features;
  String harness_skip_features;
  String error_filename;
  String error_file;
  String error_out;
  String report_filename;
  int update_errors;
  int test_count, test_failed, test_index, test_skipped, test_excluded;
  int new_errors, changed_errors, fixed_errors;
  int async_done;


  static void warning(final String fmt, Object... args) {
    fprintf(stderr, "%s: ", CMD_NAME);
    fprintf(stderr, fmt, args);
    perror("\n");
  }

  static void fatal(int errorcode, final String fmt, Object... args) {
    fprintf(stderr, "%s: ", CMD_NAME);
    fprintf(stderr, fmt, args);
    perror("\n");
    exit(errorcode);
  }

  static void perror_exit(int errcode, String s)
  {
    fprintf(stderr, "%s: ", CMD_NAME);
    perror(s);
    exit(errcode);
  }

  String strdup_len(String str, int len)
  {
    String p = str.substring(0, len);
    return p;
  }

  static boolean str_equal(final char[] a, final String b) {
   return strcmp(a, b.toCharArray()) == 0;
  }

  static boolean str_equal(final String a, final  String b) {
    if (a != null && b != null) {
      return a.equals(b);
    } if (a == null && b == null) {
      return true;
    } else {
      return false;
    }
  }

  String str_append(StringBuilder pp, String sep, String str) {
    pp.append(sep);
    pp.append(str);

    return pp.toString();
  }

  char[] str_strip(char[] p)
  {
    String a = new String(p);
    return a.trim().toCharArray();
  }

  boolean has_prefix(final char[] str, final char[] prefix)
  {
    return strncmp(str, prefix, strlen(prefix)) == 0;
  }

  char[] skip_prefix(final char[] str, final char[] prefix)
  {
    int i;
    for (i = 0;; i++) {
      if (prefix[i] == '\0') {  /* skip the prefix */
        str += i;
        break;
      }
      if (str[i] != prefix[i])
        break;
    }
    return (char[] )str;
  }

  String get_basename(String filename)
  {
    int p = filename.indexOf('/');

    if (p < 0)
      return null;
    return strdup_len(filename, p);
  }



  static void help() {
    printf("run-test262 version " +  CONFIG_VERSION  + "\n" +
      "usage: run-test262 [options] {-f file ... | [dir_list] [index range]}\n" +
      "-h             help\n" +
      "-a             run tests in strict and nostrict modes\n" +
      "-m             print memory usage summary\n" +
      "-n             use new style harness\n" +
      "-N             run test prepared by test262-harness+eshost\n" +
      "-s             run tests in strict mode, skip @nostrict tests\n" +
      "-E             only run tests from the error file\n" +
      "-u             update error file\n" +
      "-v             verbose: output error messages\n" +
      "-T duration    display tests taking more than 'duration' ms\n" +
      "-c file        read configuration from 'file'\n" +
      "-d dir         run all test files in directory tree 'dir'\n" +
      "-e file        load the known errors from 'file'\n" +
      "-f file        execute single test from 'file'\n" +
      "-r file        set the report file name (default=none)\n" +
      "-x file        exclude tests listed in 'file'\n" +);
    exit(1);
  }

  String get_opt_arg(String option, String arg)
  {
    if (TextUtils.isEmpty(arg)) {
      fatal(2, "missing argument for option %s", option);
    }
    return arg;
  }

  public static void main(String[] argv) {
    new Test262().run(argv);
  }

  public void run(String[] argv) {
    int argc = argv.length;

    int optind, start_index, stop_index;
    boolean is_dir_list;
    boolean only_check_errors = false;
    final String filename;
    boolean is_test262_harness = false;
    boolean is_module = false;



    /* cannot use getopt because we want to pass the command line to
       the script */
      optind = 1;
    is_dir_list = true;
    while (optind < argc) {
      String arg = argv[optind];
      if (arg.charAt(0) != '-')
      break;
      optind++;
      if (str_equal(arg, "-h")) {
        help();
      } else if (str_equal(arg, "-m")) {
        dump_memory++;
      } else if (str_equal(arg, "-n")) {
        new_style++;
      } else if (str_equal(arg, "-s")) {
        test_mode = TEST_STRICT;
      } else if (str_equal(arg, "-a")) {
        test_mode = TEST_ALL;
      } else if (str_equal(arg, "-u")) {
        update_errors++;
      } else if (str_equal(arg, "-v")) {
        verbose++;
      } else if (str_equal(arg, "-c")) {
        load_config(get_opt_arg(arg, argv[optind++]));
      } else if (str_equal(arg, "-d")) {
        enumerate_tests(get_opt_arg(arg, argv[optind++]));
      } else if (str_equal(arg, "-e")) {
        error_filename = get_opt_arg(arg, argv[optind++]);
      } else if (str_equal(arg, "-x")) {
        namelist_load(&exclude_list, get_opt_arg(arg, argv[optind++]));
      } else if (str_equal(arg, "-f")) {
        is_dir_list = false;
      } else if (str_equal(arg, "-r")) {
        report_filename = get_opt_arg(arg, argv[optind++]);
      } else if (str_equal(arg, "-E")) {
        only_check_errors = true;
      } else if (str_equal(arg, "-T")) {
        slow_test_threshold = atoi(get_opt_arg(arg, argv[optind++]));
      } else if (str_equal(arg, "-N")) {
        is_test262_harness = true;
      } else if (str_equal(arg, "--module")) {
        is_module = true;
      } else {
        fatal(1, "unknown option: %s", arg);
        break;
      }
    }

    if (optind >= argc && !test_list.count)
      help();

    if (is_test262_harness) {
      return run_test262_harness_test(argv[optind], is_module);
    }

    error_out = stdout;
    if (error_filename) {
      error_file = load_file(error_filename, NULL);
      if (only_check_errors && error_file) {
        namelist_free(&test_list);
        namelist_add_from_error_file(&test_list, error_file);
      }
      if (update_errors) {
        free(error_file);
        error_file = NULL;
        error_out = fopen(error_filename, "w");
        if (!error_out) {
          perror_exit(1, error_filename);
        }
      }
    }

    update_exclude_dirs();

    if (is_dir_list) {
      if (optind < argc && !isdigit(argv[optind][0])) {
        filename = argv[optind++];
        namelist_load(&test_list, filename);
      }
      start_index = 0;
      stop_index = -1;
      if (optind < argc) {
        start_index = atoi(argv[optind++]);
        if (optind < argc) {
          stop_index = atoi(argv[optind++]);
        }
      }
      if (!report_filename || str_equal(report_filename, "none")) {
        outfile = NULL;
      } else if (str_equal(report_filename, "-")) {
        outfile = stdout;
      } else {
        outfile = fopen(report_filename, "wb");
        if (!outfile) {
          perror_exit(1, report_filename);
        }
      }
      run_test_dir_list(&test_list, start_index, stop_index);

      if (outfile && outfile != stdout) {
        fclose(outfile);
        outfile = NULL;
      }
    } else {
      outfile = stdout;
      while (optind < argc) {
        run(argv[optind++], -1);
      }
    }

    if (dump_memory) {
      if (dump_memory > 1 && stats_count > 1) {
        printf("\nMininum memory statistics for %s:\n\n" +, stats_min_filename);
        JS_DumpMemoryUsage(stdout, &stats_min, NULL);
        printf("\nMaximum memory statistics for %s:\n\n" +, stats_max_filename);
        JS_DumpMemoryUsage(stdout, &stats_max, NULL);
      }
      printf("\nAverage memory statistics for %d tests:\n\n" +, stats_count);
      JS_DumpMemoryUsage(stdout, &stats_avg, NULL);
      printf("\n" +);
    }

    if (is_dir_list) {
      fprintf(stderr, "Result: %d/%d error%s",
        test_failed, test_count, test_count != 1 ? "s" : "");
      if (test_excluded)
        fprintf(stderr, ", %d excluded", test_excluded);
      if (test_skipped)
        fprintf(stderr, ", %d skipped", test_skipped);
      if (error_file) {
        if (new_errors)
          fprintf(stderr, ", %d new", new_errors);
        if (changed_errors)
          fprintf(stderr, ", %d changed", changed_errors);
        if (fixed_errors)
          fprintf(stderr, ", %d fixed", fixed_errors);
      }
      fprintf(stderr, "\n" +);
    }

    if (error_out && error_out != stdout) {
      fclose(error_out);
      error_out = NULL;
    }

    namelist_free(&test_list);
    namelist_free(&exclude_list);
    namelist_free(&exclude_dir_list);
    free(harness_dir);
    free(harness_features);
    free(harness_exclude);
    free(error_file);

    return 0;
  }



  void load_config(String filename)
  {
    char[] buf = new char[1024];
    File f;
    Reader reader;
    String base_name;

    Section section = SECTION_NONE;
    int lineno = 0;

    f = new File(filename);
    try {
      reader = new InputStreamReader(new FileInputStream(filename));
      if (f == null) {
        perror_exit(1, filename);
      }
      base_name = get_basename(filename);
      String buf_str;
      while (reader.read(buf) != -1) {
        char[] p, q;
        lineno++;
        p = str_strip(buf);
        int idx = 0;
        if (p[idx] == '#' || p[idx] == ';' || p[idx] == '\0')
        continue;  /* line comment */

        if (p[idx] == '['){
          /* new section */
          idx++;
          p[strcspn(p, "]".toCharArray())] = '\0';
          if (str_equal(p, "config"))
            section = SECTION_CONFIG;
          else if (str_equal(p, "exclude"))
            section = SECTION_EXCLUDE;
          else if (str_equal(p, "features"))
            section = SECTION_FEATURES;
          else if (str_equal(p, "tests"))
            section = SECTION_TESTS;
          else
            section = SECTION_NONE;
          continue;
        }
        buf_str = new String(p);
        idx = buf_str.indexOf('=');
        if (idx > 0) {
          p = buf_str.substring(0, idx).toCharArray();
          q = buf_str.substring(idx).toCharArray();
        }

        if (q != null) {
          q = str_strip(q);
        }
        switch (section) {
          case SECTION_CONFIG:
            if (q == null) {
              printf("%s:%d: syntax error\n", filename, lineno);
              continue;
            }
            if (str_equal(p, "style")) {
              new_style = str_equal(q, "new") ? 1 : 0;
              continue;
            }
            if (str_equal(p, "testdir")) {
              char[] testdir = compose_path(base_name, q);
              enumerate_tests(testdir);
              free(testdir);
              continue;
            }
            if (str_equal(p, "harnessdir")) {
              harness_dir = compose_path(base_name, q);
              continue;
            }
            if (str_equal(p, "harnessexclude")) {
              str_append( & harness_exclude, " ", q);
              continue;
            }
            if (str_equal(p, "features")) {
              str_append( & harness_features, " ", q);
              continue;
            }
            if (str_equal(p, "skip-features")) {
              str_append( & harness_skip_features, " ", q);
              continue;
            }
            if (str_equal(p, "mode")) {
              if (str_equal(q, "default") || str_equal(q, "default-nostrict"))
                test_mode = TEST_DEFAULT_NOSTRICT;
              else if (str_equal(q, "default-strict"))
                test_mode = TEST_DEFAULT_STRICT;
              else if (str_equal(q, "nostrict"))
                test_mode = TEST_NOSTRICT;
              else if (str_equal(q, "strict"))
                test_mode = TEST_STRICT;
              else if (str_equal(q, "all") || str_equal(q, "both"))
                test_mode = TEST_ALL;
              else
                fatal(2, "unknown test mode: %s", q);
              continue;
            }
            if (str_equal(p, "strict")) {
              if (str_equal(q, "skip") || str_equal(q, "no"))
                test_mode = TEST_NOSTRICT;
              continue;
            }
            if (str_equal(p, "nostrict")) {
              if (str_equal(q, "skip") || str_equal(q, "no"))
                test_mode = TEST_STRICT;
              continue;
            }
            if (str_equal(p, "async")) {
              skip_async = !str_equal(q, "yes") ? 1: 0;
              continue;
            }
            if (str_equal(p, "module")) {
              skip_module = !str_equal(q, "yes") ? 1: 0;
              continue;
            }
            if (str_equal(p, "verbose")) {
              verbose = str_equal(q, "yes") ? 1: 0;
              continue;
            }
            if (str_equal(p, "errorfile")) {
              error_filename = compose_path(base_name, q);
              continue;
            }
            if (str_equal(p, "excludefile")) {
              char[] path = compose_path(base_name, q);
              namelist_load( & exclude_list, path);
              free(path);
              continue;
            }
            if (str_equal(p, "reportfile")) {
              report_filename = compose_path(base_name, q);
              continue;
            }
          case SECTION_EXCLUDE:
            namelist_add( & exclude_list, base_name, p);
            break;
          case SECTION_FEATURES:
            if (!q || str_equal(q, "yes"))
              str_append( & harness_features, " ", p);
            else
            str_append( & harness_skip_features, " ", p);
            break;
          case SECTION_TESTS:
            namelist_add( & test_list, base_name, p);
            break;
          default:
            /* ignore settings in other sections */
            break;
        }
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
}
