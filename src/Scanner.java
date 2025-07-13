import java.util.ArrayList;

class ScanException extends RuntimeException {
    public ScanException(String s, int line) {
        super("Error at line "+line+": "+s);
    }
}

public class Scanner {
    private int line = 0;
    private int start = 0;
    private int current = 0;
    private String source;
    public Scanner(String source) {
        this.source = source;
    }
    public ArrayList<Token> scan() {
        Token t;
        ArrayList<Token> l = new ArrayList<>();
        while ((t = scanOnce()).type != TokenType.EOF) {
            l.add(t);
        }
        l.add(t);
        return l;
    }
    char advance() throws IndexOutOfBoundsException {
        return source.charAt(current++);
    }
    public Token scanOnce() {
        skipWhitespace();
        start = current;
        if (isAtEnd()) {
            return makeToken(TokenType.EOF);
        }
        char c = advance();
        switch (c) {
            case '{':
                return makeToken(TokenType.LBRACE);
            case '}':
                return makeToken(TokenType.RBRACE);
            case ',':
                return makeToken(TokenType.COMMA);
            case ';':
                return makeToken(TokenType.SEMICOLON);
            default:
                if (isAlpha(c) && isAlpha(peek())) return identifier();
                else if (c == 'r' && isNumber(peek())) return rName();
                else if (isNumber(c)) return literal();
                else throw new ScanException("Invalid token.", line);
        }
    }
    Token literal() {
        while (!isAtEnd() && isNumber(peek())) advance();
        return makeToken(TokenType.LITERAL);
    }
    Token rName() {
        while (!isAtEnd() && isNumber(peek())) advance();
        return makeToken(TokenType.RNAME);
    }
    boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }
    Token identifier() {
        while (isAlpha(peek())) {
            advance();
        }
        return makeToken(idenType());
    }
    TokenType idenType() {
        String token = source.substring(start, current);
        switch (token) {
            case "inc":
                return TokenType.INC;
            case "dec":
                return TokenType.DEC;
            case "add":
                return TokenType.ADD;
            case "sub":
                return TokenType.SUB;
            case "copy":
                return TokenType.COPY;
            case "print":
                return TokenType.PRINT;
            case "swap":
                return TokenType.SWAP;
            case "while":
                return TokenType.WHILE;
            case "input":
                return TokenType.INPUT;
            case "put":
                return TokenType.PUT;
            case "clear":
                return TokenType.CLEAR;
            default:
                throw new ScanException("Invalid literal name.", line);
        }
    }
    char peek() {
        try {
            return source.charAt(current);
        } catch (Exception e) {
            return '\0';
        }
    }
    boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    Token makeToken(TokenType t) {
        return new Token(source.substring(start, current), t, line);
    }
    boolean isAtEnd() {
        return current >= source.length();
    }
    public void skipWhitespace() {
        boolean looping = true;
        while (looping) {
            char c = peek();
            switch (c) {
                case ' ':
                case '\r':
                case '\t':
                    advance();
                    break;
                case '\n':
                    line++;
                    advance();
                    break;
                case '@':
                    while (peek() != '\n') advance();
                    line++;
                    break;
                default:
                    looping = false;
            }
        }
        start = current;
    }
}
