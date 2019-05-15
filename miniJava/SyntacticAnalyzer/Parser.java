package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.ExprList;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxRef;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.NullLiteral;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.WhileStmt;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassDeclList;

public class Parser {

    private Scanner scanner;
    private ErrorReporter reporter;
    private Token token;
    private boolean trace = false;

    public Parser(Scanner scanner, ErrorReporter reporter) {
        this.scanner = scanner;
        this.reporter = reporter;
    }

    /**
     * SyntaxError is used to unwind parse stack when parse fails
     *
     */
    class SyntaxError extends Error {
        private static final long serialVersionUID = 1L;
    }

    /**
     * parse input, catch possible parse error
     */
    public Package parse() {
        token = scanner.scan();
        try {
            return parseProgram();
        } catch (SyntaxError e) {
        	return null;
        }
    }

    // Program ::= (ClassDeclaration)* $
    private Package parseProgram() throws SyntaxError {
    	ClassDeclList list = new ClassDeclList();
        while (token.kind != TokenKind.EOT) {
            list.add(parseClassDeclaration());
        }
        accept(TokenKind.EOT);
        return new Package(list, scanner.currentPosition());
    }

    // ClassDeclaration ::= class id { (FieldDeclaration | MethodDeclaration)* }
    /*
     * ClassDeclaration ::= class id { (public | private)? static?
     *                      (
     *                        void id "(" ParameterList? ")" { Statement* }
     *                        | Type id 
     *                          (
     *                            ; | "(" ParameterList? ")" { Statement* }
     *                          )
     *                      )*
     *                      }
     */
    private ClassDecl parseClassDeclaration() throws SyntaxError {
        accept(TokenKind.CLASS);
        String className = token.spelling;
        Identifier classId = new Identifier(token, scanner.currentPosition());
        FieldDeclList fieldList = new FieldDeclList();
        MethodDeclList methodList = new MethodDeclList();

        accept(TokenKind.ID);
        accept(TokenKind.LBRACE);
        while (token.kind != TokenKind.RBRACE) {
            boolean isPrivate = false;
            boolean isStatic = false;
            ParameterDeclList parameterList = null;
            StatementList statementList = new StatementList();
            SourcePosition currentPosition = scanner.currentPosition();

            if (token.kind == TokenKind.PUBLIC) {
                acceptIt();
            } else if (token.kind == TokenKind.PRIVATE) {
                acceptIt();
                isPrivate = true;
            }
            if (token.kind == TokenKind.STATIC) {
                acceptIt();
                isStatic = true;
            }
            if (token.kind == TokenKind.VOID) { // void MethodDeclaration
                acceptIt();
                String methodName = token.spelling;

                accept(TokenKind.ID);
                accept(TokenKind.LPAREN);
                if (token.kind != TokenKind.RPAREN) {
                    parameterList = parseParameterList();
                } else {
                    parameterList = new ParameterDeclList();
                }
                accept(TokenKind.RPAREN);
                accept(TokenKind.LBRACE);
                while (token.kind != TokenKind.RBRACE) {
                    statementList.add(parseStatement());
                }
                accept(TokenKind.RBRACE);

                TypeDenoter thisType = new BaseType(TypeKind.VOID, scanner.currentPosition());
                MemberDecl thisField = new FieldDecl(isPrivate, isStatic, thisType, methodName,
                		currentPosition);
                MethodDecl thisMethod = new MethodDecl(thisField, parameterList, statementList,
                		currentPosition);
                methodList.add(thisMethod);
            } else {
                TypeDenoter thisType = parseType();
                String fieldName = token.spelling;

                accept(TokenKind.ID);
                if (token.kind == TokenKind.SEMICOLON) { // FieldDeclaration
                    acceptIt();
                    FieldDecl thisField = new FieldDecl(isPrivate, isStatic, thisType, fieldName,
                    		currentPosition);
                    fieldList.add(thisField);
                } else { // MethodDeclaration
                    accept(TokenKind.LPAREN);
                    if (token.kind != TokenKind.RPAREN) {
                        parameterList = parseParameterList();
                    } else {
                        parameterList = new ParameterDeclList();
                    }
                    accept(TokenKind.RPAREN);
                    accept(TokenKind.LBRACE);
                    while (token.kind != TokenKind.RBRACE) {
                        statementList.add(parseStatement());
                    }
                    accept(TokenKind.RBRACE);

                    MemberDecl thisField = new FieldDecl(isPrivate, isStatic, thisType, fieldName,
                    		currentPosition);
                    MethodDecl thisMethod = new MethodDecl(thisField, parameterList, statementList,
                    		currentPosition);
                    methodList.add(thisMethod);
                }
            }
        }
        accept(TokenKind.RBRACE);
        
        ClassDecl result = new ClassDecl(className, fieldList, methodList, classId.posn);
        result.type = new ClassType(classId, classId.posn);
        return result;
    }

