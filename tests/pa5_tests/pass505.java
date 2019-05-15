/**
 * COMP 520
 *   array length in qualified ref
 */
class MC {
    public static void main (String [] args) {
        
        MC mi = new MC();
        AC ai = new AC();
        
        mi.a  = ai;
        ai.m  = mi;

        mi.r  = new int [3];

        if (mi.a.m.r.length == 3)
            System.out.println(5);
        else
            System.out.println(-1);
    }

    public AC a;
    public int [] r;
}

class AC {
    MC m;
}
