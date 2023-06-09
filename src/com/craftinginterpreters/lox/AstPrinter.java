package com.craftinginterpreters.lox;

public class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize(expr.firstOperator.lexeme, expr.secondOperator.lexeme, expr.left, expr.middle, expr.right);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) { return parenthesize(expr.name.lexeme, expr); }

    @Override
    public String visitAssignExpr(Expr.Assign expr) { return parenthesize(expr.name.lexeme, expr); }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesize(String nameOne, String nameTwo, Expr exprOne, Expr exprTwo, Expr exprThree) {
        StringBuilder builder = new StringBuilder();

        builder.append("((").append(nameOne);

        builder.append(" ");
        builder.append(exprOne.accept(this));
        builder.append(" ");
        builder.append(exprTwo.accept(this));
        builder.append(") ").append(nameTwo).append(" ");
        builder.append(exprThree.accept(this));
        builder.append(")");

        return builder.toString();
    }
}
