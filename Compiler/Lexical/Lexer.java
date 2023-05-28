package Compiler.Lexical;

import java.io.*;

import Compiler.Lexical.Tokens.InvalidToken;
import Compiler.Lexical.Tokens.Token;
import Compiler.Lexical.Tokens.Word;
import Compiler.Lexical.Tokens.Char.*;
import Compiler.Lexical.Tokens.Num.*;
import Compiler.SymbolTable.SymbolTable;


public class Lexer {

    public static int line = 1; //contador de linhas
    private char ch = ' '; //caractere lido do arquivo
    private FileReader file;
    private SymbolTable symbolTable;

    /* Método construtor */
    public Lexer(String fileName, SymbolTable symbolTable) throws FileNotFoundException {
        try {
            file = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            throw e;
        }
        this.symbolTable = symbolTable;
    }

    /*Lê o próximo caractere do arquivo*/
    private void readch() throws IOException {
        ch = (char) file.read();
    }

    /* Lê o próximo caractere do arquivo e verifica se é igual a c*/
    private boolean readch(char c) throws IOException {
        readch();
        if (ch != c) {
            return false;
        }
        ch = ' ';
        return true;
    }

    public Token scan() throws IOException {
        //Desconsidera delimitadores e comentários na entrada
        for (;; readch()) {
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\b') {
                continue;
            } else if (ch == '\n') {
                line++; //conta linhas
            } else      
            //Procura por comentários
            if(ch=='/'){
                if(readch('*')){
                    while(true){
                        while(!readch('*')){
                            if(ch=='\n'){
                                line++;
                            }
                            // End of File
                            if(ch==(char)-1){
                                return new Token(Tag.EOF);
                            }
                        }
                        if(readch('/')) {
                            break;
                        };
                    }
                } else {
                    return new Token('/');
                }
            } else {
                break;
            }
    
        }
        
        switch (ch) {
            //Operadores
            case '&':
                if (readch('&')) {
                    return Word.and;
                } else {
                    return new InvalidToken("&");
                }
            case '|':
                if (readch('|')) {
                    return Word.or;
                } else {
                    return new InvalidToken("|");
                }
            case '=':
                if (readch('=')) {
                    return Word.eq;
                } else {
                    return new Token('=');
                }
            case '<':
                if (readch('=')) {
                    return Word.le;
                } else {
                    return new Token('<');
                }
            case '>':
                if (readch('=')) {
                    return Word.ge;
                } else {
                    return new Token('>');
                }
            case '!':
                if (readch('=')) {
                    return Word.ne;
                } else {
                    return new Token('!');
                }
        }

        //Tokens de um único caractere
        if( ch=='+'||ch=='-'||ch=='*'||ch=='.'||ch==','||
            ch==';'||ch=='('||ch==')') {
                char character = ch;
                readch();
                return new Token(character);
            } 

        //Números
        if (Character.isDigit(ch)) {
            StringBuilder value = new StringBuilder();
            boolean semDecimal = true;
            do {
                if(ch == '.'){
                    semDecimal = false;
                    value.append(ch);
                    readch();
                    if(!Character.isDigit(ch)){
                        return new InvalidToken(value.toString());
                    }
                }
                value.append(ch);
                readch();
            } while (Character.isDigit(ch)|| (ch == '.' && semDecimal));
            if(semDecimal){
                return new IntConst(Long.parseLong(value.toString()));
            }
            return new FloatConst(Double.parseDouble(value.toString()));
        }

        //Identificadores
        if (Character.isLetter(ch)) {
            StringBuffer sb = new StringBuffer();
            do {
                sb.append(ch);
                readch();
            } while (Character.isLetterOrDigit(ch) || ch == '_');
            String s = sb.toString();
            Word w =  symbolTable.get(s);
            if (w != null) {
                return w; //palavra já existe na HashTable
            }
            w = new Word(s, Tag.ID);
            symbolTable.put(s, w);
            return w;
        }

        // Caracteres
        if(ch=='\''){
            readch();
            char character = ch;
            if(readch('\'')){
                return new CharConst(character);
            } 
            String invalid = "'" + character + ch;
            return new InvalidToken(invalid);
        }

        // Strings literais
        if(ch=='{'){
            StringBuffer sb = new StringBuffer();
            readch();
            while(true){
                if(ch=='\n'){
                    sb.append(ch);
                    return new InvalidToken(sb.toString());
                }
                // End of File
                if(ch==(char)-1){
                    return new InvalidToken(sb.toString());
                }
                sb.append(ch);
                if(readch('}')) {
                    return new StringConst(sb.toString());
                };
            }
        }

        // End of File
        if(ch==(char)-1){
            return new Token(Tag.EOF);
        }

        //Caracteres não especificados
        Token t = new InvalidToken(""+ch);
        ch = ' ';
        return t;
    }
}
