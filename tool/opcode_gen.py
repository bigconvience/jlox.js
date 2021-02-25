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

import java.util.ArrayList;
import java.util.List;

import  static com.craftinginterpreters.lox.OPCodeEnum.*;
import  static com.craftinginterpreters.lox.OPCodeFormat.*;
public class JSOpCode {
  OPCodeEnum id;
  int size;
  int n_pop;
  int n_push;
  OPCodeFormat f;

  public JSOpCode(OPCodeEnum id, int size, int n_pop, int n_push, OPCodeFormat f) {
    this.id = id;
    this.size = size;
    this.n_pop = n_pop;
    this.n_push = n_push;
    this.f = f;
  }


  public static List<JSOpCode> opcode_info;

  {
    opcode_info = new ArrayList<>();
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

        opcode = '    opcode_info.add(new JSOpCode('
        for index, code in enumerate(list[1:6]):
            if index == 0:
                code = 'OP_' + code
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