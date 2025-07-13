public class Token {
    public final String token;
    public final TokenType type;
    public final int line;

    public Token(String token, TokenType type, int line) {
        this.token = token;
        this.type = type;
        this.line = line;
    }
    public String toString() {
        return "{"+type+", "+token+", "+line+"}";
    }
}
enum TokenType {
    PUT,
    CLEAR,
    INC,
    DEC,
    ADD,
    SUB,
    COPY,
    SWAP,
    WHILE,
    PRINT,
    INPUT,

    RNAME, // register
    LITERAL,

    COMMA,
    LBRACE,
    RBRACE,
    SEMICOLON,

    EOF
}