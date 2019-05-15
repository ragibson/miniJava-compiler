/**
 * COMP 520
 * Method invocation
 */
class MainClass {
   public static void main (String [] args) {

      FirstClass f = new FirstClass ();
      f.testme ();
   }
}

class FirstClass
{
   int n;

   public void testme ()
   {
      int tstvar = 10;
      System.out.println(tstvar);
   }
}
