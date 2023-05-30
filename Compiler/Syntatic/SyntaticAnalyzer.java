package Compiler.Syntatic;

import java.io.IOException;

import Compiler.Lexical.*;
import Compiler.Lexical.Tokens.*;
import Compiler.Syntatic.Exceptions.*;

public class SyntaticAnalyzer{

    private Lexer lexer;
    private Token tok;
    
    public SyntaticAnalyzer(Lexer lexer){
        this.lexer = lexer;
    }    


    public void start(){
        tok = getToken();
        program();
        eat(Tag.EOF);
    }
 
    private Token getToken(){
        try {
            tok = lexer.scan();
            if(tok.getTag() == Tag.INVALID){
                throw new InvalidTokenException((InvalidToken)tok, Lexer.line);
            }
            return tok;
        } catch (IOException e){
            throw new RuntimeException("An error ocurred while trying to read from the file");
        }

    }

    private void eat(int tag){
        if(tag != Tag.EOF && tok.getTag() == Tag.EOF){
            throw new UnexpectedEOFException();
        }
        if(tag == tok.getTag())
            advance();
        else
            throw new UnexpectedTokenException(tok, tag, Lexer.line);
    }

    private void advance(){
        tok = getToken();
    }

    private void throwUnexpected(){
        throw new UnexpectedTokenException(tok, Lexer.line);
    }

    // Each method bellow implements one of the grammar's rules with all of its productions

    // program ::= program identifier [decl-list] begin stmt-list end "."
    private void program(){
        eat(Tag.PRG);
        eat(Tag.ID);
        if(tok.getTag() != Tag.BEG)
            declList();
        eat(Tag.BEG);
        stmtList();
        eat(Tag.END);
        eat('.');       
    }

    // decl-list ::= decl ";" { decl ";"}
    private void declList() {
        do {
            decl();
            eat(';');
        } while(tok.getTag() == Tag.ID);
        
    }

    // decl ::= ident-list is type
    private void decl() {
        identList();
        eat(Tag.IS);
        type();
    }

    // ident-list ::= identifier {"," identifier}
    private void identList() {
        eat(Tag.ID);
        while(tok.getTag() == ','){
            eat(',');
            eat(Tag.ID);
        }
    }

    // type ::= int | float | char
    private void type(){
        if(tok.getTag()==Tag.INT || tok.getTag()==Tag.FLOAT || tok.getTag()==Tag.CHAR){
            eat(tok.getTag());
        }
    }
    
    // stmt-list ::= stmt {";" stmt}
    private void stmtList() {
        stmt();
        while(tok.getTag() == ';'){
            eat(';');
            stmt();
        }
    }
    
    // stmt ::= assign-stmt | if-stmt | while-stmt | repeat-stmt
    //  | read-stmt | write-stmt
    private void stmt() {
        if(tok.getTag() == Tag.ID)
            assignStmt();
        else if(tok.getTag() == Tag.IF)
            ifStmt();
        else if(tok.getTag() == Tag.WHILE)
            whileStmt();
        else if(tok.getTag() == Tag.REPEAT)
            repeatStmt();
        else if(tok.getTag() == Tag.READ)
            readStmt();
        else if(tok.getTag() == Tag.WRITE)
            writeStmt();
        else 
            throwUnexpected();
    }
    
    // assign-stmt ::= identifier "=" simple_expr
    private void assignStmt() {
        eat(Tag.ID);
        eat('=');
        simpleExpr();
    }

    // if-stmt ::=	if condition then stmt-list end-else
    private void ifStmt() {
        eat(Tag.IF);
        condition();
        eat(Tag.THEN);
        stmtList();
        endElse();
    }
    
    // end-else	::=	end
    //   |	else stmt-list end 
    private void endElse() {
        if(tok.getTag()==Tag.END){
            eat(Tag.END);
        } else if(tok.getTag()==Tag.ELSE){
            eat(Tag.ELSE);
            stmtList();
            eat(Tag.END);
        } else {
            throwUnexpected();
        }
    }

