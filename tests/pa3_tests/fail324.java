/*** line 8: condition in "if" statement does not have type "boolean"
 * COMP 520
 * Type checking
 */
class Fail324 {
    public static void main(String [] args) {
        int x = 3;
	if (x) 
	    x = x + 1;
    }
}
