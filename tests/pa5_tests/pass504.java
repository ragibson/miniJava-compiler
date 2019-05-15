/**
 * COMP 520
 *   check length field
 */
class MainClass {
    public static void main (String [] args) {
        
        MainClass m = new MainClass ();
        System.out.println(m.runTest(4));
    }

    public int runTest(int res) {
        int v = res;

        boolean expr = true;
        int [] a = new int [4];
        if (expr)
            a = new int [3];
        else
            a = new int [2];
        
        if (3 != a.length)
            v = -1;

        return v;
    }

}
