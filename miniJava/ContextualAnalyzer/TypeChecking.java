package miniJava.ContextualAnalyzer;

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
import miniJava.AbstractSyntaxTrees.ExprList;
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
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.SyntacticAnalyzer.SourcePosition;

public class TypeChecking implements Visitor<Object, TypeDenoter> {

    Identification identifier;
    ErrorReporter reporter;

    public TypeChecking(Identification identifier, ErrorReporter reporter) {
        this.identifier = identifier;
        this.reporter = reporter;
    }

    public void typeCheck(AST ast) {
        ast.visit(this, null);
    }

    private TypeDenoter getType(TypeDenoter type) {
        if (type == null) {
//            System.out.println("WARNING: found null type?");
            return new BaseType(TypeKind.ERROR, new SourcePosition());
        }
        
        return type;
    }

    private boolean typeKindEquals(TypeDenoter type1, TypeKind type2) {
    	if (type1 == null || type2 == null) {
    		return false;
    	}
    	
        return type1.typeKind == type2;
    }

    private boolean typeEquals(TypeDenoter type1, TypeDenoter type2) {
        if (type1 == null || type2 == null) {
            return false;
        }
               
        if (type1.typeKind == TypeKind.ERROR || type2.typeKind == TypeKind.ERROR) {
            return true;
        } else if (type1.typeKind == TypeKind.UNSUPPORTED || type2.typeKind == TypeKind.UNSUPPORTED) {
            return false;
        } else if (type1 instanceof ArrayType || type2 instanceof ArrayType) {
        	// arrays can be assigned and compared to null
        	if (type1.typeKind == TypeKind.NULL || type2.typeKind == TypeKind.NULL) {
        		return true;
        	}
        	
            if (!(type1 instanceof ArrayType) || !(type2 instanceof ArrayType)) {
                return false;
            }

            return typeEquals(((ArrayType) type1).eltType, ((ArrayType) type2).eltType);
        } else if (type1 instanceof ClassType || type2 instanceof ClassType) {            
            // Class objects can be assigned to null
            if (type1.typeKind == TypeKind.CLASS && type2.typeKind == TypeKind.NULL) {
                return true;
            } else if (type1.typeKind == TypeKind.NULL && type2.typeKind == TypeKind.CLASS) {
                return true;
            } else if (!(type1 instanceof ClassType) || !(type2 instanceof ClassType)) {
                return false;
            }

            Identifier className1 = ((ClassType) type1).className;
            Identifier className2 = ((ClassType) type2).className;
            
            if (className1.decl != null && className2.decl != null) {
    			if (typeKindEquals(className1.decl.type, TypeKind.UNSUPPORTED)
    					|| typeKindEquals(className2.decl.type, TypeKind.UNSUPPORTED)) {
    				return false;
    			}
            }
            
            return className1.spelling.equals(className2.spelling); // name equivalence
        }

        return type1.typeKind == type2.typeKind;
    }

    @Override
    public TypeDenoter visitPackage(Package prog, Object arg) {
        for (ClassDecl classDecl : prog.classDeclList) {
            classDecl.visit(this, null);
        }
        return new BaseType(TypeKind.UNSUPPORTED, prog.posn);
    }

    @Override
    public TypeDenoter visitClassDecl(ClassDecl cd, Object arg) {        
        for (FieldDecl fieldDecl : cd.fieldDeclList) {
            fieldDecl.visit(this, null);
        }

        for (MethodDecl methodDecl : cd.methodDeclList) {
            methodDecl.visit(this, null);
        }

        return getType(cd.type);
    }

    @Override
    public TypeDenoter visitFieldDecl(FieldDecl fd, Object arg) {
        return getType(fd.type);
    }

    @Override
    public TypeDenoter visitMethodDecl(MethodDecl md, Object arg) {
        TypeDenoter returnType = getType(md.type);

        for (ParameterDecl parameterDecl : md.parameterDeclList) {
            parameterDecl.visit(this, null);
        }

        for (Statement statement : md.statementList) {
            TypeDenoter type = getType(statement.visit(this, null));
            if (statement instanceof ReturnStmt && !typeEquals(type, returnType)) {
                reporter.reportError("*** line " + statement.posn.lineNumber + ": Return statement on column "
                        + statement.posn.columnNumber + " has type " + type.typeKind
                        + ", which does not match function return type " + returnType.typeKind + ".");
            }
        }

        return returnType;
    }

