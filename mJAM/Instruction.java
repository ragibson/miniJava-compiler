/**
 * mJAM instruction format
 * @author prins
 * @version COMP 520 V2.3
 */
package mJAM;

public class Instruction {

	public Instruction() {
		op = 0;
		r = 0;
		n = 0;
		d = 0;
	}

	public Instruction(int op, int n, int r, int d) {
		this.op = op;
		this.n = n;
		this.r = r;
		this.d = d;
	}

	// Java has no type synonyms, so the following representations are
	// assumed:
	//
	//  type
	//    OpCode = 0..15;  {4 bits unsigned}
	//    Register = 0..15; (4 bits unsigned)
	//    Length = 0..255;  {8 bits unsigned}
	//    Operand = -2147483648 .. +2147483647;  (32 bits signed for use with LOADL)
	public int op; // OpCode
	public int r;  // RegisterNumber
	public int n;  // Length
	public int d;  // Operand
}
