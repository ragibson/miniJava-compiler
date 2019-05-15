/** line 8: reference is not an assignable destination
 * COMP 520
 * Identification
 */
class MainClass {
   public static void main (String [] args) {
       int [] b = new int[10];
       b.length = -1;
   }
}

