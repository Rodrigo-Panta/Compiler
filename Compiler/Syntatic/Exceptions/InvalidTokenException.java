package Compiler.Syntatic.Exceptions;

import Compiler.Lexical.Tokens.InvalidToken;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(InvalidToken invalid, int line){
        super("Invalid token " + invalid.toString() + " on line " + line);
    }
}
