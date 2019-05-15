/**
 * COMP 520
 * static methods and fields
 */
class B {

    public static int x;

    public static void foo(int y) {
	x = x + y;
	A.s = 2 + x; 
    }
}

// mainclass
class A {
    public static int s;
    
    public static void main (String [] args) {
	B.x = 3;
	B.foo(4);
	int z = B.x + s + 7;  // 23 
	System.out.println(z);
    }
}



