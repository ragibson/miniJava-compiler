/*** line 10: binary operator "+" expecting "int" type, received "boolean" type
 * COMP 520
 * Type Checking
 */
class fail311 {

    // public static void main(String [] args) { }

    public int f() {
	return 1 + (2 < 3);
    }
}
