package Compiler.Syntatic;

import java.io.IOException;
import java.util.ArrayList;

import Compiler.Lexical.*;
import Compiler.Lexical.Tokens.*;
import Compiler.Semantic.SemanticResult;
import Compiler.Semantic.SemanticResultType;
import Compiler.SymbolTable.SymbolTable;
import Compiler.Syntatic.Exceptions.*;

public class SyntaticAnalyzer{

    private Lexer lexer;
    private Token tok;
    private SymbolTable symbolTable;
    
    public SyntaticAnalyzer(Lexer lexer, SymbolTable table){
        this.lexer = lexer;
        symbolTable = table;
    }    


    public SemanticResult start(){
        tok = getToken();
        SemanticResult result = program();
        eat(Tag.EOF);
        return result;
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
    // {if stmt-list.type == ok and decl-list.type == ok return result(ok)}
    private SemanticResult program(){
        SemanticResult declListResult = null;
        SemanticResult stmtListResult;

        eat(Tag.PRG);
        eat(Tag.ID);
        if(tok.getTag() != Tag.BEG)
            declListResult = declList();
        eat(Tag.BEG);
        stmtListResult = stmtList();
        eat(Tag.END);
        eat('.');    
        
        if(stmtListResult.isError() || declListResult != null && declListResult.isError()){
            return new SemanticResult(null, getConcatErrorMessages(stmtListResult, declListResult));
        }
        return new SemanticResult(SemanticResultType.TYPE_OK);
    }

    // decl-list ::= decl ";" { decl ";"} 
    private SemanticResult declList() {
        SemanticResult result = new SemanticResult(SemanticResultType.TYPE_OK);
        do {
            SemanticResult declResult = decl();
            if(declResult.isError()) result = declResult;
            eat(';');
        } while(tok.getTag() == Tag.ID);
        return result;
    }

    // decl ::= ident-list is type
    private SemanticResult decl() {
        ArrayList<Word> identifiers = identList();
        eat(Tag.IS);
        SemanticResult typeResult = type();
        for (Word identifier : identifiers) {
            identifier.setType(typeResult.type);
        }
        return typeResult;
    }

    // ident-list ::= identifier {"," identifier}
    private ArrayList<Word> identList() {
        ArrayList<Word> identifiers = new ArrayList<>();
        identifiers.add((Word)tok);
        eat(Tag.ID);
        while(tok.getTag() == ','){
            eat(',');
            identifiers.add((Word)tok);
            eat(Tag.ID);
        }
        return identifiers;
    }

    // type ::= int | float | char
    private SemanticResult type(){
        if(tok.getTag()==Tag.INT){
            eat(tok.getTag());
            return new SemanticResult(SemanticResultType.TYPE_INT);
        } else if(tok.getTag()==Tag.FLOAT){
            eat(tok.getTag());
            return new SemanticResult(SemanticResultType.TYPE_INT);
        } else if(tok.getTag()==Tag.CHAR){
            eat(tok.getTag());
            return new SemanticResult(SemanticResultType.TYPE_INT);
        }
        return new SemanticResult(null);  
    }
    
    // stmt-list ::= stmt {";" stmt}
    private SemanticResult stmtList() {
        SemanticResult left = stmt();
        while(tok.getTag() == ';'){
            eat(';');
            SemanticResult right = stmt();
            if(left.isError() || right.isError()){
                left = new SemanticResult(null, getConcatErrorMessages(left, right));
            }
        }
        return left;
    }
    
    // stmt ::= assign-stmt | if-stmt | while-stmt | repeat-stmt
    //  | read-stmt | write-stmt
    private SemanticResult stmt() {
        if(tok.getTag() == Tag.ID)
            return assignStmt().okIfNotError();
        else if(tok.getTag() == Tag.IF)
            return ifStmt().okIfNotError();
        else if(tok.getTag() == Tag.WHILE)
            return whileStmt().okIfNotError();
        else if(tok.getTag() == Tag.REPEAT)
            return repeatStmt().okIfNotError();
        else if(tok.getTag() == Tag.READ)
            return readStmt().okIfNotError();
        else if(tok.getTag() == Tag.WRITE)
            return writeStmt().okIfNotError();
        else 
            throwUnexpected();
        return new SemanticResult(null);
    }
    
    // assign-stmt ::= identifier "=" simple_expr
    private SemanticResult assignStmt() {
        Word old = (Word) tok;
        eat(Tag.ID);
        SemanticResult resultId = new SemanticResult(old.getType());
        eat('=');
        SemanticResult resultExpr = simpleExpr();

        if(resultId.type == resultExpr.type || resultId.type == SemanticResultType.TYPE_FLOAT && resultExpr.type == SemanticResultType.TYPE_INT)
            return resultId.okIfNotError();
        return new SemanticResult(null, SemanticResult.getIncompatibleVariableTypesErrorMessage(Lexer.line));
    }

    // if-stmt ::=	if condition then stmt-list end-else
    private SemanticResult ifStmt() {
        eat(Tag.IF);
        SemanticResult cond = condition();
        eat(Tag.THEN);
        SemanticResult list = stmtList();
        SemanticResult endOrElse = endElse();
        if(cond.isError() || list.isError() || endOrElse.isError()){
            return new SemanticResult(null, getConcatErrorMessages(cond, list, endOrElse));
        } else return new SemanticResult(SemanticResultType.TYPE_OK);
    }
    
    // end-else	::=	end
    //   |	else stmt-list end 
    private SemanticResult endElse() {
        SemanticResult result;
        if(tok.getTag()==Tag.END){
            eat(Tag.END);
            result = new SemanticResult(SemanticResultType.TYPE_OK);
        } else if(tok.getTag()==Tag.ELSE){
            eat(Tag.ELSE);
            result = stmtList();
            eat(Tag.END);
        } else {
            result = new SemanticResult(null);
            throwUnexpected();
        }
        return result;
    }

    // condition ::= expression
    private SemanticResult condition() {
        return expression();
    }

    // repeat-stmt ::= repeat stmt-list stmt-suffix
    private SemanticResult repeatStmt() {
        eat(Tag.REPEAT);
        SemanticResult list = stmtList();
        SemanticResult suffix = stmtSuffix();
        if(list.isError() || suffix.isError()) {
            return new SemanticResult(null, getConcatErrorMessages(list, suffix));
        }
        return new SemanticResult(SemanticResultType.TYPE_OK);
    }

    // stmt-suffix ::= until condition
    private SemanticResult stmtSuffix() {
        eat(Tag.UNTIL);
        return condition();
    }
    
    // while-stmt ::= stmt-prefix stmt-list end
    private SemanticResult whileStmt() {
        SemanticResult prefix = stmtPrefix();
        SemanticResult list = stmtList();
        eat(Tag.END);        
        return new SemanticResult(null, getConcatErrorMessages(prefix, list));
    }

    // stmt-prefix ::= while condition do
    private SemanticResult stmtPrefix() {
        eat(Tag.WHILE);
        SemanticResult result = condition();
        eat(Tag.DO);
        return result.okIfNotError();
    }

    // read-stmt ::= read "(" identifier ")"
    private SemanticResult readStmt() {
        eat(Tag.READ);
        eat('(');
        Word old = (Word) tok;
        eat(Tag.ID);
        eat(')');
        return new SemanticResult(old.getType(), SemanticResult.getUndefinedVariableErrorMessage(Lexer.line, old.getLexeme()));       
    }

    // write-stmt ::= write "(" writable ")"
    private SemanticResult writeStmt() {
        eat(Tag.WRITE);
        eat('(');
        SemanticResult result = writable();
        eat(')');
        return result;
    }
    
    // writable ::= simple-expr       {writable.type = simple-expr.type}
    //            | literal           {writable.type = literal.type}
    private SemanticResult writable() {
        if(tok.getTag()==Tag.STRING_CONST) {
            eat(Tag.STRING_CONST);
            return new SemanticResult(SemanticResultType.TYPE_OK);
        } else {
            SemanticResult result = simpleExpr();
            return result.okIfNotError();
        }
    }
    
    // expression ::= simple-expr { relop simple-expr }
    //      {   result = true
    //          foreach simple-expr then if isNotNumber(simple-expr) then result = false end end
    //          expression.type = result ? ok : error
    //      }
    private SemanticResult expression() {
        SemanticResult s1 = simpleExpr();
        while(tok.getTag() == Tag.EQ ||
            tok.getTag() == '>' ||
            tok.getTag() == Tag.GE ||
            tok.getTag() == '<' ||
            tok.getTag() == Tag.LE ||
            tok.getTag() == Tag.NE) {
        
            relop();
            SemanticResult s2 = simpleExpr();

            if(!s1.isNumericOrChar() || !s2.isNumericOrChar()){
                s1.type = SemanticResultType.TYPE_ERROR;
            }

        }
        
        return s1;
    }
    
    // simple-expr ::= term | simple-expr addop term
    private SemanticResult simpleExpr() {
        SemanticResult t1 = term();
        while(tok.getTag() == '+' || tok.getTag() == '-' || tok.getTag() == Tag.OR){
            addop();
            SemanticResult t2 = term();

            if(!t1.isNumericOrChar() || !t2.isNumericOrChar()){
                t1.type = SemanticResultType.TYPE_ERROR;
            }
        }
        return t1;
    }
    
    // term ::= factor-a                {term.type = factor-a.type}
    //        | term1 mulop factor-a
    //      {if isNotNumber(term1) or isNotNumber(factor-a) then term.type == error end 
    //       if mulop.type == div then term.type = float end
    //       if term1.type == float or factor-a.type == float then term.type = float end
    //       term.type = int  
    //       }
    private SemanticResult term() {
        SemanticResult left = factorA();
        while(tok.getTag() == '*' || tok.getTag() == '/' || tok.getTag() == Tag.AND){
            SemanticResult op = mulop();
            SemanticResult right = factorA();

            if(!left.isNumericOrChar() || !right.isNumericOrChar()) {
                left.type = SemanticResultType.TYPE_ERROR;
            }
            else if(op.type == SemanticResultType.TYPE_DIV || left.type == SemanticResultType.TYPE_FLOAT || right.type == SemanticResultType.TYPE_FLOAT){
                left.type = SemanticResultType.TYPE_FLOAT;
            } else {
                left.type = SemanticResultType.TYPE_INT;
            }
        }
        return left;
    }
    
    // factor-a ::= factor | "!" factor | "-" factor        
    //          {if isNumericOrChar(factor) then factor-a.type = factor.type else factor-a.type = error end}
    private SemanticResult factorA() {
        if(tok.getTag() == '!')
            eat('!');
        else if(tok.getTag() == '-')
            eat('-');    
        SemanticResult result = factor();
        if(result.isNumericOrChar()) {
            return result;
        } else {
            return new SemanticResult(null, SemanticResult.getExpectedNumericErrorMessage(Lexer.line));
        }

    }
    
    // factor ::= identifier                {factor.type = identifier.type}
    //          | constant                  {factor.type = constant.type}
    //          | "(" expression ")"        {factor.type = experssion.type}
    private SemanticResult factor() {
        SemanticResult result;
        if(tok.getTag()==Tag.ID){
            Word old = (Word) tok;
            eat(Tag.ID);
            result = new SemanticResult(old.getType());
        } else if(tok.getTag()=='('){
            eat('(');
            result = expression();
            if(!result.isNumericOrChar()) result = new SemanticResult(null, SemanticResult.getUnexpectedTypeErrorMessage(Lexer.line));
            eat(')');
        } else {
            result = constant();
        }
        return result;
    }
    
    // relop ::= "==" | ">" | ">=" | "<" | "<=" | "!="       {relop.type = ok}
    private SemanticResult relop() {
        if(tok.getTag() == Tag.EQ ||
        tok.getTag() == '>' ||
        tok.getTag() == Tag.GE ||
        tok.getTag() == '<' ||
        tok.getTag() == Tag.LE ||
        tok.getTag() == Tag.NE){
            advance();
            return new SemanticResult(SemanticResultType.TYPE_OK);
        }else {
            throwUnexpected();
        }
        return new SemanticResult(null);
    }
    
    // addop ::= "+"       {addop.type = ok} 
    //         | "-"       {addop.type = ok}
    //         | "||"      {addop.type = ok}
    private SemanticResult addop() {
        if(tok.getTag() == '+' ||
        tok.getTag() == '-' ||
        tok.getTag() == Tag.OR){
            advance();
            return new SemanticResult(SemanticResultType.TYPE_OK);
        }else {
            throwUnexpected();
        }
        return new SemanticResult(null);
    }
    
    // mulop ::= "*"        {mulop.type = mul} 
    //         | "/"        {mulop.type = div}
    //         | "&&"       {mulop.type = and}
    private SemanticResult mulop(){
       switch(tok.getTag()) {
            case '*':
                advance();
                return new SemanticResult(SemanticResultType.TYPE_MUL);
            case '/':
                advance();
                return new SemanticResult(SemanticResultType.TYPE_DIV);
            case Tag.AND:
                advance();
                return new SemanticResult(SemanticResultType.TYPE_AND);
            default:
                throwUnexpected();
        }
        return new SemanticResult(null);
    }

    // constant ::= integer_const       {constant.type = int}
    //            | float_const         {constant.type = float}
    //            | char_const          {constant.type = char}
    private SemanticResult constant(){
        switch(tok.getTag()) {
            case Tag.INT_CONST:
                advance();
                return new SemanticResult(SemanticResultType.TYPE_INT);
            case Tag.FLOAT_CONST:
                advance();
                return new SemanticResult(SemanticResultType.TYPE_FLOAT);
            case Tag.CHAR_CONST:
                advance();
                return new SemanticResult(SemanticResultType.TYPE_CHAR);
            default:
                throwUnexpected();
        }
        return new SemanticResult(null);
    }

    private String getConcatErrorMessages(SemanticResult res1, SemanticResult res2) {
        if(res1.isError() && res2. isError())
            return res1.message + "\n" + res2.message;
        else if (res1.isError()) return res1.message;
        else if (res2.isError()) return res2.message;
        else return "";
    }

    private String getConcatErrorMessages(SemanticResult res1, SemanticResult res2, SemanticResult res3) {
        String message = getConcatErrorMessages(res1, res2);
        if(res3.isError())
            return message + "\n" + res3.message;
        else return res3.message;
    }
}