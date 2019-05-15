/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;

public class Identifier extends Terminal {

    // linked to corresponding declaration node in identification
    public Declaration decl = null;

    public Identifier(Token t, SourcePosition posn) {
        super(t);
        this.posn = posn;
    }

    public void linkDeclaration(Declaration decl) {
        this.decl = decl;
    }

    public <A, R> R visit(Visitor<A, R> v, A o) {
        return v.visitIdentifier(this, o);
    }

}
