/**
 * COMP 520
 * delete non-void return value in a CallStatement
 */
class MainClass {
    public static void main (String [] args) {
        
        MainClass m = new MainClass ();
        int i = 0;
        while (i < 1025)
	{
            m.setup();
	    i = i + 1;
	}
        System.out.println(25);
    }
    
    public int setup() {
        return(55);
    }
}

