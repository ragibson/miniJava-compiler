class TestClass {
        
    public void nonStaticContext() {

        TestClass t = null;
        OtherClass o = null;
        int x = 0;

        /*
         * VALID
         */

        // QualifiedRef 
        // x = OtherClass.opubstatTest.privfield;
        x = OtherClass.opubstatTest.privfn();
    }
        
        
    public int pubfield;
    private int privfield;
    public static int pubstatfield;
    private static int privstatfield;
        
    public int pubfn() { return 1; }
    private int privfn() { return 1; }
    public static int pubstatfn() { return 1; }
    private static int privstatfn() { return 1; }
        
    public OtherClass pubOther;
    private OtherClass privOther;
    public static OtherClass pubstatOther;
    private static OtherClass privstatOther;

}

class OtherClass {
        
    public int  opubfield;
    private int oprivfield;
    public static int opubstatfield;
    private static int oprivstatfield;
        
    public int opubfn() { return 1; }
    private int oprivfn() { return 1; }
    public static int opubstatfn() { return 1; }
    private static int oprivstatfn() { return 1; }
        
    public TestClass opubTest;
    private TestClass oprivTest;
    public static TestClass opubstatTest;
    private static TestClass oprivstatTest;

}
