/**
 * COMP 520
 * Array creation and update
 */
class Pass312 {
    public static void main(String [] args) {
        int a = 2;
        int [] b = new int [ 2 * 5 - 3];
        b[0] = 13;
        boolean c = b[a-2] > a;
        if (c)
            b[a-1] = b[a-2];
    }
}