    @Override
    public TypeDenoter visitParameterDecl(ParameterDecl pd, Object arg) {
        return getType(pd.type);
    }

    @Override
    public TypeDenoter visitVarDecl(VarDecl decl, Object arg) {    	
        return getType(decl.type);
    }

    @Override
    public TypeDenoter visitBaseType(BaseType type, Object arg) {
        return getType(type);
    }

    @Override
    public TypeDenoter visitClassType(ClassType type, Object arg) {
        return getType(type);
    }

    @Override
    public TypeDenoter visitArrayType(ArrayType type, Object arg) {
        return getType(type);
    }

    @Override
    public TypeDenoter visitBlockStmt(BlockStmt stmt, Object arg) {
        for (Statement statement : stmt.sl) {
            statement.visit(this, null);
        }
        
        return new BaseType(TypeKind.UNSUPPORTED, stmt.posn);
    }

    @Override
    public TypeDenoter visitVardeclStmt(VarDeclStmt stmt, Object arg) {
        TypeDenoter refType = getType(stmt.varDecl.visit(this, null));
        TypeDenoter exprType = getType(stmt.initExp.visit(this, null));
                
		if (stmt.initExp instanceof RefExpr) {
			RefExpr refExpr = (RefExpr) stmt.initExp;
			if (refExpr.ref.decl instanceof ClassDecl) {
				reporter.reportError("*** line " + stmt.varDecl.posn.lineNumber + ": Variable declaration on column "
						+ stmt.varDecl.posn.columnNumber + " attempts invalid assignment of class.");
			}
			
			if (refExpr.ref.decl instanceof MethodDecl) {
				reporter.reportError("*** line " + stmt.varDecl.posn.lineNumber + ": Variable declaration on column "
						+ stmt.varDecl.posn.columnNumber + " attempts invalid assignment of method.");
			}
		}

        if (!typeEquals(refType, exprType)) {
            if (refType instanceof ClassType && exprType instanceof ClassType) {
                Identifier className1 = ((ClassType) refType).className;
				Identifier className2 = ((ClassType) exprType).className;
				if (className1.decl == null || className2.decl == null) {
					reporter.reportError("*** line " + stmt.varDecl.posn.lineNumber
							+ ": Variable declaration on column " + stmt.varDecl.posn.columnNumber
							+ " attempts invalid assignment of class type.");
				} else {
					reporter.reportError("*** line " + stmt.varDecl.posn.lineNumber
							+ ": Variable declaration on column " + stmt.varDecl.posn.columnNumber
							+ " attempts to assign expression of type " + className1.decl.type.typeKind
							+ " to reference of type " + className2.decl.type.typeKind + ".");
				}
            } else {
                reporter.reportError("*** line " + stmt.varDecl.posn.lineNumber + ": Variable declaration on column "
                        + stmt.varDecl.posn.columnNumber + " attempts to assign expression of type " + exprType.typeKind
                        + " to reference of type " + refType.typeKind + ".");
            }

            return new BaseType(TypeKind.ERROR, stmt.posn);
        }
        return refType;
    }

    @Override
    public TypeDenoter visitAssignStmt(AssignStmt stmt, Object arg) {
        TypeDenoter refType = getType(stmt.ref.visit(this, null));
        TypeDenoter exprType = getType(stmt.val.visit(this, null));

		if (stmt.val instanceof RefExpr) {
			RefExpr refExpr = (RefExpr) stmt.val;			
			if (refExpr.ref.decl instanceof MethodDecl) {
				reporter.reportError("*** line " + stmt.posn.lineNumber + ": Assignment on column "
						+ stmt.posn.columnNumber + " attempts invalid assignment of method.");
			}
		}
        
        if (stmt.ref instanceof QualRef) {
        	QualRef qualRef = (QualRef) stmt.ref;
			if (typeKindEquals(qualRef.ref.decl.type, TypeKind.ARRAY) && qualRef.id.spelling.equals("length")) {
				reporter.reportError("*** line " + stmt.posn.lineNumber + ": Assignment on column "
						+ stmt.posn.columnNumber + " to array length is illegal.");
				return new BaseType(TypeKind.ERROR, stmt.posn);
        	}
        }

        if (!typeEquals(refType, exprType)) {
            reporter.reportError("*** line " + stmt.posn.lineNumber + ": Assignment on column " + stmt.posn.columnNumber
                    + " attempts to assign expression of type " + exprType.typeKind + " to reference of type "
                    + refType.typeKind + ".");
            return new BaseType(TypeKind.ERROR, stmt.posn);
        }
        
        return refType;
    }

