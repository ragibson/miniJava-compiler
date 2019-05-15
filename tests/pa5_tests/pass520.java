/**
 * COMP 520
 *   codegen quality
 */
class pass520 {

    public static int t;
    public static int s;
    public int x;

    public static void main (String [] args) {
        
	s = 11;
	pass520 v = new pass520();
	v.x = 19;
	v.print(10);
    }

    private void print(int t) {
	System.out.println(x + s - t);
    }
}
