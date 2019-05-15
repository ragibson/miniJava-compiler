/*** line 14: method "f" parameter "x" type "int" incompatible with argument type "void" 
 * COMP 520
 * Type checking
 */
class Fail317 {
    public static void main(String[] args) {
        A a = new A();
        a.g();
    } 
}

class A{
    public void g() {
	int x = f(g());
    }
    
    public int f(int x) { 
        return 5;
    }
}

