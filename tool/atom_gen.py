import re

filepath = "./quickjs-atom.h"

f = open(filepath, "r")

lines = f.readlines()  # 读取全部内容

javaSrc = '../java/com/craftinginterpreters/lox'
JSAtomEnum = open(javaSrc + "/JSAtomEnum.java", "w")
JSAtomEnumStart = '''package com.craftinginterpreters.lox;

public enum JSAtomEnum {
'''
classFileEnd = '}'
space = '    '
AtomCodes = [JSAtomEnumStart, space, '__JS_ATOM_NULL,\n']

JSAtomInit = open(javaSrc + "/JSAtomInit.java", "w")
JSAtomStart = '''package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

public class JSAtomInit {
  static List<String> js_atom_init;
  
  static {
    js_atom_init = new ArrayList<>();
    js_atom_init.add(null);
'''

JSAtomCodes = [JSAtomStart, '\n']

index = 1
for i in range(0, len(lines), 1):
    list = []  ## 空列表, 将第i行数据存入list中
    line = lines[i]
    words = line.split(',')

    for idx, word in enumerate(words):
        word = re.sub('\s|\t|\n','',word)
        if idx == 0 and '(' in word:
            items = word.split('(')
            list.extend(items)
        elif len(list) == 2 and ')' in word:
            items = word.split(')')
            list.append(items[0])
        else:
            list.append(word)

    name = list[0]
    if name != 'DEF':
        continue
    print(i, index, list)
    index = index + 1
    JSAtomCodes.append(space + 'js_atom_init.add(' + list[2] + ');\n')
    AtomCodes.append(space + 'JS_ATOM_' + list[1] + ',\n')


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

AtomCodes.append(space + 'JS_ATOM_END\n')
AtomCodes.append(classFileEnd)
JSAtomEnum.writelines(AtomCodes)
JSAtomEnum.close()

JSAtomCodes.append('  }\n' + classFileEnd)
JSAtomInit.writelines(JSAtomCodes)
JSAtomInit.close()
