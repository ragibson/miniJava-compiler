class TestClass {
        
    public static void staticContext() {

        TestClass t = null;
        int x = 0;

        /*
         * VALID
         */

        // QualifiedRef 
        // x = t.pubfield;
        // x = t.privfield;
        // x = t.pubstatfield;
        // x = t.privstatfield;
        // x = t.pubstatfn();
        x = t.privstatfn();
    }
        
       
    public int pubfield;
    private int privfield;
    public static int pubstatfield;
    private static int privstatfield;
        
    public int pubfn() { return 1; }
    private int privfn() { return 1; }
    public static int pubstatfn() { return 1; }
    private static int privstatfn() { return 1; }

}
