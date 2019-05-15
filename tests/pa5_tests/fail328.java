/*** line 8: cannot qualify method "foo" in a reference
 * COMP 520
 * Identification
 */
class Fail328 { 	
    public static void main(String[] args) {
        F05 c = new F05();
        c = c.foo.next;
    }
}

class F05 {
    public F05 next;
    public F05 foo() {return this;}
}
