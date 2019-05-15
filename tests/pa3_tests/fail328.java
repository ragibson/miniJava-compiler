/*** line 10: reference "d" of type "D []" does not have a public field "x"
 * COMP 520
 * Identification
 */
class Fail328 {

    public D [] d;

    public void f() {
	int y = d.x;
    }
}

class D { public int x; }