    // Type ::= int | boolean | id | (int|id)[]
    // Type ::= (int | id) ([])? | boolean
    private TypeDenoter parseType() throws SyntaxError {
    	TypeDenoter type = null;
        if (token.kind == TokenKind.INT || token.kind == TokenKind.ID) {
        	if (token.kind == TokenKind.INT) {
        		type = new BaseType(TypeKind.INT, scanner.currentPosition());
            } else if (token.kind == TokenKind.ID) {
                type = new ClassType(new Identifier(token, scanner.currentPosition()), scanner.currentPosition());
            }
        	
            acceptIt();
            if (token.kind == TokenKind.LBRACKET) {
                acceptIt();
                accept(TokenKind.RBRACKET);
                
				// if we're parsing an array, the type we have is actually the array's eltType
                type = new ArrayType(type, scanner.currentPosition());
            }
        } else if (token.kind == TokenKind.BOOLEAN) {
            acceptIt();
            type = new BaseType(TypeKind.BOOLEAN, scanner.currentPosition());
        } else {
            parseError("Invalid Term - expecting INT, ID, or BOOLEAN, but found " + token.kind);
        }
        return type;
    }

    // ParameterList ::= Type id (, Type id)*
    private ParameterDeclList parseParameterList() throws SyntaxError {
    	ParameterDeclList list = new ParameterDeclList();
        TypeDenoter type = parseType();
        list.add(new ParameterDecl(type, token.spelling, scanner.currentPosition()));
        accept(TokenKind.ID);
        while (token.kind == TokenKind.COMMA) {
            acceptIt();
            type = parseType();
            list.add(new ParameterDecl(type, token.spelling, scanner.currentPosition()));
            accept(TokenKind.ID);
        }
        return list;
    }

    // ArgumentList ::= Expression (, Expression)*
    private ExprList parseArgumentList() throws SyntaxError {
    	ExprList list = new ExprList();
        list.add(parseExpression());
        while (token.kind == TokenKind.COMMA) {
            acceptIt();
            list.add(parseExpression());
        }
        return list;
    }

