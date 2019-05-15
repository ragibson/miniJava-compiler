/*** line 11: cannot reference "this" within a static context
 * COMP 520
 * Identification
 */
class TestClass {
        
    public static void staticContext() {
        int t = this.statfield;
    }

    public static int statfield; 
}        
