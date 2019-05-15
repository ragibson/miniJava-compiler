package miniJava.SyntacticAnalyzer;

/**
 * A token has a kind and a spelling. In a compiler it would also have a source
 * position
 */
public class Token {
	public TokenKind kind;
	public String spelling;
	public SourcePosition posn;

	public Token(TokenKind kind, String spelling) {
		this.kind = kind;
		this.spelling = spelling;
	}
}
