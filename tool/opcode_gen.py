import fileinput
import string

filepath = "./quickjs-opcode.h"

f = open(filepath, "r")

lines = f.readlines()  # 读取全部内容
count = 1

keywords = ['return,', 'null,', 'import,', 'throw,', 'catch,', 'goto,', 'instanceof,']

OPCodeEnum =open("out/OPCodeEnum.txt", "w")
for i in range(0, lines.__len__(), 1):
    list = []  ## 空列表, 将第i行数据存入list中
    for word in lines[i].split():
        word = word.strip(string.whitespace)
        list.append(word)

    if len(list) >= 2 and list[0] == 'DEF(':
        print('count: ' + str(count))
        count = count + 1
        codeEnum = list[1]
        if codeEnum in keywords:
            codeEnum = codeEnum.capitalize()
        print(codeEnum)
        OPCodeEnum.write(codeEnum + '\n')

OPCodeEnum.close()
