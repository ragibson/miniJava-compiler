package miniJava;

/**
 * reports errors from different phases of compilation and maintains a count of
 * total errors for use in the compiler driver
 *
 */
public class ErrorReporter {

	private int numErrors;

	ErrorReporter() {
		numErrors = 0;
	}

	public boolean hasErrors() {
		return numErrors > 0;
	}

	public void reportError(String message) {
		System.out.println(message);
		numErrors++;
	}
}
