class TestClass {
        
                
    public static void staticContext() {

        TestClass t = null;
        int x = 0;

        /*
         * VALID
         */

        // IdRef
        // x = x;
        // x = pubstatfield;
        // x = privstatfield;
        x = pubstatfn();
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