    @Override
    public TypeDenoter visitCallStmt(CallStmt stmt, Object arg) {
        if (!(stmt.methodRef.decl instanceof MethodDecl)) {
            reporter.reportError("*** line " + stmt.posn.lineNumber + ": Function call on column "
                    + stmt.posn.columnNumber + " references function that does not exist.");
            return new BaseType(TypeKind.ERROR, stmt.posn);
        }

        ParameterDeclList trueParams = ((MethodDecl) stmt.methodRef.decl).parameterDeclList;
        TypeDenoter trueReturn = getType(stmt.methodRef.visit(this, null));
        ExprList observedParams = stmt.argList;

        if (trueParams.size() != observedParams.size()) {
            reporter.reportError(
                    "*** line " + stmt.posn.lineNumber + ": Function call on column " + stmt.posn.columnNumber + " has "
                            + observedParams.size() + " parameters, but should have " + trueParams.size() + ".");
            return new BaseType(TypeKind.ERROR, stmt.posn);
        }

        for (int i = 0; i < observedParams.size(); i++) {
            TypeDenoter trueType = getType(trueParams.get(i).type);
            TypeDenoter observedType = getType(observedParams.get(i).visit(this, null));
            if (!typeEquals(trueType, observedType)) {
                reporter.reportError("*** line " + stmt.posn.lineNumber + ": Function call on column "
                        + stmt.posn.columnNumber + " has parameter " + i + " of type " + observedType.typeKind
                        + ", but should have type " + trueType.typeKind + ".");
                return new BaseType(TypeKind.ERROR, stmt.posn);
            }
        }

        return trueReturn;
    }

    @Override
    public TypeDenoter visitReturnStmt(ReturnStmt stmt, Object arg) {
    	if (stmt.returnExpr != null)
    		return stmt.returnExpr.visit(this, null);
    	return new BaseType(TypeKind.VOID, stmt.posn);
    }

    @Override
    public TypeDenoter visitIfStmt(IfStmt stmt, Object arg) {        
        if (!typeKindEquals(stmt.cond.visit(this, null), TypeKind.BOOLEAN)) {
            reporter.reportError("*** line " + stmt.posn.lineNumber + ": If block condition on column "
                    + stmt.posn.columnNumber + " is not of type BOOLEAN.");
            return new BaseType(TypeKind.ERROR, stmt.posn);
        }
        stmt.thenStmt.visit(this, null);
        if (stmt.elseStmt != null) {
            stmt.elseStmt.visit(this, null);
        }
        
        return new BaseType(TypeKind.UNSUPPORTED, stmt.posn);
    }

    @Override
    public TypeDenoter visitWhileStmt(WhileStmt stmt, Object arg) {
        if (!typeKindEquals(stmt.cond.visit(this, null), TypeKind.BOOLEAN)) {
            reporter.reportError("*** line " + stmt.posn.lineNumber + ": While loop condition on column "
                    + stmt.posn.columnNumber + " is not of type BOOLEAN.");
            return new BaseType(TypeKind.ERROR, stmt.posn);
        }
        stmt.body.visit(this, null);
        
        return new BaseType(TypeKind.UNSUPPORTED, stmt.posn);
    }

