/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

/**
 * An implementation of the Visitor interface provides a method visitX
 * for each non-abstract AST class X.  
 */
public interface Visitor<ArgType,ResultType> {

  // Package
    public ResultType visitPackage(Package prog, ArgType arg);

  // Declarations
    public ResultType visitClassDecl(ClassDecl cd, ArgType arg);
    public ResultType visitFieldDecl(FieldDecl fd, ArgType arg);
    public ResultType visitMethodDecl(MethodDecl md, ArgType arg);
    public ResultType visitParameterDecl(ParameterDecl pd, ArgType arg);
    public ResultType visitVarDecl(VarDecl decl, ArgType arg);
 
  // Types
    public ResultType visitBaseType(BaseType type, ArgType arg);
    public ResultType visitClassType(ClassType type, ArgType arg);
    public ResultType visitArrayType(ArrayType type, ArgType arg);
    
  // Statements
    public ResultType visitBlockStmt(BlockStmt stmt, ArgType arg);
    public ResultType visitVardeclStmt(VarDeclStmt stmt, ArgType arg);
    public ResultType visitAssignStmt(AssignStmt stmt, ArgType arg);
    public ResultType visitCallStmt(CallStmt stmt, ArgType arg);
    public ResultType visitReturnStmt(ReturnStmt stmt, ArgType arg);
    public ResultType visitIfStmt(IfStmt stmt, ArgType arg);
    public ResultType visitWhileStmt(WhileStmt stmt, ArgType arg);
    
  // Expressions
    public ResultType visitUnaryExpr(UnaryExpr expr, ArgType arg);
    public ResultType visitBinaryExpr(BinaryExpr expr, ArgType arg);
    public ResultType visitRefExpr(RefExpr expr, ArgType arg);
    public ResultType visitCallExpr(CallExpr expr, ArgType arg);
    public ResultType visitLiteralExpr(LiteralExpr expr, ArgType arg);
    public ResultType visitNewObjectExpr(NewObjectExpr expr, ArgType arg);
    public ResultType visitNewArrayExpr(NewArrayExpr expr, ArgType arg);
    
  // References
    public ResultType visitThisRef(ThisRef ref, ArgType arg);
    public ResultType visitIdRef(IdRef ref, ArgType arg);
    public ResultType visitQRef(QualRef ref, ArgType arg);
    public ResultType visitIxRef(IxRef ref, ArgType arg);

  // Terminals
    public ResultType visitIdentifier(Identifier id, ArgType arg);
    public ResultType visitOperator(Operator op, ArgType arg);
    public ResultType visitIntLiteral(IntLiteral num, ArgType arg);
    public ResultType visitBooleanLiteral(BooleanLiteral bool, ArgType arg);
    public ResultType visitNullLiteral(NullLiteral nullLit, ArgType arg);
}
