/**
 * COMP 520
 * check short circuit conditional evaluation
 */
class MainClass {
    public static void main (String [] args) {
        
        MainClass m = new MainClass ();
        m.didrun = false;
        int res = 22;
        
        boolean b = false && m.dontrun();
        boolean c = ! (true || m.dontrun());
        if (m.didrun)
            res = -1;
        System.out.println(res);
    }

    public boolean didrun;

    public boolean dontrun() {
        didrun = true;
        return true;
    }
}