    /*
     * Statement ::= { Statement* }
     *             | Type id = Expression;
     *             | (Reference | IxReference) = Expression;
     *             | Reference (ArgumentList?);
     *             | return Expression?;
     *             | if (Expression) Statement (else Statement)?
     *             | while (Expression) Statement
     * Statement ::= { Statement* }
     *             | return Expression?;
     *             | if (Expression) Statement (else Statement)?
     *             | while (Expression) Statement
     *             | int ([])? id = Expression;
     *             | boolean id = Expression;
     *             | this (.id)* (
     *                 "(" ArgumentList? ")"
     *               | ([Expression])? = Expression
     *               );
     *             | id (
     *                 id = Expression
     *               | [ (] id | Expression]) = Expression
     *               | (.id)* (
     *                   ([Expression])? = Expression;
     *                 | "(" ArgumentList? ")"
     *                 )
     *               );
     */
    private Statement parseStatement() throws SyntaxError {
    	Statement thisStatement;
    	StatementList list;
    	Expression expr;
    	Expression condition;
    	Statement ifBlock;
    	Statement whileBody;
    	VarDecl thisVariable;
    	Expression variableExpression;
    	TypeDenoter thisType;
    	String variableName;
    	Reference thisReference;
    	Identifier thisIdentifier;
    	ExprList exprList;
    	Expression ixExpression;
    	
    	SourcePosition statementStart = scanner.currentPosition();
    	
        switch (token.kind) {
        case LBRACE:
            list = new StatementList();
            acceptIt();
            while (token.kind != TokenKind.RBRACE) {
                list.add(parseStatement());
            }
            accept(TokenKind.RBRACE);
            thisStatement = new BlockStmt(list, statementStart);
            break;
        case RETURN:
            acceptIt();
            if (token.kind != TokenKind.SEMICOLON) {
                expr = parseExpression();
            } else {
                expr = null;
            }
            accept(TokenKind.SEMICOLON);
            thisStatement = new ReturnStmt(expr, statementStart);
            break;
        case IF:
            acceptIt();
            accept(TokenKind.LPAREN);
            condition = parseExpression();
            accept(TokenKind.RPAREN);
            ifBlock = parseStatement();
            if (token.kind == TokenKind.ELSE) {
                acceptIt();
                thisStatement = new IfStmt(condition, ifBlock, parseStatement(), statementStart);
            } else {
                thisStatement = new IfStmt(condition, ifBlock, statementStart);
            }
            break;
        case WHILE:
            acceptIt();
            accept(TokenKind.LPAREN);
            condition = parseExpression();
            accept(TokenKind.RPAREN);
            whileBody = parseStatement();
            thisStatement = new WhileStmt(condition, whileBody, statementStart);
            break;
        case INT: // int ([])? id = Expression;
            thisType = new BaseType(TypeKind.INT, scanner.currentPosition());

            acceptIt();
            if (token.kind == TokenKind.LBRACKET) {
                acceptIt();
                accept(TokenKind.RBRACKET);
                thisType = new ArrayType(thisType, scanner.currentPosition());
            }
            variableName = token.spelling;
            thisVariable = new VarDecl(thisType, variableName, scanner.currentPosition());

            accept(TokenKind.ID);
            accept(TokenKind.ASSIGN);
            variableExpression = parseExpression();
            accept(TokenKind.SEMICOLON);

            thisStatement = new VarDeclStmt(thisVariable, variableExpression, statementStart);
            break;
        case BOOLEAN: // boolean id = Expression;
            thisType = new BaseType(TypeKind.BOOLEAN, scanner.currentPosition());

            acceptIt();

            variableName = token.spelling;
            thisVariable = new VarDecl(thisType, variableName, scanner.currentPosition());
            accept(TokenKind.ID);
            accept(TokenKind.ASSIGN);
            variableExpression = parseExpression();
            accept(TokenKind.SEMICOLON);

            thisStatement = new VarDeclStmt(thisVariable, variableExpression, statementStart);
            break;
        case THIS:
        	/*
		     * | this (.id)* (
		     *     "(" ArgumentList? ")"
		     *   | ([Expression])? = Expression
		     *   );
        	 */
            thisReference = new ThisRef(scanner.currentPosition());
            acceptIt();
            while (token.kind == TokenKind.PERIOD) {
                acceptIt();

                thisIdentifier = new Identifier(token, scanner.currentPosition());
                thisReference = new QualRef(thisReference, thisIdentifier, scanner.currentPosition());
                accept(TokenKind.ID);
            }
            if (token.kind == TokenKind.LPAREN) { // this (.id)* "(" ArgumentList? ")"
                acceptIt();
                if (token.kind != TokenKind.RPAREN) {
                    exprList = parseArgumentList();
                } else {
                    exprList = new ExprList();
                }
                accept(TokenKind.RPAREN);
                thisStatement = new CallStmt(thisReference, exprList, statementStart);
            } else { // this (.id)* ([Expression])? = Expression
                if (token.kind == TokenKind.LBRACKET) {
                    acceptIt();
                    ixExpression = parseExpression();
                    thisReference = new IxRef(thisReference, ixExpression, scanner.currentPosition());
                    accept(TokenKind.RBRACKET);
                }
                accept(TokenKind.ASSIGN);
                variableExpression = parseExpression();
                thisStatement = new AssignStmt(thisReference, variableExpression, statementStart);
            }
            accept(TokenKind.SEMICOLON);
            break;
        case ID:
        	/*
		     * | id (
		     *     id = Expression
		     *   | [ (] id | Expression]) = Expression
		     *   | (.id)* (
		     *       ([Expression])? = Expression;
		     *     | "(" ArgumentList? ")"
		     *     )
		     *   );
        	 */
            thisIdentifier = new Identifier(token, scanner.currentPosition());
            acceptIt();
            if (token.kind == TokenKind.ID) { // id id = Expression
                variableName = token.spelling;

                acceptIt();
                accept(TokenKind.ASSIGN);

                variableExpression = parseExpression();
                thisVariable = new VarDecl(new ClassType(thisIdentifier, scanner.currentPosition()), variableName,
                        scanner.currentPosition());
                thisStatement = new VarDeclStmt(thisVariable, variableExpression, statementStart);
            } else if (token.kind == TokenKind.LBRACKET) { // id [ (] id | Expression]) = Expression
                acceptIt();
                if (token.kind == TokenKind.RBRACKET) { // id [] id = Expression
                    acceptIt();
                    variableName = token.spelling;
                    accept(TokenKind.ID);
                    accept(TokenKind.ASSIGN);
                    variableExpression = parseExpression();

                    thisType = new ClassType(thisIdentifier, scanner.currentPosition());
                    thisType = new ArrayType(thisType, scanner.currentPosition());
                    thisVariable = new VarDecl(thisType, variableName, scanner.currentPosition());
                    thisStatement = new VarDeclStmt(thisVariable, variableExpression, statementStart);
                } else { // id [Expression] = Expression
                    ixExpression = parseExpression();
                    accept(TokenKind.RBRACKET);
                    accept(TokenKind.ASSIGN);
                    variableExpression = parseExpression();

                    thisReference = new IdRef(thisIdentifier, scanner.currentPosition());
                    thisReference = new IxRef(thisReference, ixExpression, scanner.currentPosition());
                    thisStatement = new AssignStmt(thisReference, variableExpression, statementStart);
                }
            } else {
            	/*
			     * id (.id)* (
			     *      ([Expression])? = Expression;
			     *    | "(" ArgumentList? ")"
			     *    )
			     * );
            	 */
                thisReference = new IdRef(thisIdentifier, scanner.currentPosition());
                while (token.kind == TokenKind.PERIOD) {
                    acceptIt();

                    thisIdentifier = new Identifier(token, scanner.currentPosition());
                    thisReference = new QualRef(thisReference, thisIdentifier, scanner.currentPosition());
                    accept(TokenKind.ID);
                }
                if (token.kind == TokenKind.LPAREN) { // id (.id)* "(" ArgumentList? ")"
                    acceptIt();
                    if (token.kind != TokenKind.RPAREN) {
                        exprList = parseArgumentList();
                    } else {
                        exprList = new ExprList();
                    }
                    accept(TokenKind.RPAREN);
                    thisStatement = new CallStmt(thisReference, exprList, statementStart);
                } else { // id (.id)* ([Expression])? = Expression;
                    if (token.kind == TokenKind.LBRACKET) {
                        acceptIt();
                        ixExpression = parseExpression();
                        accept(TokenKind.RBRACKET);

                        thisReference = new IxRef(thisReference, ixExpression, scanner.currentPosition());
                    }
                    accept(TokenKind.ASSIGN);
                    variableExpression = parseExpression();
                    thisStatement = new AssignStmt(thisReference, variableExpression, statementStart);
                }
            }
            accept(TokenKind.SEMICOLON);
            break;
        default:
            parseError("Invalid Term - expecting LBRACE, RETURN, IF, " + "WHILE, INT, BOOLEAN, or THIS, "
                    + "but found " + token.kind);
            thisStatement = null;
        }
        return thisStatement;
    }
    
