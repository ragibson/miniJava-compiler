/**
 * COMP 520
 * Fake and real println()
 *
 */
class MainClass {
    
    public static void main(String [] args) {
	Fake System = new Fake();
	System.init();
	System.out.println(-1);  // the fake println
    }
}

// this is just terrible
class Fake {
    Fake out;

    void init() {
	out = this;
    }

    void println(int x) {
	System.out.println(x + 27);  // the real println
    }
}
