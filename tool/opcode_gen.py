import re
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

import static com.craftinginterpreters.lox.OPCodeEnum.*;
import static com.craftinginterpreters.lox.OPCodeFormat.*;
public class OPCodeInfo {
  public static Map<Integer, JSOpCode> opcode_info;
  public static Map<Integer, OPCodeEnum> opcode_enum;

  static {
    opcode_enum = new HashMap<>();
    for (OPCodeEnum codeEnum: OPCodeEnum.values()) {
      opcode_enum.put(codeEnum.ordinal(), codeEnum);
    }
    
    opcode_info = new HashMap<>();
'''

classFileEnd = '}'

javaSrc = '../java/com/craftinginterpreters/lox'
OPCodeEnum = open(javaSrc + "/OPCodeEnum.java", "w")
JSOpCode = open(javaSrc + "/OPCodeInfo.java", "w")

OPCodeEnumCodes = [OPCodeEnumStart]
JSOpCodeCodes = [JSOpCodeStart]


for i in range(0, lines.__len__(), 1):
    list = []  ## 空列表, 将第i行数据存入list中
    line = lines[i]
    words = line.split(',')

    for idx, word in enumerate(words):
        word = re.sub('\s|\t|\n','',word)
        if idx == 0 and '(' in word:
            items = word.split('(')
            list.extend(items)
        elif len(list) == 5 and ')' in word:
            items = word.split(')')
            list.append(items[0])
        else:
            list.append(word)

    name = list[0]
    if name not in ['DEF', 'def']:
        continue

    codeEnum = list[1]

    OPCodeEnumCodes.append('  OP_' + codeEnum + ',\n')
    space = '    '
    JSOpCodeCodes.append(space)
    opcode = 'opcode_info.put(\n'
    for index, code in enumerate(list[1:6]):
        if index == 0:
            code = space + '  OP_' + code + '.ordinal(),\n' \
                   + space + '  new JSOpCode(\"' + code + '\"'
        else:
            if code in keywords:
                 code = code.capitalize()
            code = ',' + code
        opcode = opcode + code

    opcode = opcode + '));'
    JSOpCodeCodes.append(opcode + '\n')
    count = count + 1

OPCodeEnumCodes.append(classFileEnd)
OPCodeEnum.writelines(OPCodeEnumCodes)
OPCodeEnum.close()

JSOpCodeCodes.append(' ' + classFileEnd + '\n')
JSOpCodeCodes.append(classFileEnd)
JSOpCode.writelines(JSOpCodeCodes)
JSOpCode.close()
