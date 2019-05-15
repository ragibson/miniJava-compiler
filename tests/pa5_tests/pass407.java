/**
 * COMP 520
 * Qualified references
 */
class MainClass {
   public static void main (String [] args) {

      FirstClass f = new FirstClass ();
      f.s = new SecondClass ();

      // write and then read;
      f.s.f = f;
      f.s.f.n = 7;
      int tstvar = f.n;

      System.out.println(tstvar);
   }
}

class FirstClass
{
   int n;
   SecondClass s;

}

class SecondClass
{
   int n;
   FirstClass f;

}



