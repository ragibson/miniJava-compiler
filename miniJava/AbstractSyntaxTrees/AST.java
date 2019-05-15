/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class AST {

    TypeDenoter type = null;

    public AST(SourcePosition posn) {
        this.posn = posn;
        
        if (posn == null) {
        	this.posn = new SourcePosition();
        }
    }

    public String toString() {
        String fullClassName = this.getClass().getName();
        String cn = fullClassName.substring(1 + fullClassName.lastIndexOf('.'));
        if (ASTDisplay.showPosition)
            cn = cn + " " + posn.toString();
        return cn;
    }

    public abstract <A, R> R visit(Visitor<A, R> v, A o);

    public SourcePosition posn;
}
