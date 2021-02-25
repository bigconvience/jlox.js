import fileinput
import string

filepath = "./quickjs-opcode.h"

f = open(filepath, "r")

lines = f.readlines()  # 读取全部内容
count = 1

keywords = ['return', 'null', 'import', 'throw', 'catch', 'goto', 'instanceof']

OPCodeEnumStart = '''package com.craftinginterpreters.lox;

public enum OPCodeEnum {
'''

JSOpCodeStart = '''package com.craftinginterpreters.lox;


public class JSOpCode {
  OPCodeEnum id;
  int size;
  int n_pop;
  int n_push;
  OPCodeFormat f;
}

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
            print('striped: ' + codeEnum)
        if codeEnum in keywords:
            codeEnum = codeEnum.capitalize()
        OPCodeEnumCodes.append('  ' + codeEnum + ',\n')
        JSOpCode.write(line)

OPCodeEnumCodes.append(classFileEnd)
OPCodeEnum.writelines(OPCodeEnumCodes)
OPCodeEnum.close()

JSOpCode.write(classFileEnd)
JSOpCode.close()