    /*
     * Expression ::= Reference
     *              | IxReference
     *              | Reference (ArgumentList?)
     *              | unop Expression
     *              | Expression binop Expression
     *              | (Expression)
     *              | num | true | false
     *              | new (id() | int[Expression] | id[Expression])
     * Expression ::=
     *              (
     *                unop Expression
     *                | (Expression)
     *                | num | true | false
     *                | new (id() | int[Expression] | id[Expression])
     *                | (id|this)(.id)* ([Expression] | "(" ArgumentList? ")" )?
     *              )
     *              (binop Expression)*
     *              
     * Expression ::= Conjunction (|| Conjunction)*
     */
    private Expression parseExpression() throws SyntaxError {
    	SourcePosition position = scanner.currentPosition();
    	Expression e1 = parseConjunction();
    	e1.posn = position;
    	while (token.kind == TokenKind.OR) {
    		Operator op = new Operator(token);
    		acceptIt();
    		Expression e2 = parseConjunction();
    		e1 = new BinaryExpr(op, e1, e2, scanner.currentPosition());
    	}
    	return e1;
    }
    
    // Conjunction ::= Equality (&& Equality)*
    private Expression parseConjunction() throws SyntaxError {
    	Expression e1 = parseEquality();
    	while (token.kind == TokenKind.AND) {
    		Operator op = new Operator(token);
    		acceptIt();
    		Expression e2 = parseEquality();
    		e1 = new BinaryExpr(op, e1, e2, scanner.currentPosition());
    	}
    	return e1;
    }
    
