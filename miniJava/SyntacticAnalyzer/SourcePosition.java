package miniJava.SyntacticAnalyzer;

public class SourcePosition {
    public int lineNumber;
    public int columnNumber;
    
    public SourcePosition() {
        this.lineNumber = 1;
        this.columnNumber = 1;
    }
    
    public SourcePosition(int line, int column) {
        this.lineNumber = line;
        this.columnNumber = column;
    }
    
    public SourcePosition copy() {
        return new SourcePosition(this.lineNumber, this.columnNumber);
    }
}
