from scanner import ParseState, TokenType
from macro import ParameterizedMacro

class Parser:
    def __init__(self, source):
        self.source = source

    def parse_marco(self):
        s = ParseState(self.source)
        s.next_token()
        ident = s.token.ident
        s.parse_expect(TokenType.identifier.value)
        print(ident)
        s.parse_expect('(')
        param_list = []
        param = s.token.val
        while param == TokenType.number.value or param == TokenType.identifier.value:
            param_list.append(param)
            if param == TokenType.number.value:
                print(s.token.num)
            else:
                print(s.token.ident)
            s.next_token()
            if s.token.val != ',':
                break
            else:
                s.next_token()
                param = s.token.val
        s.parse_expect(')')
        macro = ParameterizedMacro(ident, param_list)
