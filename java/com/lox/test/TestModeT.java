package com.lox.test;

/**
 * @author benpeng.jiang
 * @title: TestModeT
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/221:56 PM
 */
public enum TestModeT {
  TEST_DEFAULT_NOSTRICT, /* run tests as nostrict unless test is flagged as strictonly */
  TEST_DEFAULT_STRICT,   /* run tests as strict unless test is flagged as nostrict */
  TEST_NOSTRICT,         /* run tests as nostrict, skip strictonly tests */
  TEST_STRICT,           /* run tests as strict, skip nostrict tests */
  TEST_ALL,              /* run tests in both strict and nostrict, unless restricted by spec */
}
