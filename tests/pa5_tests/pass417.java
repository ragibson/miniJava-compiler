/**
 * COMP 520
 * Instance values
 */
class MainClass {
    public static void main (String [] args) {
        
        FirstClass f = new FirstClass ();
        f.n = -1;
        f = f.getAlternate(17);
        System.out.println(f.n);
    }
}

class FirstClass
{
    public int n;
    
    public FirstClass getAlternate(int v) {

        FirstClass fc = new FirstClass();
        fc.n = v;
        
        FirstClass rc = fc.getThis();
        return rc;
    }

    public FirstClass getThis() {
        return this;
    }
}
