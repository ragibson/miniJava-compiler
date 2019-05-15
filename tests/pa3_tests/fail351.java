/*** line 11: reference "pubfn" does not denote a field or a variable
 * COMP 520
 * Identification
 */
class TestClass {
        
    public static void staticContext() {

        int x = 0;

        x = pubfn;
    }
        
        
    public static int pubfn() { return 1; }
}