    @Override
    public TypeDenoter visitUnaryExpr(UnaryExpr expr, Object arg) {
        TypeDenoter combinedType;

        switch (expr.operator.kind) {
        case MINUS:
            if (!typeKindEquals(expr.expr.visit(this, null), TypeKind.INT)) {
                reporter.reportError("*** line " + expr.posn.lineNumber + ": Operand on column "
                        + expr.posn.columnNumber + " with operator " + expr.operator.kind + " is not of type INT.");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            combinedType = new BaseType(TypeKind.INT, expr.posn);
            break;
        case NOT:
            if (!typeKindEquals(expr.expr.visit(this, null), TypeKind.BOOLEAN)) {
                reporter.reportError("*** line " + expr.posn.lineNumber + ": Operand on column "
                        + expr.posn.columnNumber + " with operator " + expr.operator.kind + " is not of type BOOLEAN.");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            combinedType = new BaseType(TypeKind.BOOLEAN, expr.posn);
            break;
        default:
            assert false;
            combinedType = null;
            break;
        }
        return combinedType;
    }

    @Override
    public TypeDenoter visitBinaryExpr(BinaryExpr expr, Object arg) {
        TypeDenoter leftType = expr.left.visit(this, null);
        TypeDenoter rightType = expr.right.visit(this, null);
        TypeDenoter combinedType;

        switch (expr.operator.kind) {
        case AND:
        case OR:
            if (!typeKindEquals(leftType, TypeKind.BOOLEAN)) {
                reporter.reportError(
                        "*** line " + expr.left.posn.lineNumber + ": Operand on column " + expr.left.posn.columnNumber
                                + " with operator " + expr.operator.kind + " is not of type BOOLEAN.");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            if (!typeKindEquals(rightType, TypeKind.BOOLEAN)) {
                reporter.reportError(
                        "*** line " + expr.right.posn.lineNumber + ": Operand on column " + expr.right.posn.columnNumber
                                + " with operator " + expr.operator.kind + " is not of type BOOLEAN.");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            combinedType = new BaseType(TypeKind.BOOLEAN, expr.posn);
            break;
        case EQUALS:
        case NOTEQUAL:           
            if (!typeEquals(leftType, rightType)) {
				if (leftType instanceof ClassType && rightType instanceof ClassType) {
					Identifier className1 = ((ClassType) rightType).className;
					Identifier className2 = ((ClassType) leftType).className;
					reporter.reportError("*** line " + expr.posn.lineNumber + ": Binary Expression on column "
							+ expr.posn.columnNumber + " has unmatched types " + className1.decl.type.typeKind + " and "
							+ className2.decl.type.typeKind + ".");
				} else {
					reporter.reportError("*** line " + expr.posn.lineNumber + ": Binary Expression on column "
							+ expr.posn.columnNumber + " has unmatched types " + leftType.typeKind + " and "
							+ rightType.typeKind + ".");
				}
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            combinedType = new BaseType(TypeKind.BOOLEAN, expr.posn);
            break;
        case GREATEREQUAL:
        case GREATERTHAN:
        case LESSEQUAL:
        case LESSTHAN:
            if (!typeKindEquals(leftType, TypeKind.INT)) {
                reporter.reportError(
                        "*** line " + expr.left.posn.lineNumber + ": Operand on column " + expr.left.posn.columnNumber
                                + " with operator " + expr.operator.kind + " is not of type INT.");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            if (!typeKindEquals(rightType, TypeKind.INT)) {
                reporter.reportError(
                        "*** line " + expr.right.posn.lineNumber + ": Operan on column " + expr.right.posn.columnNumber
                                + " with operator " + expr.operator.kind + " is not of type INT.");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            combinedType = new BaseType(TypeKind.BOOLEAN, expr.posn);
            break;
        case MINUS:
        case PLUS:
        case TIMES:
        case DIVIDE:
            if (!typeKindEquals(leftType, TypeKind.INT)) {
                reporter.reportError(
                        "*** line " + expr.left.posn.lineNumber + ": Operand on column " + expr.left.posn.columnNumber
                                + " with operator " + expr.operator.kind + " is not of type INT.");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            if (!typeKindEquals(rightType, TypeKind.INT)) {
                reporter.reportError(
                        "*** line " + expr.right.posn.lineNumber + ": Operand on column " + expr.right.posn.columnNumber
                                + " with operator " + expr.operator.kind + " is not of type INT.");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
            combinedType = new BaseType(TypeKind.INT, expr.posn);
            break;
        default:
            assert false;
            combinedType = null;
            break;
        }

        return combinedType;
    }

    @Override
    public TypeDenoter visitRefExpr(RefExpr expr, Object arg) {
        return expr.ref.visit(this, null);
    }

    @Override
    public TypeDenoter visitCallExpr(CallExpr expr, Object arg) {
        if (!(expr.functionRef.decl instanceof MethodDecl)) {
            reporter.reportError("*** line " + expr.posn.lineNumber + ": Function call on column "
                    + expr.posn.columnNumber + " references function that does not exist.");
            return new BaseType(TypeKind.ERROR, expr.posn);
        }

        MethodDecl methodDecl = (MethodDecl) expr.functionRef.decl;
        ParameterDeclList trueParams = methodDecl.parameterDeclList;
        TypeDenoter trueReturn = getType(expr.functionRef.visit(this, null));
        ExprList observedParams = expr.argList;

        if (trueParams.size() != observedParams.size()) {
            reporter.reportError("*** line " + expr.posn.lineNumber + ": Function call on column " + expr.posn.columnNumber
                    + " has " + observedParams.size() + " parameters, but should have " + trueParams.size() + ".");
            return new BaseType(TypeKind.ERROR, expr.posn);
        }

        for (int i = 0; i < observedParams.size(); i++) {
            TypeDenoter trueType = getType(trueParams.get(i).type);
            TypeDenoter observedType = getType(observedParams.get(i).visit(this, null));
            if (!typeEquals(trueType, observedType)) {
                reporter.reportError("*** line " + expr.posn.lineNumber + ": Function call on column "
                        + expr.posn.columnNumber + " has parameter " + i + " of type " + observedType.typeKind
                        + ", but should have type " + trueType.typeKind + ".");
                return new BaseType(TypeKind.ERROR, expr.posn);
            }
        }

        return trueReturn;
    }

    @Override
    public TypeDenoter visitLiteralExpr(LiteralExpr expr, Object arg) {
        return expr.lit.visit(this, null);
    }

    @Override
    public TypeDenoter visitNewObjectExpr(NewObjectExpr expr, Object arg) {
        return getType(expr.classtype);
    }

    @Override
    public TypeDenoter visitNewArrayExpr(NewArrayExpr expr, Object arg) {
        if (!typeKindEquals(expr.sizeExpr.visit(this, null), TypeKind.INT)) {
            reporter.reportError("*** line " + expr.sizeExpr.posn.lineNumber + ": Array size expression on column "
                    + expr.sizeExpr.posn.columnNumber + " is not of type INT.");
            return new BaseType(TypeKind.ERROR, expr.sizeExpr.posn);
        }

        return getType(new ArrayType(expr.eltType, expr.posn));
    }

    @Override
    public TypeDenoter visitThisRef(ThisRef ref, Object arg) {
        return getType(ref.decl.type);
    }

    @Override
    public TypeDenoter visitIdRef(IdRef ref, Object arg) {    	
        return getType(ref.decl.type);
    }

    @Override
    public TypeDenoter visitQRef(QualRef ref, Object arg) {
        TypeDenoter refType = ref.ref.visit(this, null);
		if (!(refType instanceof ClassType)) {
			if (!(typeKindEquals(refType, TypeKind.ARRAY) && ref.id.spelling.equals("length"))) {
				reporter.reportError("*** line " + ref.posn.lineNumber + ": Function call on column "
						+ ref.posn.columnNumber + " is not on an object.");
			}
		}
        
        return ref.id.visit(this, null);
    }

    @Override
    public TypeDenoter visitIxRef(IxRef ref, Object arg) {
        if (!typeKindEquals(ref.indexExpr.visit(this, null), TypeKind.INT)) {
            reporter.reportError("*** line " + ref.indexExpr.posn.lineNumber + ": Indexing expression on column "
                    + ref.indexExpr.posn.columnNumber + " is not of type INT.");
            return new BaseType(TypeKind.ERROR, ref.posn);
        }

        if (ref.decl.type instanceof ArrayType) {
            return getType(((ArrayType) ref.decl.type).eltType);
        } else {
            reporter.reportError("*** line " + ref.indexExpr.posn.lineNumber + ": Indexed reference on column "
                    + ref.indexExpr.posn.columnNumber + " is not of type ARRAY.");
            return new BaseType(TypeKind.ERROR, ref.posn);
        }
    }

    @Override
    public TypeDenoter visitIdentifier(Identifier id, Object arg) {
        return getType(id.decl.type);
    }

    @Override
    public TypeDenoter visitOperator(Operator op, Object arg) {
        return new BaseType(TypeKind.UNSUPPORTED, op.posn);
    }

    @Override
    public TypeDenoter visitIntLiteral(IntLiteral num, Object arg) {
        return new BaseType(TypeKind.INT, num.posn);
    }

    @Override
    public TypeDenoter visitBooleanLiteral(BooleanLiteral bool, Object arg) {
        return new BaseType(TypeKind.BOOLEAN, bool.posn);
    }

    @Override
    public TypeDenoter visitNullLiteral(NullLiteral nullLit, Object arg) {
        return new BaseType(TypeKind.NULL, nullLit.posn);
    }

}
