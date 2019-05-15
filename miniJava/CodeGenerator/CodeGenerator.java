package miniJava.CodeGenerator;

//for code generator
import mJAM.Machine;
import mJAM.Machine.Op;
import mJAM.Machine.Prim;
import mJAM.ObjectFile;

import java.util.ArrayList;
import java.util.Stack;

//to run debugger on object code
import mJAM.Disassembler;
import mJAM.Interpreter;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxRef;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.NullLiteral;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.SyntacticAnalyzer.TokenKind;

public class CodeGenerator implements Visitor<Object, Object> {

	ErrorReporter reporter;
	int localsDisplacement; // offset[LB]
	// offset[OB] handled on a per-object basis
	
	int mainCodeAddr;
	int staticFieldPushAddr;
	int localAllocationPopCount;
	int currentMethodNumParams;
	boolean runInterpreter = false;
	
	// List of (instruction address, MethodDecl) for function call patching
	ArrayList<FunctionPatch> functionPatchList;

	public CodeGenerator(ErrorReporter reporter) {
		this.reporter = reporter;
		Machine.initCodeGen();
		functionPatchList = new ArrayList<FunctionPatch>();
	}

	public void generateCode(AST ast, String fileName) {	
		staticFieldPushAddr = Machine.nextInstrAddr();
		Machine.emit(Op.PUSH, 0); // allocate space for static variables
		
		Machine.emit(Op.LOADL, 0); // array length 0
		Machine.emit(Prim.newarr); // empty String array argument
		
		mainCodeAddr = Machine.nextInstrAddr(); // record instr addr where main is called // "main" is called
		Machine.emit(Op.CALL, Machine.Reg.CB, 0); // static call main (address to be patched)
		Machine.emit(Op.HALT, 0, 0, 0); // end execution
		
		ast.visit(this, null);

		/*
		 * write code to object code file (.mJAM)
		 */
		String objectCodeFileName = fileName.replace(".java", ".mJAM");
		ObjectFile objF = new ObjectFile(objectCodeFileName);
		System.out.print("Writing object code file " + objectCodeFileName + " ... ");
		if (objF.write()) {
			reporter.reportError("***Writing object code file FAILED");
			return;
		} else
			System.out.println("SUCCEEDED");

		/****************************************************************************************
		 * The pa4 code generator should write an object file as shown above at the end
		 * of code generation and exit(0) if there are no errors.
		 * 
		 * During development of the code generator you may want to run the generated
		 * code directly after code generation using the mJAM debugger mode. You can
		 * adapt the following code for this (it assumes objectCodeFileName holds the
		 * name of the objectFile)
		 * 
		 */
		// create asm file using disassembler
		String asmCodeFileName = objectCodeFileName.replace(".mJAM", ".asm");
		System.out.print("Writing assembly file " + asmCodeFileName + " ... ");
		Disassembler d = new Disassembler(objectCodeFileName);
		if (d.disassemble()) {
			reporter.reportError("***Writing assembly file FAILED");
			return;
		} else
			System.out.println("SUCCEEDED");

		/*
		 * run code using debugger
		 * 
		 */
		
		if (runInterpreter) {
			System.out.println("Running code in debugger ... ");
			Interpreter.debug(objectCodeFileName, asmCodeFileName);
			System.out.println("*** mJAM execution completed");
		}
	}

