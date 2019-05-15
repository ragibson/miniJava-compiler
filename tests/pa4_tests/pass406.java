/**
 * COMP 520
 * Object creation and update
 */
class MainClass {
   public static void main (String [] args) {

      FirstClass f = new FirstClass ();
      f.s = new SecondClass ();

      // write and then read;
      f.n = 5;
      f.s.n = 1;
      
      int tstvar = f.n + f.s.n;
      
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



