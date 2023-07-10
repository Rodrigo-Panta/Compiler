package Compiler.Lexical.Tokens;

import Compiler.Lexical.Tag;
import Compiler.Semantic.SemanticResultType;

public class Word extends Token {

    private String lexeme = "";
    private SemanticResultType type;
    
    public static final Word and = new Word("&&", Tag.AND);
    public static final Word or = new Word("||", Tag.OR);
    public static final Word eq = new Word("==", Tag.EQ);
    public static final Word ne = new Word("!=", Tag.NE);
    public static final Word le = new Word("<=", Tag.LE);
    public static final Word ge = new Word(">=", Tag.GE);

    

    public Word(String s, int tag) {
        super(tag);
        lexeme = s;
    }

    public void setType(SemanticResultType type) {
        this.type = type; 
    }

    public SemanticResultType getType() {
        return type; 
    }

    public String toString() {
        return "" + lexeme;
    }

    public String getLexeme() {
        return lexeme;
    }
    
}
