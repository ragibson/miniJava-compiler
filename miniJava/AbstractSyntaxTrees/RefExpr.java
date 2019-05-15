/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class RefExpr extends Expression
{
    public RefExpr(Reference r, SourcePosition posn){
        super(posn);
        ref = r;
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitRefExpr(this, o);
    }

    public Reference ref;
}
