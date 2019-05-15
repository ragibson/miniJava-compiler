/**
 * COMP 520
 * Type Checking
 */
class Pass322 {         
    public static void main(String[] args) {
        Pass322 a = new Pass322();
        boolean c = a.b() && a.p() == 5;                        
    }
        
    int p() {
	return 5;
    }
        
    boolean b() {
	return true == false;
    }
}

