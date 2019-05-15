/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class IxRef extends Reference {
	
	public IxRef(Reference ref, Expression expr, SourcePosition posn){
		super(posn);
		this.ref = ref;
		this.indexExpr = expr;
	}

	public <A,R> R visit(Visitor<A,R> v, A o){
		return v.visitIxRef(this, o);
	}
	
	public Reference ref;
	public Expression indexExpr;

}
