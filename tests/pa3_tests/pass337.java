/**
 * COMP 520
 * Instance values
 */
class Pass337 {
    public static void main (String [] args) {
        
        FirstClass f = new FirstClass ();
        f = f.getAlternate(57);
        
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
