package com.cmlex.lexer.mylexer;


import java.util.*;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Keywords lookup table
    private static final Map<String, TokenType> keywords = new HashMap<>();
    static {
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("char", TokenType.CHAR);
        keywords.put("void", TokenType.VOID);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("return", TokenType.RETURN);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("struct", TokenType.STRUCT);
        keywords.put("typedef", TokenType.TYPEDEF);
        keywords.put("enum", TokenType.ENUM);
        keywords.put("static", TokenType.STATIC);
        keywords.put("extern", TokenType.EXTERN);
        keywords.put("const", TokenType.CONST);
        keywords.put("volatile", TokenType.VOLATILE);
        keywords.put("sizeof", TokenType.SIZEOF);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single-character tokens
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case '[': addToken(TokenType.LBRACKET); break;
            case ']': addToken(TokenType.RBRACKET); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;

            // Operators (could be multi-character)
            case '+':
                addToken(match('+') ? TokenType.PLUS_PLUS : TokenType.PLUS);
                break;
            case '-':
                if (match('>')) addToken(TokenType.ARROW);
                else if (match('-')) addToken(TokenType.MINUS_MINUS);
                else addToken(TokenType.MINUS);
                break;
            case '*': addToken(TokenType.STAR); break;
            case '/':
                if (match('/')) {
                    // Skip line comment
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // Skip block comment
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    if (isAtEnd()) {
                        System.err.println("Unterminated block comment at line " + line);
                        return;
                    }
                    advance(); // Consume '*'
                    advance(); // Consume '/'
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case '%': addToken(TokenType.PERCENT); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '<':
                if (match('=')) addToken(TokenType.LESS_EQUAL);
                else if (match('<')) addToken(TokenType.LESS_LESS);
                else addToken(TokenType.LESS);
                break;
            case '>':
                if (match('=')) addToken(TokenType.GREATER_EQUAL);
                else if (match('>')) addToken(TokenType.GREATER_GREATER);
                else addToken(TokenType.GREATER);
                break;
            case '&':
                addToken(match('&') ? TokenType.AND_AND : TokenType.AMPERSAND);
                break;
            case '|':
                addToken(match('|') ? TokenType.OR_OR : TokenType.PIPE);
                break;
            case '^': addToken(TokenType.CARET); break;
            case '~': addToken(TokenType.TILDE); break;

            // Whitespace
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            // String literals
            case '"': string(); break;
            case '\'': character(); break;

            // Numbers and identifiers
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    System.err.println("Unexpected character: " + c + " at line " + line);
                }
                break;
        }
    }

    // Helper methods
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, line));
    }

    // Handle string literals
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            System.err.println("Unterminated string at line " + line);
            return;
        }
        advance(); // Consume closing "
        // String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING);
    }

    // Handle character literals
    private void character() {
        while (peek() != '\'' && !isAtEnd()) {
            advance();
        }
        if (isAtEnd()) {
            System.err.println("Unterminated character at line " + line);
            return;
        }
        advance(); // Consume closing '
        // String value = source.substring(start + 1, current - 1);
        addToken(TokenType.CHARACTER);
    }

    // Handle numbers (integers and floats)
    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // Consume '.'
            while (isDigit(peek())) advance();
            addToken(TokenType.FLOAT_LITERAL);
        } else {
            addToken(TokenType.INTEGER);
        }
    }

    // Handle identifiers and keywords
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }
}