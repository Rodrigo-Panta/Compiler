package Compiler.Syntatic.Exceptions;

public class UnexpectedEOFException extends RuntimeException{
    public UnexpectedEOFException(){
        super("Unexpected END of file");
    }
    
}
