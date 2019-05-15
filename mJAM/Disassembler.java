/**
 * generate human readable mJAM instructions 
 * @author prins
 * @version COMP 520 V2.2
 */
package mJAM;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Disassemble the mJAM object code 
 * from input file xxx.mJAM 
 * into output file xxx.asm
 * 
 * @author prins
 * @version COMP 520 v2.2
 */
public class Disassembler {

	private String objectFileName;
	private String asmName;
	private FileWriter asmOut;
	private boolean error = false;
	private Map<Integer, String> addrToLabel;

	public Disassembler(String objectFileName) {
		this.objectFileName = objectFileName;
	}

	/**
	 * Writes the r-field of an instruction in the form "l<I>reg</I>r", where
	 * l and r are the bracket characters to use.
	 * @param leftbracket     the character to print before the register.
	 * @param r           the number of the register.
	 * @param rightbracket    the character to print after the register.
	 */
	private void writeR(char leftbracket, int r, char rightbracket) {
		asmWrite(Character.toString(leftbracket));
		asmWrite(Machine.intToReg[r].toString());
		asmWrite(Character.toString(rightbracket));
	}

	/**
	 * Writes a void n-field of an instruction.
	 */
	private void blankN() {
		asmWrite("      ");
	}

	// Writes the n-field of an instruction.
	/**
	 * Writes the n-field of an instruction in the form "(n)".
	 * @param n   the integer to write.
	 */
	private void writeN(int n) {
		asmWrite(String.format("%-6s","(" + n + ")"));
	}

	/**
	 * Writes the d-field of an instruction.
	 * @param d   the integer to write.
	 */
	private void writeD(int d) {
		asmWrite(Integer.toString(d));
	}

	/**
	 * Writes the name of primitive routine with relative address d.
	 * @param d   the displacment of the primitive routine.
	 */
	private void writePrimitive(int d) {
		Machine.Prim prim = Machine.intToPrim[d];
		asmWrite(String.format("%-8s",prim.toString()));
	}
		
	/**
	 * Writes the given instruction in assembly-code format.
	 * @param instr   the instruction to display.
	 */
	private void writeInstruction(Instruction instr) {

		String targetLabel = "***";
		// get label of destination addr, if instr transfers control
		if (instr.r == Machine.Reg.CB.ordinal())
			targetLabel = addrToLabel.get(instr.d);

		Machine.Op instruction = Machine.intToOp[instr.op];
		asmWrite(String.format("%-7s",instruction.toString()));
		switch (instruction) {
		case LOAD:
			blankN();
			writeD(instr.d);
			writeR('[', instr.r, ']');
			break;

		case LOADA:
			blankN();
			writeD(instr.d);
			writeR('[', instr.r, ']');
			break;

		case LOADI:
			break;

		case LOADL:
			blankN();
			writeD(instr.d);
			break;

		case STORE:
			blankN();
			writeD(instr.d);
			writeR('[', instr.r, ']');
			break;

		case STOREI:
			break;

		case CALL:
			if (instr.r == Machine.Reg.PB.ordinal()) {
				blankN();
				writePrimitive(instr.d);
			} else {
				blankN();
				asmWrite(targetLabel);
			}
			break;

		case CALLI:
			blankN();
			asmWrite(targetLabel);
			break;

		case RETURN:
			writeN(instr.n);
			writeD(instr.d);
			break;

		case CALLD:
			blankN();
			writeD(instr.d);
			break;

		case PUSH:
			blankN();
			writeD(instr.d);
			break;

		case POP:
			blankN();
			writeD(instr.d);
			break;

		case JUMP:
			blankN();
			asmWrite(targetLabel);
			break;

		case JUMPI:
			break;

		case JUMPIF:
			writeN(instr.n);
			asmWrite(targetLabel);
			break;

		case HALT:
			writeN(instr.n);
			break;

		default:
			asmWrite("????  ");
			writeN(instr.n);
			writeD(instr.d);
			writeR('[', instr.r, ']');
			break;
		}
	}

	/**
	 * disassembles program held in code store
	 */
	void disassembleProgram(String asmFileName) {

		try {
			asmOut = new FileWriter(asmFileName);
		} catch (IOException e) {
			System.out.println("Disassembler: can not create asm output file "
					+ asmName);
			error = true;
			return;
		}

		// collect all addresses that may be the target of a jump instruction
		SortedSet<Integer> targets = new TreeSet<Integer>();
		for (int addr = Machine.CB; addr < Machine.CT; addr++) {
			Instruction inst = Machine.code[addr];
			Machine.Op op = Machine.intToOp[inst.op];
			switch (op) {
			case CALL:
			case CALLI:
				// only consider calls (branches) within code memory (i.e. not primitives)
				if (inst.r == Machine.Reg.CB.ordinal())
					targets.add(inst.d);
				break;
			case JUMP:
				// address following an unconditional branch is an implicit target
				targets.add(addr+1);
				targets.add(inst.d);
				break;
			case JUMPIF:
				// a jump of any sort creates a branch target
				targets.add(inst.d);
				break;
			default:
				break;
			}
		}

		// map branch target addresses to unique labels
		addrToLabel = new HashMap<Integer, String>();
		int labelCounter = 10;
		for (Integer addr : targets) {
			String label = "L" + labelCounter++ ;
			addrToLabel.put(addr, label);
		}

		// disassemble each instruction
		for (int addr = Machine.CB; addr < Machine.CT; addr++) {
			
			// generate instruction address
			asmWrite(String.format("%3d  ", addr));

			// if this addr is a branch target, output label
			if (addrToLabel.containsKey(addr))
				asmWrite(String.format("%-7s", addrToLabel.get(addr) + ":"));
			else
				asmWrite("       ");

			// instruction
			writeInstruction(Machine.code[addr]);

			// newline 
			asmWrite("\n");
		}

		// close output file
		try {
			asmOut.close();
		} catch (IOException e) {
			error = true;
		}
	}

	private void asmWrite(String s) {
		try {
			asmOut.write(s);
		} catch (IOException e) {
			error = true;
		}
	}

	public static void main(String[] args) {
		System.out.println("********** mJAM Disassembler (1.0) **********");
		String objectFileName = "obj.mJAM";
		if (args.length == 1)
			objectFileName = args[0];
		Disassembler d = new Disassembler(objectFileName);
		d.disassemble();
	}

	/**
	 * Disassemble object file
	 * @return true if error encountered else false
	 */
	public boolean disassemble() {
		ObjectFile objectFile = new ObjectFile(objectFileName);

		// read object file into code store
		if (objectFile.read()) {
			System.out.println("Disassembler: unable to read object file"
					+ objectFileName);
			return true;
		}

		// assembler-code output file name
		if (objectFileName.endsWith(".mJAM"))
			asmName = objectFileName.substring(0, objectFileName.length() - 5)
					+ ".asm";
		else
			asmName = objectFileName + ".asm";

		// disassemble to file
		disassembleProgram(asmName);

		if (error) {
			System.out.println("Disassembler: unable to write asm file"
					+ asmName);
			return true;
		}
		
		return false;
	}
}
