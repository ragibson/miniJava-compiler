/*** line 8: reference "c" of type "int" cannot be qualified
 * COMP 520
 * Identification
 */
class Fail329 { 	
    public static void main(String[] args) {
        int c = 4;
        c = c.foo;
    }

    int foo;
}

