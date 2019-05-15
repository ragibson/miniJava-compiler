package miniJava.ContextualAnalyzer;

import java.util.HashMap;

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
import miniJava.AbstractSyntaxTrees.Declaration;
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
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.CodeGenerator.CompilerHint;

public class Identification implements Visitor<Object, Object> {

    public IdentificationTable table;
    ErrorReporter reporter;
    String forbiddenVariableName;

    public Identification(ErrorReporter reporter) {
        this.reporter = reporter;
    }

    public void linkDeclarationsAndReferences(AST ast) {
        table = new IdentificationTable(reporter);

        /*
         * add predefined names:
         *   class System { public static _PrintStream out; }
         *   class _PrintStream { public void println(int n){}; }
         *   class String { }
         */
        table.classes.put("System", table.retrieve("System"));
        table.classes.put("_PrintStream", table.retrieve("_PrintStream"));
        table.classes.put("String", table.retrieve("String"));

        table.classFields.put("System", new HashMap<String, Declaration>());
        table.classFields.get("System").put("out", ((ClassDecl) table.retrieve("System")).fieldDeclList.get(0));

        table.classFields.put("_PrintStream", new HashMap<String, Declaration>());
        table.classFields.get("_PrintStream").put("println",
                ((ClassDecl) table.retrieve("_PrintStream")).methodDeclList.get(0));
        
        ast.visit(this, null);
    }

    @Override
    public Object visitPackage(Package prog, Object arg) {
        // initial pass to find all classes and class members
        for (ClassDecl classDecl : prog.classDeclList) {            
        	if (table.retrieveClass(classDecl.name) != null) {
        		reporter.reportError("*** line " + classDecl.posn.lineNumber + ": duplicate declaration of class " + classDecl.name + ".");
        	}
        	
            table.classes.put(classDecl.name, classDecl);
            table.classFields.put(classDecl.name, new HashMap<String, Declaration>());
            table.classMethods.put(classDecl.name, new HashMap<String, Declaration>());

            // assume no field/method name collisions
            for (FieldDecl fieldDecl : classDecl.fieldDeclList) {
                table.classFields.get(classDecl.name).put(fieldDecl.name, fieldDecl);
            }

            for (MethodDecl methodDecl : classDecl.methodDeclList) {
                table.classFields.get(classDecl.name).put(methodDecl.name, methodDecl);
                table.classMethods.get(classDecl.name).put(methodDecl.name, methodDecl);
            }
        }

        // actual identification pass
        for (ClassDecl classDecl : prog.classDeclList) {
            classDecl.visit(this, table);
        }

        return null;
    }

    @Override
    public Object visitClassDecl(ClassDecl cd, Object arg) {
        assert table.currentScopeLevel() == 0;
        
        table.openScope(); // class names
        table.enter(cd.name, cd);
        table.openScope(); // member names
        
        // assume no field/method name collisions
        for (FieldDecl fieldDecl : cd.fieldDeclList)
            fieldDecl.visit(this, table);

        for (MethodDecl methodDecl : cd.methodDeclList)
            methodDecl.visit(this, table);

        table.closeScope(); // member names
        table.closeScope(); // class names
        return null;
    }

    @Override
    public Object visitFieldDecl(FieldDecl fd, Object arg) {
        assert table.currentScopeLevel() == 2;

        table.enter(fd.name, fd);
        fd.type.visit(this, table);
        return null;
    }

    @Override
    public Object visitMethodDecl(MethodDecl md, Object arg) {
    	table.setCurrentMethod(md);
    	
    	if (md.type instanceof ClassType) {
			Declaration classDecl = table.retrieveClass(((ClassType) md.type).className.spelling);
			if (classDecl == null) {
				reporter.reportError("*** line " + md.posn.lineNumber + ": method declaration on column "
						+ md.posn.columnNumber + " returns type does not exist.");
			}
    	}
    	
        assert table.currentScopeLevel() == 2;
        table.enter(md.name, md);
        table.openScope(); // parameter names

        for (ParameterDecl parameterDecl : md.parameterDeclList)
            parameterDecl.visit(this, table);

        table.openScope(); // local variable names
               
        for (Statement statementDecl : md.statementList) {
            statementDecl.visit(this, table);
        }
        
        table.closeScope(); // local variable names

        table.closeScope(); // parameter names
        return null;
    }

