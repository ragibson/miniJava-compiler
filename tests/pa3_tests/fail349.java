/*** line 8: field "privstatfield" has private visibility in class "Other"
 * COMP 520
 * Identification
 */
class TestClass {
        
    public static void staticContext() {
        int x = Other.privstatfield;
    }
}        

class Other {
    private static int privstatfield;
}
