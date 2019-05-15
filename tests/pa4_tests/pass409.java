/**
 * COMP 520
 * Array update and array.length
 */
class MainClass {
   public static void main (String [] args) {

      int aa_length = 4;
      int [] aa = new int [aa_length];

      int i = 1;
      aa[0] = i;
      while (i < aa.length) {
          aa[i] = aa[i-1] + i;
          i = i + 1;
      }
      
      int tstvar = aa[3] + 2;
      System.out.println(tstvar);
   }
}
