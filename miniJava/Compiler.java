package miniJava;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ASTDisplay;
import miniJava.CodeGenerator.CodeGenerator;
import miniJava.ContextualAnalyzer.Identification;
import miniJava.ContextualAnalyzer.TypeChecking;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;

public class Compiler {

	enum CompilerStage {
		PARSER(0), // PA1, PA2
		TYPE_CHECKING(1), // PA3
		CODE_GENERATION(2); // PA4, PA5

		private int value;

		CompilerStage(final int initValue) {
			value = initValue;
		}

		public int getValue() {
			return value;
		}
	};

	public static void checkFailure(ErrorReporter reporter, String stepName) {
		if (reporter.hasErrors()) {
			System.out.println("INVALID program after " + stepName);

			// return code for invalid input
			System.exit(4);
		}
	}

	public static void main(String[] args) {
		// Use to test different portions of the compiler
		CompilerStage finalStage = CompilerStage.CODE_GENERATION;
		
		if (args.length > 1) {
		    switch (args[1]) {
		    case "PARSER":
		        finalStage = CompilerStage.PARSER;
		        break;
		    case "TYPE CHECKING":
		        finalStage = CompilerStage.TYPE_CHECKING;
		        break;
		    case "CODE GENERATION":
		        finalStage = CompilerStage.CODE_GENERATION;
		        break;
		    }
		}

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(args[0]);
		} catch (FileNotFoundException e) {
			System.out.println("Input file " + args[0] + " not found");
			System.exit(3);
		}

		ErrorReporter reporter = new ErrorReporter();
		Scanner scanner = new Scanner(inputStream, reporter);
		Parser parser = new Parser(scanner, reporter);
		ASTDisplay display = new ASTDisplay();
		Identification identifier = new Identification(reporter);
//        IdentificationVerifier verifier = new IdentificationVerifier();
		TypeChecking typeChecker = new TypeChecking(identifier, reporter);
		AST theAST = null;

		try {
			if (finalStage.getValue() >= CompilerStage.PARSER.getValue()) {
				theAST = parser.parse();
				checkFailure(reporter, "parsing");

				// Needed for pa2_tests
				if (finalStage == CompilerStage.PARSER)
					display.showTree(theAST);
			}

			if (finalStage.getValue() >= CompilerStage.TYPE_CHECKING.getValue()) {
				identifier.linkDeclarationsAndReferences(theAST);
//				verifier.verifyIdentification(theAST);
				checkFailure(reporter, "identification");
				typeChecker.typeCheck(theAST);
				checkFailure(reporter, "type checking");
			}

			if (finalStage.getValue() >= CompilerStage.CODE_GENERATION.getValue()) {
				CodeGenerator generator = new CodeGenerator(reporter);
				generator.generateCode(theAST, args[0]);
				checkFailure(reporter, "code generation");
			}

			System.out.println("valid program");

			// return code for valid input
			System.exit(0);
		} catch (Exception e) {
			reporter.reportError("*** line " + scanner.currentPosition().lineNumber + ": compiler failed with " + e);

			// return code for invalid input
			System.exit(4);
		}
	}
}
