/*** line 12: cannot return a value from a void method
 * COMP 520
 * Type Checking
 */
class fail313 { 	
    public static void main(String[] args) {
	fail313 f = new fail313();
	f.noresult();
    }
    
    public void noresult() {
	return 10;
    }
}