    // Equality ::= Relational ((== | !=) Relational)*
    private Expression parseEquality() throws SyntaxError {
    	Expression e1 = parseRelational();
    	while (token.kind == TokenKind.EQUALS || token.kind == TokenKind.NOTEQUAL) {
    		Operator op = new Operator(token);
    		acceptIt();
    		Expression e2 = parseRelational();
    		e1 = new BinaryExpr(op, e1, e2, scanner.currentPosition());
    	}
    	return e1;
    }
    
    // Relational ::= Additive ((<= | < | > | >=) Additive)*
    private Expression parseRelational() throws SyntaxError {
    	Expression e1 = parseAdditive();
		while (token.kind == TokenKind.LESSEQUAL || token.kind == TokenKind.LESSTHAN
				|| token.kind == TokenKind.GREATERTHAN || token.kind == TokenKind.GREATEREQUAL) {
    		Operator op = new Operator(token);
    		acceptIt();
    		Expression e2 = parseAdditive();
    		e1 = new BinaryExpr(op, e1, e2, scanner.currentPosition());
    	}
    	return e1;
    }
    
    // Additive ::= Multiplicative ((+ | -) Multiplicative)*
    private Expression parseAdditive() throws SyntaxError {
    	Expression e1 = parseMultiplicative();
		while (token.kind == TokenKind.PLUS || token.kind == TokenKind.MINUS) {
    		Operator op = new Operator(token);
    		acceptIt();
    		Expression e2 = parseMultiplicative();
    		e1 = new BinaryExpr(op, e1, e2, scanner.currentPosition());
    	}
    	return e1;
    }
    
    // Multiplicative ::= Unary ((* | /) Unary)*
    private Expression parseMultiplicative() throws SyntaxError {
    	Expression e1 = parseUnary();
		while (token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE) {
    		Operator op = new Operator(token);
    		acceptIt();
    		Expression e2 = parseUnary();
    		e1 = new BinaryExpr(op, e1, e2, scanner.currentPosition());
    	}
    	return e1;
    }
    
    // Unary ::= (- | !)* BaseExpression
    private Expression parseUnary() throws SyntaxError {
    	if (token.kind == TokenKind.MINUS || token.kind == TokenKind.NOT) {
    		Operator op = new Operator(token);
    		acceptIt();
    		return new UnaryExpr(op, parseUnary(), scanner.currentPosition());
    	} else {
    		return parseBaseExpression();
    	}
    }
    
