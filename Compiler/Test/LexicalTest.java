package Compiler.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import Compiler.Lexical.*;
import Compiler.Lexical.Tokens.Token;

public class LexicalTest {
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Usage: java LexicalTest {testcases filenames}");
            return;
        }
        Lexer lexer;
        for(int i=0; i < args.length; i++){            
            try{
                lexer = new Lexer(args[i]);
                Token t = lexer.scan();
                while(t.getTag()!=Tag.EOF){
                    t = lexer.scan(); 
                    System.out.println(t.toString());
                    try{Thread.sleep(20);}catch(InterruptedException e){}
                }
            } catch(FileNotFoundException e) {
                System.out.println("File: " + args[i] + " not found.");
            } catch(IOException e){
                System.out.println("Something went wrong while reading the file: "+ args[i]);
            }
        }

    }
}
