class TestClass {
        
    public static void staticContext() {

        TestClass t = null;
        int x = 0;

        /*
         * VALID
         */

        // QualifiedRef 
        // x = TestClass.pubstatfield;
        x = TestClass.privstatfield;
    }
        
       
    public int pubfield;
    private int privfield;
    public static int pubstatfield;
    private static int privstatfield;
}

