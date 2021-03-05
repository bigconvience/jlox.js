def find_all_indexes(input_str, substring):
    l2 = []
    length = len(input_str)
    index = 0
    while index < length:
        i = input_str.find(substring, index)
        if i == -1:
            return l2
        l2.append(i)
        index = i + 1
    return l2

class Macro:
    def __init__(self, name, args, body):
        self.name = name
        self.args = args
        self.body = body
        self.replace_list = []

    def build_replace_rules(self):
        for arg in self.args:
           arg_idx_list = find_all_indexes(arg, self.body)
           self.replace_list.extend(arg_idx_list)
        print(self.replace_list)

class ParameterizedMacro:
    def __init__(self, name, params):
        self.name = name
        self.params = params



def parameterized_macro_replace(macro, stmt):
    name = macro.name
    params = stmt.params
    args = macro.args
    if len(params) != len(args):
        return None

    replaces = macro.replace
    if replaces is None:
        return ''