	@Override
	public Object visitPackage(Package prog, Object arg) {
		/*
		 * 	Generate Runtime Entity Descriptions for the program's Declarations
		 * 
		 * 	ClassDecl		# nonstatic fields
		 * 	FieldDecl
		 * 		static		offset in global segment
		 * 		nonstatic	offset in instance
		 * 	MethodDecl		offset in code store of method start
		 * 	LocalDecl		offset relative to LB for the value (can be negative for parameters)
		 */
		
		int staticFieldOffset = 0;
		for (ClassDecl classDecl : prog.classDeclList) {
			int instanceFieldOffset = 0;
			for (FieldDecl fieldDecl : classDecl.fieldDeclList) {
				if (fieldDecl.isStatic) {
					fieldDecl.description = new RuntimeEntityDescription(staticFieldOffset++);
				} else {
					fieldDecl.description = new RuntimeEntityDescription(instanceFieldOffset++);
				}
			}
			
			classDecl.description = new RuntimeEntityDescription(instanceFieldOffset);
		}
		
		Machine.patch(staticFieldPushAddr, staticFieldOffset);
		Machine.patch(mainCodeAddr, Machine.nextInstrAddr());
		
		// Ensure correctness and uniqueness of main method
		int numMainMethods = 0;
		boolean malformedMain = false;
		for (ClassDecl cd : prog.classDeclList) {
			for (MethodDecl m : cd.methodDeclList) {
				if (m.name.equals("main")) {
					numMainMethods++;
					if (m.isPrivate || !m.isStatic || m.type.typeKind != TypeKind.VOID) {
						malformedMain = true;
					}

					if (m.parameterDeclList.size() == 1) {
						if (m.parameterDeclList.get(0).type instanceof ArrayType) {
							ArrayType arrayType = (ArrayType) m.parameterDeclList.get(0).type;
							if (arrayType.eltType instanceof ClassType
									&& ((ClassType) arrayType.eltType).className.spelling.equals("String")) {
								// correctly formed main method
								functionPatchList.add(new FunctionPatch(mainCodeAddr, m));
							} else {
								malformedMain = true;
							}
						} else {
							malformedMain = true;
						}
					} else {
						malformedMain = true;
					}
				}
			}
		}
		
		// add return to end of all void function and ensure return at end of all non-void functions
		for (ClassDecl cd : prog.classDeclList) {
			for (MethodDecl md : cd.methodDeclList) {
				StatementList statementList = md.statementList;
				if (statementList.size() == 0) {
					md.statementList.add(new ReturnStmt(null, md.posn));
				}
				Statement lastStatement = statementList.get(statementList.size()-1);
				if (md.type.typeKind != TypeKind.VOID) {
					if (!(lastStatement instanceof ReturnStmt)) {
						reporter.reportError("*** non-void method '" + md.name + "' does not return at end.");
					}
				} else {
					md.statementList.add(new ReturnStmt(null, lastStatement.posn));
				}
			}
		}
		
		if (numMainMethods != 1 || malformedMain) {
			reporter.reportError("*** program does not have a unique public static void main(String[] args) method.");
		}
		
		for (ClassDecl c : prog.classDeclList)
			c.visit(this, null);
		
		// final pass to patch function call addresses
		for (FunctionPatch fp : functionPatchList) {
//			System.out.println("Patching method " + fp.methodDecl.name + " at addr " + fp.codeAddr);
			Machine.patch(fp.codeAddr, fp.methodDecl.description.memoryOffset);
		}
		
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl cd, Object arg) {
		for (FieldDecl f : cd.fieldDeclList)
			f.visit(this, null);
		for (MethodDecl m : cd.methodDeclList)
			m.visit(this, null);
		return null;
	}

	@Override
	public Object visitFieldDecl(FieldDecl fd, Object arg) {
		fd.type.visit(this, null);
		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, Object arg) {	
		localsDisplacement = 3;
		currentMethodNumParams = md.parameterDeclList.size();
		
		md.type.visit(this, null);
		
		int parameterOffsetStart = -md.parameterDeclList.size();
		for (ParameterDecl p : md.parameterDeclList) {
			p.visit(this, null);
			p.description = new RuntimeEntityDescription(parameterOffsetStart++);
		}
		
		// offset in code store of method start
		md.description = new RuntimeEntityDescription(Machine.nextInstrAddr());
		
		for (Statement s : md.statementList)
			s.visit(this, null);
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, Object arg) {
		pd.type.visit(this, null);
		return null;
	}

