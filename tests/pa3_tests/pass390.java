/**
 * COMP 520
 * IxRef
 */
class Pass390 {         

    public static void main(String[] args) {
        Arr2D m = new Arr2D();
	Arr1D r = m.row[1];
	int   x = r.col[3];
    }
}

class Arr2D { Arr1D [] row; }

class Arr1D { int [] col; }
