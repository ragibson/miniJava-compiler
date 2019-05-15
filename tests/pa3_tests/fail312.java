/*** line 8: binary operator "+" expecting "int" type, received "void" type
 * COMP 520
 * Type Checking
 */
class fail312 { 	
    public static void main(String[] args) {
	fail312 f = new fail312();
	int x = 1 + f.noresult();
    }
    
    void noresult() {}
}


