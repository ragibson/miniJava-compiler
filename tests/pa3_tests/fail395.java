/*** line 9: cannot reference private field "oprivfield" outside of class "OtherClass" 
 * COMP 520
 * Identification
 */
class TestClass {
                
    public void nonStaticContext() {

        int x = pubstatfield.pubstatOther.oprivfield;
    }
        
    public static TestClass pubstatfield;   
    public static OtherClass pubstatOther;
}

class OtherClass {
        
    private int oprivfield;
}
