/*** line 11: cannot reference "x" within the initializing expression of the declaration for "x"
 * COMP 520
 * Identification
 */
class fail305 { 	

    int x;
    int y;

    public void foo() {
	int x = y + x;
    }
}

