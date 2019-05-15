/**
 * COMP 520
 *   static visibility and access
 */
class pass507 {
    public static void main (String [] args) {
        
        s = 6;
	pass507 v = new pass507();

	v.init();

        if (v.x == 7)
            System.out.println(v.x);
        else
            System.out.println(-1);
    }

    private static boolean b;
    private static int s;
    private int x;

    private void init() {
	x = s + 1;
    }
}