	@Override
	public Object visitVarDecl(VarDecl decl, Object arg) {
//		System.out.println("Placing " + decl.name + " at offset " + localsDisplacement);
		decl.description = new RuntimeEntityDescription(localsDisplacement++);
		decl.type.visit(this, null);
		return null;
	}

	@Override
	public Object visitBaseType(BaseType type, Object arg) {
		return null;
	}

	@Override
	public Object visitClassType(ClassType type, Object arg) {
		type.className.visit(this, null);
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType type, Object arg) {
		type.eltType.visit(this, null);
		return null;
	}

	@Override
	public Object visitBlockStmt(BlockStmt stmt, Object arg) {
		// keep track of how many local variables were allocated
		this.localAllocationPopCount = 0;
		for (Statement s : stmt.sl)
			s.visit(this, null);
		if (this.localAllocationPopCount > 0) {
			this.localsDisplacement -= this.localAllocationPopCount;
			Machine.emit(Op.POP, this.localAllocationPopCount);
		}
		return null;
	}

	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		this.localAllocationPopCount++;
		stmt.varDecl.visit(this, null);
		stmt.initExp.visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignStmt(AssignStmt stmt, Object arg) {
		if (stmt.ref.decl.compilerHint == CompilerHint.STATIC_FIELD_ACCESS) {
			stmt.val.visit(this, null);
			Machine.emit(Op.STORE, Machine.Reg.SB, stmt.ref.decl.description.memoryOffset);
		} else if (stmt.ref instanceof IdRef) {
			IdRef idRef = (IdRef) stmt.ref;
			
			if (idRef.decl instanceof FieldDecl) {
				Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
				Machine.emit(Op.LOADL, idRef.id.decl.description.memoryOffset);
				stmt.val.visit(this, null);
				Machine.emit(Prim.fieldupd);
			} else {
				stmt.val.visit(this, null);
				storeIdRef((IdRef) stmt.ref);
			}
//			Machine.emit(Op.STORE, Machine.Reg.LB, stmt.ref.decl.description.memoryOffset);
		} else if (stmt.ref instanceof QualRef) {
			visitQRefHelper((QualRef) stmt.ref);
			stmt.val.visit(this, null);
			Machine.emit(Prim.fieldupd);
		} else if (stmt.ref instanceof IxRef) {
			IxRef ixRef = (IxRef) stmt.ref;
			ixRef.ref.visit(this, null);
			ixRef.indexExpr.visit(this, null);
			stmt.val.visit(this, null);
			Machine.emit(Prim.arrayupd);
		}
		
		return null;
	}

	@Override
	public Object visitCallStmt(CallStmt stmt, Object arg) {
		for (Expression e : stmt.argList)
			e.visit(this, null);

		if (stmt.methodRef.decl.compilerHint == CompilerHint.DEFAULT_PRINTLN) {
			assert stmt.argList.size() == 1;
			Machine.emit(Prim.putintnl);
		} else {
			int callCodeAddr = Machine.nextInstrAddr();
			if (((MethodDecl) stmt.methodRef.decl).isStatic) { // CALL
				Machine.emit(Op.CALL, Machine.Reg.CB, 0);
				functionPatchList.add(new FunctionPatch(callCodeAddr, (MethodDecl) stmt.methodRef.decl));
			} else { // CALLI
				stmt.methodRef.visit(this, null);
				if (stmt.methodRef instanceof QualRef) {
					QualRef methodQualRef = (QualRef) stmt.methodRef;
					Reference object = methodQualRef.ref;
	//				Identifier function = methodQualRef.id;
					object.visit(this, null);
				} else {
					// Implicit this.function();
					visitThisRef(null, null);
				}

//				methodQualRef.visit(this, null);
				callCodeAddr = Machine.nextInstrAddr();
				Machine.emit(Op.CALLI, Machine.Reg.CB, 0);
				functionPatchList.add(new FunctionPatch(callCodeAddr, (MethodDecl) stmt.methodRef.decl));
			}
		}
		
		// Remove non-void return results from the stack if not assigned to variables
		if (stmt.methodRef.decl.type.typeKind != TypeKind.VOID) {
			Machine.emit(Op.POP, 1);
		}
		return null;
	}

