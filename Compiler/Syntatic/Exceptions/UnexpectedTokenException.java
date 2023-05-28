package Compiler.Syntatic.Exceptions;

import Compiler.Lexical.Tag;
import Compiler.Lexical.Tokens.Token;

public class UnexpectedTokenException extends RuntimeException{
    public UnexpectedTokenException(Token unexpected, int expected, int line){
        super("Unexpected token: " + unexpected.toString() + " of type: " + Tag.getTagName(unexpected.getTag()) + " on line: " + line + "\nExpected: type: " + Tag.getTagName(expected));
    }

    public UnexpectedTokenException(Token unexpected, int line){
        super("Unexpected token: " + unexpected.toString() + " of type: " + Tag.getTagName(unexpected.getTag()) + " on line: " + line);
    }
}
