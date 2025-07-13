import java.util.ArrayList;

class CompilerException extends RuntimeException {
    public CompilerException(String s, Token token) {
        super("Error at line "+token.line+" at token "+token.token+": "+s);
    }
}

public class Compiler {
    static final int CONTROL_REG = 63;
    static final int NUM_SPECIAL_REGS = 64;
    int rpos = 0;
    int position = 0;
    ArrayList<Token> tokens;
    public String compile(ArrayList<Token> tokens) {
        this.tokens = tokens;
        StringBuilder builder = new StringBuilder();
        while (!isAtEnd()) {
            builder.append(compileStatement());
        }
        return builder.toString();
    }
    public String compileStatement() {
        Token current = advance();
        StringBuilder sb = new StringBuilder();
        switch (current.type) {
            case PUT: {
                Token op1 = advance();
                consume(TokenType.COMMA, "Expected comma.");
                Token r = advance();
                int rid = rvalue(r.token);
                sb.append(move(rid))
                        .append("[-]")
                        .append("+".repeat(Integer.parseInt(op1.token)));
                break;
            }
            case INC: {
                Token r = advance();
                consumeComma();
                Token op = advance();
                int rid = rvalue(r.token);
                sb.append(move(rid))
                        .append("+".repeat(Integer.parseInt(op.token)));
                break;
            }
            case DEC: {
                Token r = advance();
                consumeComma();
                Token op = advance();
                int rid = rvalue(r.token);
                sb.append(move(rid))
                        .append("-".repeat(Integer.parseInt(op.token)));
                break;
            }
            case ADD: {
                Token r1 = advance();
                consumeComma();
                Token r2 = advance();
                consumeComma();
                Token r3 = advance();
                int rid1 = rvalue(r1.token);
                int rid2 = rvalue(r2.token);
                int rid3 = rvalue(r3.token);
                // avoid adding duplicates
                if (rid1 == rid3) {
                    sb.append(move(rid2))
                            .append("[-")
                            .append(move(rid3))
                            .append("+")
                            .append(move(rid2))
                            .append("]")
                            .append(move(rid3));
                    break;
                }
                if (rid2 == rid3) {
                    sb.append(move(rid1))
                            .append("[-")
                            .append(move(rid3))
                            .append("+")
                            .append(move(rid1))
                            .append("]")
                            .append(move(rid3));
                    break;
                }
                // move rid1 to cr
                sb.append(moveValue(rid1, CONTROL_REG));
                // clear rid3
                sb.append(move(rid3))
                        .append("[-]")
                        .append(move(CONTROL_REG));
                // move cr to rid1 and rid3
                sb.append("[-")
                        .append(move(rid1))
                        .append("+")
                        .append(move(rid3))
                        .append("+")
                        .append(move(CONTROL_REG))
                        .append("]");
                // move rid2 to cr
                sb.append(moveValue(rid2, CONTROL_REG));
                // move cr to rid2 and rid3
                sb.append("[-")
                        .append(move(rid2))
                        .append("+")
                        .append(move(rid3))
                        .append("+")
                        .append(move(CONTROL_REG))
                        .append("]");
                break;
            }
            case PRINT: {
                Token r = advance();
                int rid = rvalue(r.token);
                sb.append(move(rid))
                        .append(".");
                break;
            }
            case INPUT: {
                Token r = advance();
                int rid = rvalue(r.token);
                sb.append(move(rid))
                        .append(",");
                break;
            }
            case SUB: {
                Token r1 = advance();
                consumeComma();
                Token r2 = advance();
                consumeComma();
                Token r3 = advance();
                consumeComma();
                int rid1 = rvalue(r1.token);
                int rid2 = rvalue(r2.token);
                int rid3 = rvalue(r3.token);
                // avoid adding duplicates
                if (rid1 == rid3) {
                    sb.append(move(rid2))
                            .append("[-")
                            .append(move(rid3))
                            .append("-")
                            .append(move(rid2))
                            .append("]")
                            .append(move(rid3));
                    break;
                }
                if (rid2 == rid3) {
                    sb.append(move(rid1))
                            .append("[-")
                            .append(move(rid3))
                            .append("-")
                            .append(move(rid1))
                            .append("]")
                            .append(move(rid3));
                    break;
                }
                // move rid1 to cr
                sb.append(moveValue(rid1, CONTROL_REG));
                // clear rid3
                sb.append(move(rid3))
                        .append("[-]")
                        .append(move(CONTROL_REG));
                // move cr to rid1 and rid3
                sb.append("[-")
                        .append(move(rid1))
                        .append("+")
                        .append(move(rid3))
                        .append("+")
                        .append(move(CONTROL_REG))
                        .append("]");
                // move rid2 to cr
                sb.append(moveValue(rid2, CONTROL_REG));
                // move cr to rid2 and -rid3
                sb.append("[-")
                        .append(move(rid2))
                        .append("+")
                        .append(move(rid3))
                        .append("-")
                        .append(move(CONTROL_REG))
                        .append("]");
                break;
            }
            case CLEAR: {
                int rid = rvalue(advance().token);
                sb.append(move(rid))
                        .append("[-]");
                break;
            }
            case COPY: {
                int rid1 = rvalue(advance().token);
                consumeComma();
                int rid2 = rvalue(advance().token);
                // move rid1 to cr
                sb.append(moveValue(rid1, CONTROL_REG));
                // move cr to rid1 and rid2
                sb.append("[-")
                        .append(move(rid1))
                        .append("+")
                        .append(move(rid2))
                        .append("+")
                        .append(move(CONTROL_REG))
                        .append("]");
                break;
            }
            case SWAP: {
                int rid1 = rvalue(advance().token);
                consumeComma();
                int rid2 = rvalue(advance().token);
                sb.append(moveValue(rid1, CONTROL_REG));
                sb.append(moveValue(rid2, rid1));
                sb.append(moveValue(CONTROL_REG, rid2));
                break;
            }
            default:
                throw new CompilerException("Unknown statement.", current);
        }
        consume(TokenType.SEMICOLON, "Expected semicolon after statement.");
        return sb.toString();
    }
    public String moveValue(int r1, int r2) {
        return move(r1) +
                "[-" +
                move(r2) +
                "+" +
                move(r1) +
                "]" +
                move(r2);
    }
    public void consumeComma() {
        consume(TokenType.COMMA, "Expected comma.");
    }
    public Token advance() {
        if (isAtEnd()) {
            throw new CompilerException("Unexpected end of input.", tokens.getLast());
        }
        return tokens.get(position++);
    }
    public boolean isAtEnd() {
        return tokens.get(position).type == TokenType.EOF;
    }
    public void consume(TokenType type, String message) {
        Token ad = advance();
        if (ad.type != type) throw new CompilerException(message, ad);
    }
    public int rvalue(String rname) {
        if (!rname.startsWith("r"))
            throw new IllegalArgumentException("Invalid register name: " + rname);
        return Integer.parseInt(rname.substring(1)) + NUM_SPECIAL_REGS;
    }
    public String move(int currentR) {
        String s = "";
        if (currentR > rpos) {
            s = ">".repeat(currentR-rpos);
        } else if (currentR < rpos) {
            s = "<".repeat(rpos-currentR);
        }
        rpos = currentR;
        return s;
    }
}
