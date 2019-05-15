/**
 * COMP 520
 * stack deallocation
 */
class MainClass {
    public static void main (String [] args) {
        int tstvar = 24;
        
        int i = 0;
        while (i < 1025) {
            int j = i;
            i = i + 1;
        }
        
        System.out.println(tstvar);
    }
}