    @Override
    public Object visitParameterDecl(ParameterDecl pd, Object arg) {
        assert table.currentScopeLevel() == 3;
        table.enter(pd.name, pd);
        pd.type.visit(this, table);
        
		if (pd.type instanceof ClassType) {
			ClassType classType = (ClassType) pd.type;
			String className = classType.className.spelling;
			if (table.retrieveClass(className) == null) {
				reporter.reportError("*** line " + pd.posn.lineNumber + ": reference to undefined class " + className);
			}
		}
        
        return null;
    }

    @Override
    public Object visitVarDecl(VarDecl decl, Object arg) {
        assert table.currentScopeLevel() >= 4;
        table.enter(decl.name, decl);
        decl.type.visit(this, table);
        return null;
    }

    @Override
    public Object visitBaseType(BaseType type, Object arg) {
        return null;
    }

    @Override
    public Object visitClassType(ClassType type, Object arg) {
        type.className.visit(this, table);
        return null;
    }

    @Override
    public Object visitArrayType(ArrayType type, Object arg) {
        type.eltType.visit(this, table);
        return null;
    }

    @Override
    public Object visitBlockStmt(BlockStmt stmt, Object arg) {
        assert table.currentScopeLevel() >= 4;
        table.openScope();

        for (Statement statement : stmt.sl)
            statement.visit(this, table);

        table.closeScope();
        return null;
    }

