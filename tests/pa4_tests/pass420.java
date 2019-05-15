/**
 * COMP 520
 *  IxQRef
 */
class MainClass {
 
    public static void main (String [] args) {
        
        A a = new A();
	a.iarr = new int [2];
	a.iarr[0] = 15;
	a.iarr[1] = 5 + a.iarr[0];
	System.out.println(a.iarr[1]);
    }
}

class A {
    private int x;
    public int [] iarr;
}

