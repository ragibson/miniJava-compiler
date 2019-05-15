/**
 * COMP 520
 *   static visibility and access
 */
class fail501 {

    private static int s;

    public static void main (String [] args) {
        Foo.s = 1;
    }
}

class Foo {
    private static int s;
}
