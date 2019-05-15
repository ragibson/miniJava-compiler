package miniJava.ContextualAnalyzer;

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
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

public class IdentificationVerifier implements Visitor<Object, Object> {

    public void verifyIdentification(AST ast) {
        ast.visit(this, null);
    }

    @Override
    public Object visitPackage(Package prog, Object arg) {
        for (ClassDecl c : prog.classDeclList)
            c.visit(this, null);
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
        md.type.visit(this, null);
        for (ParameterDecl p : md.parameterDeclList)
            p.visit(this, null);
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
        for (Statement s : stmt.sl)
            s.visit(this, null);
        return null;
    }

    @Override
    public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
        stmt.initExp.visit(this, null);
        stmt.varDecl.visit(this, null);
        return null;
    }

    @Override
    public Object visitAssignStmt(AssignStmt stmt, Object arg) {
        stmt.ref.visit(this, null);
        stmt.val.visit(this, null);
        return null;
    }

    @Override
    public Object visitCallStmt(CallStmt stmt, Object arg) {
        for (Expression e : stmt.argList)
            e.visit(this, null);
        stmt.methodRef.visit(this, null);
        return null;
    }

    @Override
    public Object visitReturnStmt(ReturnStmt stmt, Object arg) {
        stmt.returnExpr.visit(this, null);
        return null;
    }

    @Override
    public Object visitIfStmt(IfStmt stmt, Object arg) {
        stmt.cond.visit(this, null);
        stmt.thenStmt.visit(this, null);
        if (stmt.elseStmt != null)
            stmt.elseStmt.visit(this, null);
        return null;
    }

    @Override
    public Object visitWhileStmt(WhileStmt stmt, Object arg) {
        stmt.body.visit(this, null);
        stmt.cond.visit(this, null);
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
        expr.expr.visit(this, null);
        expr.operator.visit(this, null);
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
        expr.left.visit(this, null);
        expr.operator.visit(this, null);
        expr.right.visit(this, null);
        return null;
    }

    @Override
    public Object visitRefExpr(RefExpr expr, Object arg) {
        expr.ref.visit(this, null);
        return null;
    }

    @Override
    public Object visitCallExpr(CallExpr expr, Object arg) {
        for (Expression e : expr.argList)
            e.visit(this, null);
        expr.functionRef.visit(this, null);
        return null;
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
        expr.lit.visit(this, null);
        return null;
    }

    @Override
    public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
        expr.classtype.visit(this, null);
        return null;
    }

    @Override
    public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
        expr.eltType.visit(this, null);
        expr.sizeExpr.visit(this, null);
        return null;
    }

    @Override
    public Object visitThisRef(ThisRef ref, Object arg) {
        if (ref.decl == null) {
            System.out.println("*ThisRef has no declaration!");
        } else {
//            System.out.println("ThisRef has a declaration!");
        }
        return null;
    }

    @Override
    public Object visitIdRef(IdRef ref, Object arg) {
        ref.id.visit(this, null);
        if (ref.decl == null) {
            System.out.println("*IdRef " + ref.id.spelling + " has no declaration!");
        } else {
//            System.out.println("IdRef " + ref.id.spelling + " has a declaration!");
        }
        return null;
    }

    @Override
    public Object visitQRef(QualRef ref, Object arg) {
        ref.id.visit(this, null);
        ref.ref.visit(this, null);
        if (ref.decl == null) {
            System.out.println("*QualRef " + ref.id.spelling + " has no declaration!");
        } else {
//            System.out.println("*QualRef " + ref.id.spelling + " has a declaration!");
        }
        return null;
    }

    @Override
    public Object visitIxRef(IxRef ref, Object arg) {
        ref.indexExpr.visit(this, null);
        ref.ref.visit(this, null);
        if (ref.decl == null) {
            System.out.println("*IxRef has no declaration!");
        } else {
//            System.out.println("IxRef has a declaration!");
        }
        return null;
    }

    @Override
    public Object visitIdentifier(Identifier id, Object arg) {
        if (id.decl == null) {
            System.out.println("*Identifier " + id.spelling + " has no declaration!");
        } else {
//            System.out.println("Identifier " + id.spelling + " has a declaration!");
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
