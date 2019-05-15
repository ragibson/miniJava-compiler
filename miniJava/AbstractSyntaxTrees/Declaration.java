/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.CodeGenerator.CompilerHint;
import miniJava.CodeGenerator.RuntimeEntityDescription;
import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Declaration extends AST {
	
	public Declaration(String name, TypeDenoter type, SourcePosition posn) {
		super(posn);
		this.name = name;
		this.type = type;
		this.description = null;
		this.compilerHint = CompilerHint.NONE;
	}
	
    public Declaration(String name, TypeDenoter type, RuntimeEntityDescription description, SourcePosition posn) {
        super(posn);
        this.name = name;
        this.type = type;
        this.description = description;
        this.compilerHint = CompilerHint.NONE;
    }
	
	public String name;
	public TypeDenoter type;
	public RuntimeEntityDescription description;
	public CompilerHint compilerHint;
}
