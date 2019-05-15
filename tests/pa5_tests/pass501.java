/**
 * COMP 520
 * check short circuit conditional evaluation
 */
class MainClass {
    public static void main (String [] args) {
        
        MainClass m = new MainClass ();
        m.didrun = false;
        System.out.println(m.runTest(1));
    }

    private int runTest(int res) {
        boolean t = true;
        boolean f = false;
        
        if (f && dontrun(t))
            res = -1;
        if (! (t || dontrun(t)))
            res = -1;
        if ( (t || dontrun(t)) && f && dontrun(t))
            res = -1;
        if ( ! ((t && t && f && dontrun(t)) || f || f || t || dontrun(t)) )
            res = -1;
        if (didrun)
            res = -1;
        return res;
    }

    public boolean didrun;

    public boolean dontrun(boolean r) {
        didrun = true;
        return r;
    }
}

