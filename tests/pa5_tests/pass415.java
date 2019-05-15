/**
 * COMP 520
 * Instance values
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

      this.n = 4;
      s.n = 5;
      System.out.println(2 + this.zoo (this, this.s));
   }

   public int foo (int x, int y)
   {
      return (n + x + y);
   }

   public int zoo (FirstClass ff, SecondClass ss)
   {
      return (ff.n + ss.n + this.n);
   }
}

class SecondClass
{
   int n;
   FirstClass f;

}
