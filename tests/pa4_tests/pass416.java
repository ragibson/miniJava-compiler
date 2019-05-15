/**
 * COMP 520
 *   Local variable name can be reused in disjoint scope
 */
class MainClass {
   public static void main (String [] args) {

       int tstvar = 1;

       {
           int x = 5;  
           tstvar = tstvar + x;
       }

       tstvar = tstvar + 1;

       {
           int x = 9;  
           tstvar = tstvar + x;
       }
       
       System.out.println(tstvar);
   }
}

