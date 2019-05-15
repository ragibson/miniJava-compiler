/*** line 11: variable "x "is already defined in method "foo"
 * COMP 520
 * Identification
 */
class fail304 { 	
    //  public static void main(String[] args) {}
    
    public void foo(int parm) {
        int x = 0;
        {
            int x = 4;
        }
    }
}
