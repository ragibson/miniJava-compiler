/* miniJava test program
 * COMP 520 
 * bubblesort using a doubly-linked list
 */
class pass510 {
    public static void main(String[] args){
        BBS b = new BBS();
	b.start(10);
    }
}

class Number {
    public Number prev;
    public Number next;
    public int val;
}

// This class creates a doubly-linked list of integers and
// sorts the list using Bubblesort
class BBS{
    
    int size;
    Number leftSentinel;
    Number rightSentinel;

    // create list, sort, and check result.
    public void start(int size){
        this.size = size;
        init();
	sort();
	if (check())
            System.out.println(10);
        else
            System.out.println(-1);
    }

 
    // Sort list using Bubblesort
    private void sort(){
	Number cur = leftSentinel;
        Number next = cur.next;
	while (cur != rightSentinel) {
	    if (cur.val <= next.val) { 
                // advance
                cur = next;
                next = cur.next;
            }
	    else { 
                // swap and back up
                int t = cur.val;
                cur.val = next.val;
                next.val = t;
                next = cur;
                cur  = cur.prev;
	    }
	}
    }

    // Is list in increasing order? 
    private boolean check(){
        boolean ordered = true;
	Number cur = leftSentinel;
	while (cur != rightSentinel) {
            if (cur.val >= cur.next.val)
                ordered = false;
            cur = cur.next;
	}
        return ordered;
    }
    
    // Initialize doubly-linked list of random perm 1..size
    private void init(){

        int incr = 17; // gcd(size,incr) == 1
        int i = 1;

        leftSentinel = new Number();
        leftSentinel.val = -1;
        Number prev = leftSentinel;

        while (i <= size) {
            Number next = new Number();
            next.val = 1 + mod(i * incr, size);
            prev.next = next;
            next.prev = prev;

            prev = next;
            i = i + 1;
        }

        rightSentinel = new Number();
        rightSentinel.val = size + 100;
        rightSentinel.prev = prev;
        prev.next = rightSentinel;
    }

    // a mod b
    private int mod(int a, int b) {
        return a - (a/b)*b;
    }
}
