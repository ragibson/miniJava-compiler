class TestClass {
        
    public void nonStaticContext() {

        TestClass t = null;
        int x = 0;

        /*
         * VALID
         */

        // IdRef, ThisRef
        x = x;
    }
}        

