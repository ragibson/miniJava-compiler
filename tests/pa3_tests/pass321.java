/**
 * COMP 520
 * Identification
 */
class Pass321 { 	
    public static void main(String[] args) {
        Pass321 p = new Pass321();
        p.next = p;
        p.next.next.x = 3;
    } 
    
    public Pass321 next;
    private int x;
}
