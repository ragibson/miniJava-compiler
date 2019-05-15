/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class AssignStmt extends Statement
{
    public AssignStmt(Reference r, Expression e, SourcePosition posn){
        super(posn);
        ref = r;
        val = e;
    }
    
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitAssignStmt(this, o);
    }
    
    public Reference ref;
    public Expression val;
}