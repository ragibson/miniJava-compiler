/**
 * COMP 520
 * Object creation and field reference
 */
class MainClass {
   public static void main (String [] args) {

       FirstClass f = new FirstClass ();
       int tstvar = 5 + f.n;

       System.out.println(tstvar);
   }
}

class FirstClass
{
   int n;

}



