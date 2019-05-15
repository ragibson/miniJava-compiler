/** Line 11:  missing return statement in method "check"
 * COMP 520
 * Type checking
 */
class A {

    public static void main (String [] args) {
	int r = check(5);
    }
    
    static int check(int x){
	if (x != 5)
	    return 0;
	x = 5;
    }
}

