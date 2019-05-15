/**
 * COMP 520
 * check short circuit conditional evaluation
 */
class MainClass {
    public static void main (String [] args) {
        
        MainClass m = new MainClass ();
        m.didrun = false;
        System.out.println(m.runTest(2));
    }

    private int runTest(int res) {
        boolean b = (false && dontrun(true)) 
            || (true || dontrun(false))
            || dontrun(false);

        if (b) {
            if (didrun)
                res = -2;
        }
        else
            res = -1;

        return res;
    }

    public boolean didrun;

    public boolean dontrun(boolean r) {
        didrun = true;
        return r;
    }
}

