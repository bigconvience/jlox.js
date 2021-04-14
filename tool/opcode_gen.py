import re
filepath = "./quickjs-opcode.h"

f = open(filepath, "r")

lines = f.readlines()  # 读取全部内容
count = 1

keywords = ['return', 'null', 'import', 'throw', 'catch', 'goto', 'instanceof', 'const']

OPCodeEnumStart = '''package com.lox.javascript;

public enum OPCodeEnum {
'''
ShortOPCodeEnumStart = '''package com.lox.javascript;

public enum ShortOPCodeEnum {
'''

JSOpCodeStart = '''package com.lox.javascript;

import java.util.HashMap;
import java.util.Map;

import static com.lox.javascript.OPCodeEnum.*;
import static com.lox.javascript.OPCodeFormat.*;
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

ShortOpCodeStart = '''package com.lox.javascript;

import java.util.HashMap;
import java.util.Map;

import static com.lox.javascript.ShortOPCodeEnum.*;
import static com.lox.javascript.OPCodeFormat.*;
public class ShortOPCodeInfo {
  public static Map<Integer, JSOpCode> opcode_info;
  public static Map<Integer, ShortOPCodeEnum> opcode_enum;

  static {
    opcode_enum = new HashMap<>();
    for (ShortOPCodeEnum codeEnum: ShortOPCodeEnum.values()) {
      opcode_enum.put(codeEnum.ordinal(), codeEnum);
    }
    
    opcode_info = new HashMap<>();
'''
classFileEnd = '}'

javaSrc = '../java/com/lox/javascript'
OPCodeEnum = open(javaSrc + "/OPCodeEnum.java", "w")
ShortOPCodeEnum = open(javaSrc + "/ShortOPCodeEnum.java", "w")
JSOpCode = open(javaSrc + "/OPCodeInfo.java", "w")
ShortOpCode = open(javaSrc + "/ShortOPCodeInfo.java", "w")

OPCodeEnumCodes = [OPCodeEnumStart]
ShortOPCodeEnumCodes = [ShortOPCodeEnumStart]
JSOpCodeCodes = [JSOpCodeStart]
ShortOpCodeCodes = [ShortOpCodeStart]

after_nop = False;

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

    if after_nop:
        if name == 'DEF':
            ShortOPCodeEnumCodes.append('  OP_' + codeEnum + ',\n')
        elif name == 'def':
            OPCodeEnumCodes.append('  OP_' + codeEnum + ',\n')
    else:
        OPCodeEnumCodes.append('  OP_' + codeEnum + ',\n')
        ShortOPCodeEnumCodes.append('  OP_' + codeEnum + ',\n')


    space = '    '
    JSOpCodeCodes.append(space)
    ShortOpCodeCodes.append(space)
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
    if after_nop:
        if name == 'DEF':
            ShortOpCodeCodes.append(opcode + '\n')
        elif name == 'def':
            JSOpCodeCodes.append(opcode + '\n')
    else:
        JSOpCodeCodes.append(opcode + '\n')
        ShortOpCodeCodes.append(opcode + '\n')

    if codeEnum == 'nop':
        after_nop = True
    count = count + 1

OPCodeEnumCodes.append(classFileEnd)
OPCodeEnum.writelines(OPCodeEnumCodes)
OPCodeEnum.close()

ShortOPCodeEnumCodes.append(classFileEnd)
ShortOPCodeEnum.writelines(ShortOPCodeEnumCodes)
ShortOPCodeEnum.close()

JSOpCodeCodes.append(' ' + classFileEnd + '\n')
JSOpCodeCodes.append(classFileEnd)
JSOpCode.writelines(JSOpCodeCodes)
JSOpCode.close()

ShortOpCodeCodes.append(' ' + classFileEnd + '\n')
ShortOpCodeCodes.append(classFileEnd)
ShortOpCode.writelines(ShortOpCodeCodes)
ShortOpCode.close()
