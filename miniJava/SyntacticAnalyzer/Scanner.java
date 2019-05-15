package miniJava.SyntacticAnalyzer;

import java.io.*;
import miniJava.ErrorReporter;

public class Scanner {

	private InputStream inputStream;
	private ErrorReporter reporter;

	private char currentChar;
	private SourcePosition position;
	private StringBuilder currentSpelling;

	// true when end of line is found
	private boolean eot = false;

	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.inputStream = inputStream;
		this.reporter = reporter;
		this.position = new SourcePosition();

		// initialize scanner state
		readChar();
	}

	/**
	 * skip whitespace and scan next token
	 */
	public Token scan() {
		// start of a token: collect spelling and identify token kind
		currentSpelling = new StringBuilder();
		TokenKind kind;
		String spelling;

		do {
			// skip whitespace
			while (!eot && isWhitespace(currentChar))
				skipIt();

			kind = scanToken();
			spelling = currentSpelling.toString();

			// skip comments
			if (kind == TokenKind.DIVIDE && (currentChar == '/' || currentChar == '*')) {
				kind = null;
				currentSpelling.setLength(0);

				if (currentChar == '/') {
					do {
						skipIt();
					} while (currentChar != eolWindows && currentChar != eolUnix && !eot);
				} else if (currentChar == '*') {
					skipIt();
					do {
						if (currentChar == '*') {
							skipIt();
							if (currentChar == '/') {
								skipIt();
								break;
							} else {
								continue;
							}
						}
						skipIt();
					} while (!eot);
					
					if (eot) {
						// comment was never closed
						kind = TokenKind.ERROR;
					}
				}
			}
		} while (kind == null);

		// return new token
		return new Token(kind, spelling);
	}

	/**
	 * determine token kind
	 */
	public TokenKind scanToken() {

		if (eot)
			return (TokenKind.EOT);

		if (isAlphabetic(currentChar)) {
			// scan keywords and identifiers
			while (isAlphabetic(currentChar) || isDigit(currentChar) || currentChar == '_') {
				takeIt();
			}
			switch (currentSpelling.toString()) {
			case "boolean":
				return TokenKind.BOOLEAN;
			case "class":
				return TokenKind.CLASS;
			case "else":
				return TokenKind.ELSE;
			case "false":
				return TokenKind.FALSE;
			case "if":
				return TokenKind.IF;
			case "int":
				return TokenKind.INT;
			case "new":
				return TokenKind.NEW;
			case "null":
			    return TokenKind.NULL;
			case "private":
				return TokenKind.PRIVATE;
			case "public":
				return TokenKind.PUBLIC;
			case "return":
				return TokenKind.RETURN;
			case "static":
				return TokenKind.STATIC;
			case "this":
				return TokenKind.THIS;
			case "true":
				return TokenKind.TRUE;
			case "void":
				return TokenKind.VOID;
			case "while":
				return TokenKind.WHILE;
			}

			return TokenKind.ID;
		}

		// scan numbers
		if (isDigit(currentChar)) {    
			while (isDigit(currentChar)) {
				takeIt();
			}
			
            if (currentSpelling.charAt(0) == '0' && currentSpelling.length() > 1) {
                scanError("Scan Error: octal number '" + currentSpelling + "' not supported");
                return TokenKind.ERROR;
            }
			return TokenKind.NUM;
		}

		// scan operators and symbols
		switch (currentChar) {
		case '<':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return TokenKind.LESSEQUAL;
			}
			return TokenKind.LESSTHAN;
		case '>':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return TokenKind.GREATEREQUAL;
			}
			return TokenKind.GREATERTHAN;
		case '=':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return TokenKind.EQUALS;
			}
			return TokenKind.ASSIGN;
		case '!':
			takeIt();
			if (currentChar == '=') {
				takeIt();
				return TokenKind.NOTEQUAL;
			}
			return TokenKind.NOT;
		case '&':
			takeIt();
			if (currentChar == '&') {
				takeIt();
				return TokenKind.AND;
			}
			scanError("Encountered single '&'");
			return TokenKind.ERROR;
		case '|':
			takeIt();
			if (currentChar == '|') {
				takeIt();
				return TokenKind.OR;
			}
			scanError("Encountered single '|'");
			return TokenKind.ERROR;
		case '+':
			takeIt();
			return TokenKind.PLUS;
		case '-':
			takeIt();
			return TokenKind.MINUS;
		case '*':
			takeIt();
			return TokenKind.TIMES;
		case '/':
			takeIt();
			return TokenKind.DIVIDE;
		case '.':
			takeIt();
			return TokenKind.PERIOD;
		case ',':
			takeIt();
			return TokenKind.COMMA;
		case ';':
			takeIt();
			return TokenKind.SEMICOLON;
		case '{':
			takeIt();
			return TokenKind.LBRACE;
		case '}':
			takeIt();
			return TokenKind.RBRACE;
		case '[':
			takeIt();
			return TokenKind.LBRACKET;
		case ']':
			takeIt();
			return TokenKind.RBRACKET;
		case '(':
			takeIt();
			return TokenKind.LPAREN;
		case ')':
			takeIt();
			return TokenKind.RPAREN;
		default:
			scanError("Unrecognized character '" + currentChar + "' in input");
			return TokenKind.ERROR;
		}
	}

	private void takeIt() {
		currentSpelling.append(currentChar);
		nextChar();
	}

	private void skipIt() {    
		nextChar();
	}

	private boolean isAlphabetic(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private boolean isDigit(char c) {
		return (c >= '0') && (c <= '9');
	}

	private boolean isWhitespace(char c) {    
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}

	private void scanError(String m) {
		reporter.reportError("Scan Error:  " + m);
	}

	private final static char eolUnix = '\n';
	private final static char eolWindows = '\r';

	/**
	 * advance to next char in inputstream detect end of file or end of line as end
	 * of input
	 */
	private void nextChar() {
		if (!eot)
			readChar();
	}

    private void readChar() {        
        if (currentChar == '\t') {
            // assuming tabs are rendered as 4 spaces
            this.position.columnNumber += 4;
        } else if (currentChar == '\n') {
            this.position.lineNumber++;
            this.position.columnNumber = 1;
        } else {
            this.position.columnNumber++;
        }

        try {
            int c = inputStream.read();
            currentChar = (char) c;
            if (c == -1) {
                eot = true;
            }
        } catch (IOException e) {
            scanError("I/O Exception!");
            eot = true;
        }
    }
    
    public SourcePosition currentPosition() {
        return this.position.copy();
    }
}
