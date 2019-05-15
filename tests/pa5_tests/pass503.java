/**
 * COMP 520
 *   recursive calls with multiple arguments
 */
class MainClass {
    public static void main (String [] args) {
        
        MainClass m = new MainClass ();
        System.out.println(m.runTest(3));
    }

    private int runTest(int res) {
        int v = res;
        if ( 3 != gcd(9,33))
            v = -1;
        return v;
    }

    private int gcd(int n, int m) {
        int r = n;
        if (n > m)
            r = gcd(n - m, m);
        if (n < m)
            r = gcd(n, m - n);
        return r;
    }
}
