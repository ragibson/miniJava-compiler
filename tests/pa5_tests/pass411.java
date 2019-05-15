/**
 * COMP 520
 * Field reference and update
 */
class MainClass {
   public static void main (String [] args) {

      FirstClass f = new FirstClass ();
      f.s = new SecondClass ();
      f.s.f = f;

      f.testme ();
   }
}

class FirstClass
{
   int n;
   SecondClass s;

   public void testme ()
   {
      int tstvar = 10;

      n = 11;
      tstvar = s.f.n;
      System.out.println(tstvar);
   }

}

class SecondClass
{
   int n;
   FirstClass f;
}

