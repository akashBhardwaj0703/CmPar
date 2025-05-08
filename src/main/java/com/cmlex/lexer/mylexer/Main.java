package com.cmlex.lexer.mylexer;



import java.util.List;     

public class Main {
    public static void main(String[] args) {
        String sourceCode = """
            int main() {
                int x = 42;
                float y = 3.14;
                if (x > 0) {
                    return x;
                }
                return 0;
            }
            """;

        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
        // System.out.println("hello");
    }
}


//javac Main.java
//java Main