    // condition ::= expression
    private void condition() {
        expression();
    }

    // repeat-stmt ::= repeat stmt-list stmt-suffix
    private void repeatStmt() {
        eat(Tag.REPEAT);
        stmtList();
        stmtSuffix();
    }

    // stmt-suffix ::= until condition
    private void stmtSuffix() {
        eat(Tag.UNTIL);
        condition();
    }
    
    // while-stmt ::= stmt-prefix stmt-list end
    private void whileStmt() {
        stmtPrefix();
        stmtList();
        eat(Tag.END);
    }

    // stmt-prefix ::= while condition do
    private void stmtPrefix() {
        eat(Tag.WHILE);
        condition();
        eat(Tag.DO);
    }

    // read-stmt ::= read "(" identifier ")"
    private void readStmt() {
        eat(Tag.READ);
        eat('(');
        eat(Tag.ID);
        eat(')');
    }

    // write-stmt ::= write "(" writable ")"
    private void writeStmt() {
        eat(Tag.WRITE);
        eat('(');
        writable();
        eat(')');
    }
    
    // writable ::= simple-expr | literal
    private void writable() {
        if(tok.getTag()==Tag.STRING_CONST)
            eat(Tag.STRING_CONST);
        else{
            simpleExpr();
        }
    }
    
    // expression ::= simple-expr { relop simple-expr }
    private void expression() {
        simpleExpr();
        while(tok.getTag() == Tag.EQ ||
            tok.getTag() == '>' ||
            tok.getTag() == Tag.GE ||
            tok.getTag() == '<' ||
            tok.getTag() == Tag.LE ||
            tok.getTag() == Tag.NE) {
                relop();
                simpleExpr();
            }
    }
    
    // simple-expr ::= term | simple-expr addop term
    private void simpleExpr() {
        term();
        while(tok.getTag() == '+' || tok.getTag() == '-' || tok.getTag() == Tag.OR){
            addop();
            term();
        }
    }
    
    // term ::= factor-a | term mulop factor-a
    private void term() {
        factorA();
        while(tok.getTag() == '*' || tok.getTag() == '/' || tok.getTag() == Tag.AND){
            mulop();
            factorA();
        }
    }
    
    // factor-a ::= factor | "!" factor | "-" factor
    private void factorA() {
        if(tok.getTag() == '!')
            eat('!');
        else if(tok.getTag() == '-')
            eat('-');    
        factor();
        
    }
    
    // factor ::= identifier | constant | "(" expression ")"
    private void factor() {
        if(tok.getTag()==Tag.ID){
            eat(Tag.ID);
        } else if(tok.getTag()=='('){
            eat('(');
            expression();
            eat(')');
        } else {
            constant();
        }
    }
    
    // relop ::= "==" | ">" | ">=" | "<" | "<=" | "!="
    private void relop() {
        if(tok.getTag() == Tag.EQ ||
        tok.getTag() == '>' ||
        tok.getTag() == Tag.GE ||
        tok.getTag() == '<' ||
        tok.getTag() == Tag.LE ||
        tok.getTag() == Tag.NE){
            advance();
        }else {
            throwUnexpected();
        }
    }
    
    // addop ::= "+" | "-" | "||"
    private void addop() {
        if(tok.getTag() == '+' ||
        tok.getTag() == '-' ||
        tok.getTag() == Tag.OR){
            advance();
        }else {
            throwUnexpected();
        }
    }
    
    // mulop ::= "*" | "/" | "&&"
    private void mulop(){
        if(tok.getTag() == '*' ||
        tok.getTag() == '/' ||
        tok.getTag() == Tag.AND){
            advance();
        }else {
            throwUnexpected();
        }
    }

    // constant ::= integer_const | float_const | char_const
    private void constant(){
        if(tok.getTag() == Tag.INT_CONST ||
        tok.getTag() == Tag.FLOAT_CONST ||
        tok.getTag() == Tag.CHAR_CONST){
            advance();
        }else {
            throwUnexpected();
        }
    }

}