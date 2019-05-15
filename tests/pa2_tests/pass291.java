// PA2 ref ASTs pass
class Refs {

    void p() {
        a = 1+2+3;
        a.b = 4;
        a.b.c = 5;
        this = that;
        this.that = 6;
        x = a[7];
    	a[8] = c;
        c.p(9);
        Foo x = a.b.c.d;
        Foo [] af = new Foo [10];
        that = new Test();
    }
}
