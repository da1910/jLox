package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        Expr expr = ternary();
        while (match(COMMA)) {
            Token operator = previous();
            Expr right = ternary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = equality();

        while (match(QUESTION)) {
            Token operatorOne = previous();
            Expr middle = ternary();
            Token operatorTwo = consume(COLON, "Expected ':' in expression.");
            Expr right = ternary();
            expr = new Expr.Ternary(expr, operatorOne, middle, operatorTwo, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr;
        try {
            expr = comparison();
        } catch (ParseError parseError) {
            if (match(BANG_EQUAL, EQUAL_EQUAL)) {
                Token operator = previous();
                Expr right = comparison();
                throw error(operator, "Binary operator encountered with no left operand.");
            }
            throw parseError;
        }

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr;
        try {
            expr = term();
        } catch (ParseError parseError) {
            if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
                Token operator = previous();
                Expr right = term();
                throw error(operator, "Binary operator encountered with no left operand.");
            }
            throw parseError;
        }

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr;
        try {
            expr = factor();
        } catch (ParseError parseError) {
            if (match(MINUS, PLUS)) {
                Token operator = previous();
                Expr right = factor();
                throw error(operator, "Binary operator encountered with no left operand.");
            }
            throw parseError;
        }

        while(match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr;
        try {
            expr = unary();
        } catch (ParseError parseError) {
            if (match(SLASH, STAR)) {
                Token operator = previous();
                Expr right = unary();
                throw error(operator, "Binary operator encountered with no left operand.");
            }
            throw parseError;
        }

        while(match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE -> {
                    return;
                }
            }

            advance();
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

}
