/*** line 7: cannot reference non-static symbol "x" in static context
 * COMP 520
 * Identification
 */
class fail318 { 	
    public static void main(String[] args) {
        int y = x + 3;
    }
    
    public int x;
}
