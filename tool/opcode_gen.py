import fileinput
import string

filepath = "./quickjs-opcode.h"

f = open(filepath, "r")

lines = f.readlines()  # 读取全部内容
count = 1

keywords = ['return', 'null', 'import', 'throw', 'catch', 'goto', 'instanceof', 'const']

OPCodeEnumStart = '''package com.craftinginterpreters.lox;

public enum OPCodeEnum {
'''

JSOpCodeStart = '''package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

import  static com.craftinginterpreters.lox.OPCodeEnum.*;
import  static com.craftinginterpreters.lox.OPCodeFormat.*;
public class JSOpCode {
  String id;
  int size;
  int n_pop;
  int n_push;
  OPCodeFormat f;

  public JSOpCode(String id, int size, int n_pop, int n_push, OPCodeFormat f) {
    this.id = id;
    this.size = size;
    this.n_pop = n_pop;
    this.n_push = n_push;
    this.f = f;
  }


  public static Map<Integer, JSOpCode> opcode_info;

  {
    opcode_info = new HashMap<>();
'''

classFileEnd = '}'

javaSrc = '../java/com/craftinginterpreters/lox'
OPCodeEnum =open(javaSrc + "/OPCodeEnum.java", "w")
JSOpCode =open(javaSrc + "/JSOpCode.java", "w")


OPCodeEnumCodes = [OPCodeEnumStart]
JSOpCodeCodes = [JSOpCodeStart]

for i in range(0, lines.__len__(), 1):
    list = []  ## 空列表, 将第i行数据存入list中
    line = lines[i]
    for word in line.split():
        word = word.strip(string.whitespace)
        list.append(word)

    if len(list) >= 2 and list[0] == 'DEF(':
        count = count + 1
        codeEnum = list[1]
        if codeEnum.endswith(','):
            codeEnum = codeEnum.strip(',')

        OPCodeEnumCodes.append('  OP_' + codeEnum + ',\n')
        space = '    '
        JSOpCodeCodes.append(space)
        opcode = 'opcode_info.put(\n'
        for index, code in enumerate(list[1:6]):
            if index == 0:
                code = code.strip(',')
                code = space + '  OP_' + code + '.ordinal(),\n' \
                       + space + '  new JSOpCode(\"' + code + '\",'
            if code.endswith(')'):
                code = code.strip(')')
            if code in keywords:
                code = code.capitalize()
            opcode = opcode + code

        opcode = opcode + '));'
        JSOpCodeCodes.append(opcode + '\n')

OPCodeEnumCodes.append(classFileEnd)
OPCodeEnum.writelines(OPCodeEnumCodes)
OPCodeEnum.close()

JSOpCodeCodes.append(' ' + classFileEnd + '\n')
JSOpCodeCodes.append(classFileEnd)
JSOpCode.writelines(JSOpCodeCodes)
JSOpCode.close()