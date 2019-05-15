/**
 * COMP 520
 *   codegen
 */
class pass508 {

    public static int t;
    public static int s;
    public int x;

    public static void main (String [] args) {
        
	s = 11;
	pass508 v = new pass508();
	v.x = 19;
	System.out.println(v.x - s);
    }
}