	@Override
	public Object visitReturnStmt(ReturnStmt stmt, Object arg) {
		if (stmt.returnExpr != null)
			stmt.returnExpr.visit(this, null);
		Machine.emit(Op.RETURN, stmt.returnExpr == null ? 0 : 1, 0, currentMethodNumParams);
		return null;
	}

	@Override
	public Object visitIfStmt(IfStmt stmt, Object arg) {
		stmt.cond.visit(this, null);

		int codeAddrJump1 = Machine.nextInstrAddr();
		Machine.emit(Op.JUMPIF, 0, Machine.Reg.CB, 0);
		stmt.thenStmt.visit(this, null);
		int codeAddrJump2 = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, 0, Machine.Reg.CB, 0);
		Machine.patch(codeAddrJump1, Machine.nextInstrAddr());

		if (stmt.elseStmt != null) {
			stmt.elseStmt.visit(this, null);
		}

		Machine.patch(codeAddrJump2, Machine.nextInstrAddr());

		return null;
	}

	@Override
	public Object visitWhileStmt(WhileStmt stmt, Object arg) {
		int codeAddrJump1 = Machine.nextInstrAddr();
		Machine.emit(Op.JUMP, 0, Machine.Reg.CB, 0);
		stmt.body.visit(this, null);

		int codeAddrJump2 = Machine.nextInstrAddr();
		stmt.cond.visit(this, null);
		Machine.emit(Op.JUMPIF, 1, Machine.Reg.CB, codeAddrJump1 + 1);

		Machine.patch(codeAddrJump1, codeAddrJump2);

		return null;
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
		if (expr.operator.kind == TokenKind.MINUS) {
			// convert unary -x into 0-x
			Machine.emit(Op.LOADL, 0);
		}

		expr.expr.visit(this, null);
		expr.operator.visit(this, null);
		return null;
	}

	@Override
	public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
		if (expr.operator.kind == TokenKind.AND) {
			// short-circuiting AND
			expr.left.visit(this, null);
			Machine.emit(Op.LOAD, Machine.Reg.ST, -1);
			int codeAddrSkipSecond = Machine.nextInstrAddr();
			Machine.emit(Op.JUMPIF, 0, Machine.Reg.CB, 0);
			expr.right.visit(this, null);
			expr.operator.visit(this, null);
			Machine.patch(codeAddrSkipSecond, Machine.nextInstrAddr());
		} else if (expr.operator.kind == TokenKind.OR) {
			// short-circuiting OR
			expr.left.visit(this, null);
			Machine.emit(Op.LOAD, Machine.Reg.ST, -1);
			int codeAddrSkipSecond = Machine.nextInstrAddr();
			Machine.emit(Op.JUMPIF, 1, Machine.Reg.CB, 0);
			expr.right.visit(this, null);
			expr.operator.visit(this, null);
			Machine.patch(codeAddrSkipSecond, Machine.nextInstrAddr());
		} else {
			expr.left.visit(this, null);
			expr.right.visit(this, null);
			expr.operator.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitRefExpr(RefExpr expr, Object arg) {
		if (expr.ref.decl.compilerHint == CompilerHint.STATIC_FIELD_ACCESS) {
			Machine.emit(Op.LOAD, Machine.Reg.SB, expr.ref.decl.description.memoryOffset);
		} else if (expr.ref instanceof IdRef) {
			expr.ref.visit(this, null);
		} else if (expr.ref instanceof QualRef) {
			expr.ref.visit(this, null);
		} else if (expr.ref instanceof IxRef) {
			IxRef ixRef = (IxRef) expr.ref;
			ixRef.ref.visit(this, null);
			ixRef.indexExpr.visit(this, null);
			Machine.emit(Prim.arrayref);
		} else if (expr.ref instanceof ThisRef) {
			Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
		}

		return null;
	}

	@Override
	public Object visitCallExpr(CallExpr expr, Object arg) {
		for (Expression e : expr.argList)
			e.visit(this, null);
		
		if (expr.functionRef.decl.compilerHint != CompilerHint.DEFAULT_PRINTLN) {			
			expr.functionRef.visit(this, null);
			int callCodeAddr = Machine.nextInstrAddr();
			if (((MethodDecl) expr.functionRef.decl).isStatic) { // CALL
				Machine.emit(Op.CALL, Machine.Reg.CB, 0);
				functionPatchList.add(new FunctionPatch(callCodeAddr, (MethodDecl) expr.functionRef.decl));
			} else { // CALLI
				// reload OB
				if (expr.functionRef instanceof QualRef) {
					QualRef qualRef = (QualRef) expr.functionRef;
					qualRef.ref.visit(this, null);
				} else {
					Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
				}
				callCodeAddr = Machine.nextInstrAddr();
				Machine.emit(Op.CALLI, Machine.Reg.CB, 0);
				functionPatchList.add(new FunctionPatch(callCodeAddr, (MethodDecl) expr.functionRef.decl));
			}
		}
		
		return null;
	}

	@Override
	public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
		expr.lit.visit(this, null);
		return null;
	}

	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
