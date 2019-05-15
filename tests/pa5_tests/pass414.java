/**
 * COMP 520
 * Method invocation and recursion
 */
class MainClass {
   public static void main (String [] args) {

      MainClass m = new MainClass ();
      System.out.println(1 + m.fib(7));
   }

    public int fib(int n) {
        int res = -1;
        if (n <= 0)
            res = 0;
        else if (n == 1)
            res = 1;
        else 
            res = fib(n-1) + fib(n-2);
        return res;
    }
}

