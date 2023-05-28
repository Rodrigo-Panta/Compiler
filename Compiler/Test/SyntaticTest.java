package Compiler.Test;

import Compiler.Lexical.Lexer;
import Compiler.SymbolTable.SymbolTable;
import Compiler.Syntatic.SyntaticAnalyzer;

public class SyntaticTest {
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Usage: java SyntaticTest filename");
            return;
        }
        
        try{
            SymbolTable symbolTable = new SymbolTable();
            Lexer lexer = new Lexer(args[0], symbolTable);
            SyntaticAnalyzer syntaticAnalyzer = new SyntaticAnalyzer(lexer);
            syntaticAnalyzer.start();
            System.out.println("Syntatic Analysis completed successfully!");
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
