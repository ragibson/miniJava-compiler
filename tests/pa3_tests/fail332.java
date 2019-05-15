/*** line 8: binary operator "!=" requires arguments to have the same type.
 * COMP 520
 * Type checking
 */
class fail332 { 	
       
    public void foo () {
	boolean c = (new A() != new B());
    }
}

class A {
    int y;
}

class B {
    int x;
}