    /*
     * BaseExpression ::= (Expression)
     *                    | num | true | false
     *                    | new (id() | int[Expression] | id[Expression])
     *                    | (id|this)(.id)* ([Expression] | "(" ArgumentList? ")" )?
     */
    private Expression parseBaseExpression() throws SyntaxError {
    	Expression thisExpression;
    	Expression ixExpression;
    	Identifier thisIdentifier;
    	TypeDenoter thisType;
    	Reference thisReference;
    	ExprList exprList;
    	
        switch (token.kind) {
        case LPAREN: // (Expression)
            acceptIt();
            thisExpression = parseExpression();
            accept(TokenKind.RPAREN);
            break;
        case NUM: // num
            thisExpression = new LiteralExpr(new IntLiteral(token), scanner.currentPosition());
            acceptIt();
            break;
        case NULL: // null
            thisExpression = new LiteralExpr(new NullLiteral(token), scanner.currentPosition());
            acceptIt();
            break;
        case TRUE: // true | false
        case FALSE:
            thisExpression = new LiteralExpr(new BooleanLiteral(token), scanner.currentPosition());
            acceptIt();
            break;
        case NEW: // new (id() | int[Expression] | id[Expression])
            acceptIt();
            if (token.kind == TokenKind.ID) {
                thisIdentifier = new Identifier(token, scanner.currentPosition());
                acceptIt();
                if (token.kind == TokenKind.LPAREN) { // new id()
                    acceptIt();
                    accept(TokenKind.RPAREN);

                    thisExpression = new NewObjectExpr(new ClassType(thisIdentifier, scanner.currentPosition()),
                            scanner.currentPosition());
                } else { // new id[Expression]
                    accept(TokenKind.LBRACKET);
                    ixExpression = parseExpression();
                    accept(TokenKind.RBRACKET);

                    thisType = new ClassType(thisIdentifier, scanner.currentPosition());
                    thisExpression = new NewArrayExpr(thisType, ixExpression, scanner.currentPosition());
                }
            } else { // new int[Expression]
                accept(TokenKind.INT);
                accept(TokenKind.LBRACKET);
                ixExpression = parseExpression();
                accept(TokenKind.RBRACKET);
                thisExpression = new NewArrayExpr(new BaseType(TypeKind.INT, scanner.currentPosition()), ixExpression,
                        scanner.currentPosition());
            }
            break;
        case ID: // (id|this)(.id)* ([Expression] | "(" ArgumentList? ")" )?
        case THIS:
            if (token.kind == TokenKind.ID) {
                thisReference = new IdRef(new Identifier(token, scanner.currentPosition()), scanner.currentPosition());
            } else { // token.kind == TokenKind.THIS
                thisReference = new ThisRef(scanner.currentPosition());
            }

            acceptIt();
            while (token.kind == TokenKind.PERIOD) {
                acceptIt();
                thisReference = new QualRef(thisReference, new Identifier(token, scanner.currentPosition()),
                        scanner.currentPosition());
                accept(TokenKind.ID);
            }
            if (token.kind == TokenKind.LBRACKET) { // (id|this)(.id)* [Expression]
                acceptIt();
                ixExpression = parseExpression();
                accept(TokenKind.RBRACKET);

                thisExpression = new RefExpr(new IxRef(thisReference, ixExpression, scanner.currentPosition()),
                        scanner.currentPosition());
            } else if (token.kind == TokenKind.LPAREN) { // (id|this)(.id)* "(" ArgumentList? ")"
                acceptIt();
                if (token.kind != TokenKind.RPAREN) {
                    exprList = parseArgumentList();
                } else {
                    exprList = new ExprList();
                }
                accept(TokenKind.RPAREN);

                thisExpression = new CallExpr(thisReference, exprList, scanner.currentPosition());
            } else { // (id|this)(.id)*
                thisExpression = new RefExpr(thisReference, scanner.currentPosition());
            }
            break;
        default:
            parseError("Invalid Term - expecting NOT, MINUS, NUM, TRUE, FALSE, NEW, ID, or THIS, "
                    + "but found " + token.kind);
            thisExpression = null;
        }
        
        return thisExpression;
    }

    /**
     * accept current token and advance to next token
     */
    private void acceptIt() throws SyntaxError {
        accept(token.kind);
    }

    /**
     * verify that current token in input matches expected token and advance to next
     * token
     * 
     * @param expectedToken
     * @throws SyntaxError if match fails
     */
    private void accept(TokenKind expectedTokenKind) throws SyntaxError {       
        if (token.kind == expectedTokenKind) {
            if (trace)
                pTrace();
            token = scanner.scan();
        } else
            parseError("expecting '" + expectedTokenKind + "' but found '" + token.kind + "'");
    }

    /**
     * report parse error and unwind call stack to start of parse
     * 
     * @param e string with error detail
     * @throws SyntaxError
     */
    private void parseError(String e) throws SyntaxError {
        reporter.reportError("Parse error: " + e);
        throw new SyntaxError();
    }

    // show parse stack whenever terminal is accepted
    private void pTrace() {
        StackTraceElement[] stl = Thread.currentThread().getStackTrace();
        for (int i = stl.length - 1; i > 0; i--) {
            if (stl[i].toString().contains("parse"))
                System.out.println(stl[i]);
        }
        System.out.println("accepting: " + token.kind + " (\"" + token.spelling + "\")");
        System.out.println();
    }

}
