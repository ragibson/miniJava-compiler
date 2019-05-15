/*** line 11: reference "x" is not an array
 * COMP 520
 * Identification or Type checking
 */
class Fail322 {
    // public static void main(String[] args) {}

    public int x;

    void f() {
	x = x[3];
    }
}
