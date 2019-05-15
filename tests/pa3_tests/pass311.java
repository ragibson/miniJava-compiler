/**
 * COMP 520
 * Array update
 */
class Pass311 {
   public static void main (String [] args) {

      int aa_length = 4;
      int [] aa = new int [aa_length];

      int i = 0;

      i = 1;
      aa[0] = i;
      while (i < aa_length) {
          aa[i] = aa[i-1] + i;
          i = i + 1;
      }
   }
}
