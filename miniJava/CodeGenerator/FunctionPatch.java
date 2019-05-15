package miniJava.CodeGenerator;

import miniJava.AbstractSyntaxTrees.MethodDecl;

public class FunctionPatch {
	int codeAddr;
	MethodDecl methodDecl;

	public FunctionPatch(int codeAddr, MethodDecl methodDecl) {
		this.codeAddr = codeAddr;
		this.methodDecl = methodDecl;
	}
}