//		expr.classtype.visit(this, null);
		Machine.emit(Op.LOADL, -1);
		Machine.emit(Op.LOADL, expr.classtype.className.decl.description.memoryOffset);
		Machine.emit(Prim.newobj);
		return null;
	}

	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
//		expr.eltType.visit(this, null);
		expr.sizeExpr.visit(this, null);
		Machine.emit(Prim.newarr);
		return null;
	}

	@Override
	public Object visitThisRef(ThisRef ref, Object arg) {
		Machine.emit(Op.LOADA, Machine.Reg.OB, 0);
		return null;
	}

	public void loadIdRef(IdRef ref) {
		if (ref.decl instanceof FieldDecl) {
			FieldDecl fieldDecl = (FieldDecl) ref.decl;
			if (fieldDecl.isStatic) {
				Machine.emit(Op.LOAD, Machine.Reg.SB, ref.id.decl.description.memoryOffset);
			} else {
				Machine.emit(Op.LOAD, Machine.Reg.OB, ref.id.decl.description.memoryOffset);
			}
		} else if (ref.id.decl.description != null) {
			if (ref.id.decl.compilerHint == CompilerHint.STATIC_FIELD_ACCESS) {
				Machine.emit(Op.LOAD, Machine.Reg.SB, ref.id.decl.description.memoryOffset);
			} else if (!(ref.id.decl instanceof MethodDecl)) {
				Machine.emit(Op.LOAD, Machine.Reg.LB, ref.id.decl.description.memoryOffset);
			}
		}
	}

	public void storeIdRef(IdRef ref) {
		if (ref.decl instanceof FieldDecl) {
			FieldDecl fieldDecl = (FieldDecl) ref.decl;
			if (fieldDecl.isStatic) {
				Machine.emit(Op.STORE, Machine.Reg.SB, ref.id.decl.description.memoryOffset);
			} else {

			}
		} else {
			if (ref.id.decl.compilerHint == CompilerHint.STATIC_FIELD_ACCESS) {
				Machine.emit(Op.STORE, Machine.Reg.SB, ref.id.decl.description.memoryOffset);
			} else {
				Machine.emit(Op.STORE, Machine.Reg.LB, ref.id.decl.description.memoryOffset);
			}
		}
	}
	
	@Override
	public Object visitIdRef(IdRef ref, Object arg) {
		loadIdRef(ref);
		return null;
	}
	
	public void visitQRefHelper(QualRef ref) {		
		// emits code that puts the final address and field index on the stack
		// I.e. calling this method and then emitting fieldref will put the value of the
		// QualRef on the stack
		if (ref.id.decl.description != null) {
			Stack<Integer> fieldOffsetStack = new Stack<Integer>();

			fieldOffsetStack.push(ref.id.decl.description.memoryOffset);
//			System.out.println("visitQRefHelper pushed " + fieldOffsetStack.peek() + " for reference " + ref.id.spelling);
			while (ref.ref instanceof QualRef) {
				ref = (QualRef) ref.ref;
				fieldOffsetStack.push(ref.decl.description.memoryOffset);
//				System.out.println("visitQRefHelper pushed " + fieldOffsetStack.peek() + " for reference " + ref.id.spelling);
			}

			ref.ref.visit(this, null);
//			Machine.emit(Op.LOAD, Machine.Reg.LB, ref.ref.decl.description.memoryOffset);
			
			int stackSize = fieldOffsetStack.size();
			for (int i = 0; i < stackSize; i++) {
				int fieldOffset = fieldOffsetStack.pop();
				Machine.emit(Op.LOADL, fieldOffset);
//				System.out.println("visitQRefHelper emitted LOADL " + fieldOffset);
				if (i+1 < stackSize) {
					Machine.emit(Prim.fieldref);
				}
			}
		}
	}
	
	@Override
	public Object visitQRef(QualRef ref, Object arg) {
		if (ref.id.decl.compilerHint == CompilerHint.ARRAY_LENGTH) {
			loadIdRef((IdRef) ref.ref);
//			Machine.emit(Op.LOAD, Machine.Reg.LB, ref.ref.decl.description.memoryOffset);
			Machine.emit(Prim.arraylen);
		} else if (ref.id.decl.description != null) {
			visitQRefHelper(ref);
			Machine.emit(Prim.fieldref);
		}
		return null;
	}

	@Override
	public Object visitIxRef(IxRef ref, Object arg) {
		return null;
	}

	@Override
	public Object visitIdentifier(Identifier id, Object arg) {
		return null;
	}

	@Override
	public Object visitOperator(Operator op, Object arg) {
		switch (op.kind) {
		case LESSTHAN:
			Machine.emit(Prim.lt);
			break;
		case GREATERTHAN:
			Machine.emit(Prim.gt);
			break;
		case EQUALS:
			Machine.emit(Prim.eq);
			break;
		case LESSEQUAL:
			Machine.emit(Prim.le);
			break;
		case GREATEREQUAL:
			Machine.emit(Prim.ge);
			break;
		case NOTEQUAL:
			Machine.emit(Prim.ne);
			break;
		case AND:
			Machine.emit(Prim.and);
			break;
		case OR:
			Machine.emit(Prim.or);
			break;
		case PLUS:
			Machine.emit(Prim.add);
			break;
		case MINUS:
			Machine.emit(Prim.sub);
			break;
		case TIMES:
			Machine.emit(Prim.mult);
			break;
		case DIVIDE:
			Machine.emit(Prim.div);
			break;
		case NOT:
			Machine.emit(Prim.neg);
			break;
		default:
			reporter.reportError("*** Encountered unknown operator " + op.kind + " in code generation.");
			break;
		}
		return null;
	}

	@Override
	public Object visitIntLiteral(IntLiteral num, Object arg) {
		int intValue = Integer.parseInt(num.spelling);
		Machine.emit(Op.LOADL, intValue);
		return null;
	}

	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		if (bool.spelling.equals("true")) {
			Machine.emit(Op.LOADL, Machine.trueRep);
		} else if (bool.spelling.equals("false")) {
			Machine.emit(Op.LOADL, Machine.falseRep);
		}
		return null;
	}

	@Override
	public Object visitNullLiteral(NullLiteral nullLit, Object arg) {
		Machine.emit(Op.LOADL, Machine.nullRep);
		return null;
	}

}
