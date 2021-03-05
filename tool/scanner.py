from enum import Enum


class TokenType(Enum):
    number = -6
    identifier = -5
    left_paren = -4
    right_paren = -3
    comma = -2
    end = -1


class Token:
    def __init__(self, val):
        self.val = val
        self.ptr = None
        self.ident = None
        self.num = None


def lre_js_is_ident_next(c):
    return c.isalpha() or c.isdigit() or c == '_'


class ParseState:
    def __init__(self, buf):
        self.buf = buf
        self.start = 0
        self.end = len(buf)
        self.token = Token(0)

    def parse_error(self, fmt):
        raise Exception(fmt)

    def parse_expect(self, tok):
        if self.token.val != tok:
            self.parse_error("expecting {0}".format(tok))
        return self.next_token()

    def next_token(self):
        p = self.start
        self.token = Token(None)
        while True:
            c = self.buf[p]
            if c.isalpha() or c == '_':
                ident = self.parse_ident(p, c)
                self.token.ident = ident
                self.token.val = TokenType.identifier.value
                break
            if c.isdigit():
                num = self.parse_number(p, c)
                self.token.num = num
                self.token.val = TokenType.number.value
                break
            elif c == '\f':
                p = p + 1
                continue
            elif c == '\v':
                p = p + 1
                continue
            elif c == ' ':
                p = p + 1
                continue
            elif c == '\t':
                p = p + 1
                continue
            else:
                self.token.val = c
                p = p + 1
                self.start = p
                break
        return 0

    def parse_ident(self, start, c):
        lexeme = ''
        buf = self.buf
        while True:
            if c.isalpha():
                lexeme = lexeme + c
            start = start + 1
            c = buf[start]
            if not lre_js_is_ident_next(c):
                break
        self.start = start
        return lexeme

    def parse_number(self, start, c):
        lexeme = ''
        buf = self.buf
        while True:
            if c.isdigit():
                lexeme = lexeme + c
            start = start + 1
            c = buf[start]
            if not c.isdigit():
                break
        self.start = start
        return int(lexeme)

