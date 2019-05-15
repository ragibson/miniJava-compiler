/*** line 11: index expression does not have type "int"
 * COMP 520
 * Identification
 */
class Fail327 {
    // public static void main(String[] args) {}

    public int [] a;

    void f() {
	int x = a[a];
    }
}
