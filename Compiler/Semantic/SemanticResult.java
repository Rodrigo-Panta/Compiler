package Compiler.Semantic;

public class SemanticResult {
    public SemanticResultType type;
    public String message;

    public SemanticResult (SemanticResultType type, String message){
        this(type);
        this.message = message;
    }


    public SemanticResult (SemanticResultType type, int line){
        this(type);
        message = "Semantic error on line " + line;
    }

    
    public SemanticResult (SemanticResultType type){
        if(type == null) this.type = SemanticResultType.TYPE_ERROR;
        else this.type = type;
    
    }

    public boolean isNumericOrChar(){
        return type == SemanticResultType.TYPE_INT || type == SemanticResultType.TYPE_FLOAT || type == SemanticResultType.TYPE_CHAR; 
    }
    
    
    public boolean isError(){
        return type == SemanticResultType.TYPE_ERROR; 
    }

    public SemanticResult okIfNotError(){
            if(isError()) return this;
            return new SemanticResult(SemanticResultType.TYPE_OK); 
    }
    
    
    public static String getUnexpectedTypeErrorMessage(int line) {
        return "Unexpected Expression Type on line "+ line + "\nExpected type int, float or char";
    }

    public static String getExpectedNumericErrorMessage(int line) {
        return "Expected a numeric type expression on line "+ line+".";
    }

    public static String getUndefinedVariableErrorMessage(int line, String varName) {
        return "Undefined variable " + varName + " on line "+ line+".";
    }

    
    public static String getIncompatibleVariableTypesErrorMessage(int line) {
        return "Incompatible variable types on " + " on line "+ line+".";
    }
}
