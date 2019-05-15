// PA1 parse refs decls pass
class Test {

    int p() {
        this = that;
        this();
        this.that(5);
	this.that[2] = 3;
        this.that.those[3]= them;
        this.that.those();
        int [] x = 1;
        a b = c;
	p();
	p.b[4] = 5;
	p.b(3);
	int z = this.p(x) * that.q() + those.r[a.p];
    }
}

