import string

filepath = "./quickjs-atom.h"

f = open(filepath, "r")

lines = f.readlines()  # 读取全部内容

javaSrc = '../java/com/craftinginterpreters/lox'
JSAtomEnum = open(javaSrc + "/AtomEnum.java", "w")
JSAtomEnumStart = '''package com.craftinginterpreters.lox;

public enum AtomEnum {
'''
classFileEnd = '}'
space = '    '
AtomCodes = [JSAtomEnumStart, space, '__JS_ATOM_NULL,\n']

JSAtom = open(javaSrc + "/JSAtom.java", "w")
JSAtomStart = '''package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

public class JSAtom {
  static final int JS_ATOM_TYPE_STRING = 0;
  static final int JS_ATOM_TYPE_GLOBAL_SYMBOL = 1;
  static final int JS_ATOM_TYPE_SYMBOL = 2;
  static final int JS_ATOM_TYPE_PRIVATE = 3;

  private final int val;
  public JSAtom(int val) {
    this.val = val;
  }
  public int getVal() {
    return val;
  }
  
  static List<String> js_atom_init;
  static {
    js_atom_init = new ArrayList<>();
'''

JSAtomCodes = [JSAtomStart, '\n']

index = 0
for i in range(0, len(lines), 1):
    list = []  ## 空列表, 将第i行数据存入list中
    line = lines[i]
    for word in line.split(','):
        word = word.strip(string.whitespace)
        list.append(word)

    if len(list) >= 2 and list[0].startswith('DEF('):
        first = list[0].removeprefix('DEF(').removesuffix(',')
        second = list[1]
        idx = second.index('\"')
        second = second[idx+1:]
        idx = second.index('\"')
        second = second[:idx]
        second = '\"' + second + '\"'
        index = index + 1
        print(str(index) + ':' + first + ' ' + second)
        AtomCodes.append(space + 'JS_ATOM_' + first + ',\n')
        JSAtomCodes.append(space + 'js_atom_init.add(' + second + ');\n')

AtomCodes.append(space + 'JS_ATOM_END\n')
AtomCodes.append(classFileEnd)
JSAtomEnum.writelines(AtomCodes)
JSAtomEnum.close()

JSAtomCodes.append('  }\n' + classFileEnd)
JSAtom.writelines(JSAtomCodes)
JSAtom.close()