/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Reference extends AST
{
    
    // linked to corresponding declaration node in identification
    public Declaration decl = null;
    
	public Reference(SourcePosition posn){
		super(posn);
	}

}