    @Override
    public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
    	// It is illegal to use a variable in its initializing expression
    	this.forbiddenVariableName = stmt.varDecl.name;
        stmt.initExp.visit(this, table);        
        this.forbiddenVariableName = null;
        stmt.varDecl.visit(this, table);
        return null;
    }

    @Override
    public Object visitAssignStmt(AssignStmt stmt, Object arg) {
        stmt.ref.visit(this, table);
        stmt.val.visit(this, table);
        
        if (stmt.ref.decl instanceof FieldDecl) {
        	FieldDecl fieldDecl = (FieldDecl)stmt.ref.decl;
        	if (fieldDecl.isStatic) {
        		stmt.ref.decl.compilerHint = CompilerHint.STATIC_FIELD_ACCESS;
        	}
        }
        
		if (stmt.val instanceof RefExpr) {
			RefExpr refExpr = (RefExpr) stmt.val;
			if (refExpr.ref instanceof IdRef) {
				String idName = ((IdRef) refExpr.ref).id.spelling;
				int tableLevel = table.retrieveLevel(idName);
				if (tableLevel == IdentificationTable.CLASS_NAMES_LEVEL) {
					reporter.reportError(
							"*** line " + stmt.posn.lineNumber + " assignment to class " + idName + " is illegal.");
				}
			}
		}
        
        return null;
    }

    @Override
    public Object visitCallStmt(CallStmt stmt, Object arg) {
        stmt.methodRef.visit(this, table);
        for (Expression expression : stmt.argList)
            expression.visit(this, table);
        
		if (!(stmt.methodRef.decl instanceof MethodDecl)) {
			reporter.reportError("*** line " + stmt.posn.lineNumber + ": call to non-function is illegal.");
			return null;
		}
        
        MethodDecl calledMethod = (MethodDecl) stmt.methodRef.decl;
        if (table.currentMethod().isStatic && !(stmt.methodRef instanceof QualRef) && !calledMethod.isStatic) {
            reporter.reportError("*** line " + stmt.posn.lineNumber + ": static method called in illegal context.");
        }
        
        if (stmt.methodRef instanceof QualRef) {
            QualRef qualRef = (QualRef) stmt.methodRef;
            if (qualRef.ref instanceof IdRef) {
                IdRef idRef = (IdRef) qualRef.ref; 
                Declaration idDecl = idRef.decl;
                
                if (idDecl instanceof VarDecl && idDecl.type instanceof ClassType) {
                    idDecl = table.retrieveClass(((ClassType) idDecl.type).className.spelling);
                    MethodDecl calledMethodInAnotherClass = (MethodDecl) table.retrieveMethodFromClass(calledMethod.name,
                            idDecl.name);

                    if (calledMethodInAnotherClass.isPrivate) {
                        reporter.reportError(
                                "*** line " + stmt.posn.lineNumber + ": private method called in illegal context.");
                    }

                } else if (idDecl instanceof ClassDecl) {
                    if (table.retrieveMethodFromClass(calledMethod.name, idDecl.name) instanceof MethodDecl) {
                        MethodDecl calledMethodInAnotherClass = (MethodDecl) table.retrieveMethodFromClass(calledMethod.name,
                                idDecl.name);
                        if (!calledMethodInAnotherClass.isStatic) {
                            reporter.reportError(
                                    "*** line " + stmt.posn.lineNumber + ": static method called in illegal context.");
                        }

                        if (calledMethodInAnotherClass.isPrivate) {
                            reporter.reportError(
                                    "*** line " + stmt.posn.lineNumber + ": private method called in illegal context.");
                        }
                    } else {
                        reporter.reportError(
                                "*** line " + stmt.posn.lineNumber + ": member " + calledMethod.name + " on column "
                                        + stmt.posn.columnNumber + " does not exist in class " + idDecl.name + ".");
                    }
                }
                
            }
        }
        
        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmt stmt, Object arg) {
    	if (stmt.returnExpr != null)
    		stmt.returnExpr.visit(this, table);
        return null;
    }

    @Override
    public Object visitIfStmt(IfStmt stmt, Object arg) {
        stmt.cond.visit(this, table);
        
        if (stmt.thenStmt instanceof VarDeclStmt) {
            reporter.reportError("*** line " + stmt.thenStmt.posn.lineNumber + ": If statement then block on column "
                    + stmt.thenStmt.posn.columnNumber + " has variable declaration as entire body.");
        }
        
        stmt.thenStmt.visit(this, table);
        
        if (stmt.elseStmt != null) {
            stmt.elseStmt.visit(this, table);
            
            if (stmt.elseStmt instanceof VarDeclStmt) {
                reporter.reportError(
                        "*** line " + stmt.elseStmt.posn.lineNumber + ": If statement else block on column "
                                + stmt.elseStmt.posn.columnNumber + " has variable declaration as entire body.");
            }
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmt stmt, Object arg) {
        stmt.cond.visit(this, table);
        stmt.body.visit(this, table);
        
        if (stmt.body instanceof VarDeclStmt) {
            reporter.reportError("*** line " + stmt.body.posn.lineNumber + ": While statement block on column "
                    + stmt.body.posn.columnNumber + " has variable declaration as entire body.");
        }
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
        expr.operator.visit(this, table);
        expr.expr.visit(this, table);
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
        expr.operator.visit(this, table);
        expr.left.visit(this, table);
        expr.right.visit(this, table);
        return null;
    }

    @Override
    public Object visitRefExpr(RefExpr expr, Object arg) {
        expr.ref.visit(this, table);
        return null;
    }

    @Override
    public Object visitCallExpr(CallExpr expr, Object arg) {
        expr.functionRef.visit(this, table);
        for (Expression expression : expr.argList)
            expression.visit(this, table);
        
        return null;
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
        expr.lit.visit(this, table);
        return null;
    }

    @Override
    public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
        expr.classtype.visit(this, null);
        return null;
    }

    @Override
    public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
        expr.eltType.visit(this, table);
        expr.sizeExpr.visit(this, table);
        return null;
    }

    @Override
    public Object visitThisRef(ThisRef ref, Object arg) {
        ref.decl = table.currentClass();
        
        if (table.currentMethod().isStatic) {
            reporter.reportError("*** line " + ref.posn.lineNumber + ": this reference in a static method.");
        }
        return null;
    }

    @Override
    public Object visitIdRef(IdRef ref, Object arg) {
    	if (ref.id.spelling.equals(this.forbiddenVariableName)) {
			reporter.reportError(
					"*** line " + ref.posn.lineNumber + ": variable " + ref.id.spelling + " used in its own initializing expression.");
    	}
    	
        ref.id.visit(this, table);
        ref.decl = ref.id.decl;
        
        if (ref.decl instanceof MethodDecl) {
        	MethodDecl methodDecl = (MethodDecl) ref.decl;
        	if (methodDecl.isStatic != table.currentMethod().isStatic) {
        		System.out.println("current method is " + table.currentMethod().name);
				System.out.println("MethodDecl " + methodDecl.name + " isStatic=" + methodDecl.isStatic
						+ ", but currentMethod.isStatic=" + table.currentMethod().isStatic);
				reporter.reportError("*** line " + ref.posn.lineNumber
						+ " reference does not match current static/non-static context.");
        	}
        } else if (ref.decl instanceof FieldDecl) {
        	FieldDecl fieldDecl = (FieldDecl) ref.decl;
        	if (fieldDecl.isStatic != table.currentMethod().isStatic) {
				reporter.reportError("*** line " + ref.posn.lineNumber
						+ " reference does not match current static/non-static context.");
        	}
        }
        
        return null;
    }

    @Override
    public Object visitQRef(QualRef ref, Object arg) {  
        ref.ref.visit(this, table);
        
        if (ref.ref.decl.type instanceof ClassType) {
            String className = ((ClassType) ref.ref.decl.type).className.spelling;
//            System.out.println("Setting classContext to " + className + " after visiting " + ref.ref.decl.name);
            table.setClassContext(className);
        }
        
		if (!(ref.ref.decl.type.typeKind == TypeKind.ARRAY && ref.id.spelling.equals("length"))) {
			ref.id.visit(this, table);
		} else {
			ref.id.decl = new FieldDecl(false, false, new BaseType(TypeKind.INT, ref.ref.posn), "length", ref.ref.posn);
			ref.id.decl.compilerHint = CompilerHint.ARRAY_LENGTH;
		}
		ref.decl = ref.id.decl;
		
		table.unsetClassContext();
		
    	// Support static field assignment ClassName.field = value;
		if (ref.ref instanceof IdRef) {
			String idName = ((IdRef) ref.ref).id.spelling;
			int tableLevel = table.retrieveLevel(idName);
			if (tableLevel == IdentificationTable.CLASS_NAMES_LEVEL) {
				ref.ref.decl.compilerHint = CompilerHint.STATIC_FIELD_ACCESS;
				ref.decl.compilerHint = CompilerHint.STATIC_FIELD_ACCESS;
				
		        if (ref.id.decl instanceof FieldDecl) {
		        	FieldDecl fieldDecl = (FieldDecl) ref.id.decl;
		        	if (table.currentMethod().isStatic && !fieldDecl.isStatic) {
						reporter.reportError("*** line " + ref.posn.lineNumber
								+ " reference does not match current static/non-static context.");
		        	}
		        }
			}
		}
		
		if (ref.ref.decl instanceof MethodDecl) {
			reporter.reportError("*** line " + ref.posn.lineNumber
					+ " function reference cannot appear in the middle of a qualified reference.");
		}
        
        return null;
    }

    @Override
    public Object visitIxRef(IxRef ref, Object arg) {
        ref.ref.visit(this, table);
        ref.indexExpr.visit(this, table);
        ref.decl = ref.ref.decl;
        return null;
    }

    @Override
    public Object visitIdentifier(Identifier id, Object arg) {
        id.decl = table.retrieve(id.spelling);
        if (id.decl == null) {
            reporter.reportError("*** line " + id.posn.lineNumber + ": Identifier " + id.spelling + " on column "
                    + id.posn.columnNumber + " has no known declaration.");
            System.exit(4);
        }
        
		String currentClassName = table.currentClass().name;
		if (id.decl instanceof FieldDecl && table.retrieveFieldFromClass(id.spelling, currentClassName) == null) {
			FieldDecl fieldDecl = (FieldDecl) id.decl;
			if (fieldDecl.isPrivate) {
				reporter.reportError("*** line " + id.posn.lineNumber + " accessed private field " + id.spelling
						+ " in illegal context.");
			}
		}
        return null;
    }

    @Override
    public Object visitOperator(Operator op, Object arg) {
        return null;
    }

    @Override
    public Object visitIntLiteral(IntLiteral num, Object arg) {
        return null;
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteral bool, Object arg) {
        return null;
    }

    @Override
    public Object visitNullLiteral(NullLiteral nullLit, Object arg) {
        return null;
    }

}
