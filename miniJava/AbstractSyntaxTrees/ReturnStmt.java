/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ReturnStmt extends Statement 
{
	public ReturnStmt(Expression e, SourcePosition posn){
		super(posn);
		returnExpr = e;
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
		return v.visitReturnStmt(this, o);
	}

	public Expression returnExpr;
}	
