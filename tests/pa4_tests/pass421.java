/**
 * COMP 520
 * Object Array creation
 */
class MainClass {
    
    public static void main (String [] args) {
       A [] aa = new A[2];
       A b = new A();
       b.x = 3;
       aa[0] = b;
       A c = new A();
       c.x = 5;
       aa[1] = c;

       A t = aa[0];
       A s = aa[1];

       int result = t.x + s.x + 13;  // 21
       System.out.println(result);
   }
}

class A {
    int x;
}
