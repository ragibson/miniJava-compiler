/*** line 9: cannot nonstatic field "pubfield" from a static context. 
 * COMP 520
 * Identification
 */
class TestClass {
        
    public static void StaticContext() {

        int x = TestClass.pubfield;
    }
        
    public int pubfield;
}
