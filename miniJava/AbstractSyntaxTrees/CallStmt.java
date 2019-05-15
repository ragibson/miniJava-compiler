/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class CallStmt extends Statement
{
    public CallStmt(Reference m, ExprList el, SourcePosition posn){
        super(posn);
        methodRef = m;
        argList = el;
    }
    
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitCallStmt(this, o);
    }
    
    public Reference methodRef;
    public ExprList argList;
}