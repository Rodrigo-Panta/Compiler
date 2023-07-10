package Compiler.Test;

import Compiler.Lexical.Lexer;
import Compiler.Semantic.SemanticResult;
import Compiler.SymbolTable.SymbolTable;
import Compiler.Syntatic.SyntaticAnalyzer;

public class SemanticTest {
    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Usage: java SyntaticTest filename");
            return;
        }
        
        try{
            SymbolTable symbolTable = new SymbolTable();
            Lexer lexer = new Lexer(args[0], symbolTable);
            SyntaticAnalyzer syntaticAnalyzer = new SyntaticAnalyzer(lexer, symbolTable);
            SemanticResult result = syntaticAnalyzer.start();
            System.out.println("Resultado: " + result.type);
            if(!result.isError())
                System.out.println("Semantic Analysis completed successfully!");
            else {
                System.out.println(result.message);
            } 
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
