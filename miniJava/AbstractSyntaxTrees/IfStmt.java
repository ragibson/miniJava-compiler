/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class IfStmt extends Statement
{
    public IfStmt(Expression b, Statement t, Statement e, SourcePosition posn){
        super(posn);
        cond = b;
        thenStmt = t;
        elseStmt = e;
    }
    
    public IfStmt(Expression b, Statement t, SourcePosition posn){
        super(posn);
        cond = b;
        thenStmt = t;
        elseStmt = null;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitIfStmt(this, o);
    }
    
    public Expression cond;
    public Statement thenStmt;
    public Statement elseStmt;
}