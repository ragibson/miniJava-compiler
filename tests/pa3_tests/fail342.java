/*** line 11: cannot reference "this" within a static context
 * COMP 520
 * Identification
 */
class TestClass {
        
    public static void staticContext() {
        TestClass t = this;
    }
}        
