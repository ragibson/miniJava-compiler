// PA1 parse parse pass
class MainClass {
   public static void main (String [] args) {
      SecondSubClass newobj = new SecondSubClass ();
   }
}
class SuperClass
{
   private void fillup (boolean open, int [] jar, int marble, int upto) {

      int index = 0;
      if (open == true) {
         while ( index < upto ) {
            ownjar [index] = jar [index];
	    jar [index] = marble;
         }  // while
      }  // if
   }  // fillup

}  // class SuperClass

