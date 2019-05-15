/**
 * Interprets mJAM programs
 * @author prins
 * @version COMP 520 V2.3
 */
package mJAM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

// import mJAM.Machine.Reg;

public class Interpreter {

	// DATA STORE
	static int[] data = new int[1024];

	// DATA STORE REGISTERS AND OTHER REGISTERS
	final static int CB = 0, SB = 0, HB = 1024; // = upper bound of data array + 1
										

	static int CT, CP, ST, HT, LB, OB, status, temp;

	// machine status values
	final static int running = 0, halted = 1, failedDataStoreFull = 2,
			failedInvalidCodeAddress = 3, failedInvalidInstruction = 4,
			failedOverflow = 5, failedZeroDivide = 6, failedIOError = 7,
			failedArrayIndex = 8, failedNullRef = 9, failedHeapRef =10,
			failedFieldIndex = 11, failedMethodIndex = 12;

	static long accumulator;

	// Debugger state
	enum DebuggerStatus {
		PAUSED, RUNNING
	}

	static DebuggerStatus debuggerStatus = DebuggerStatus.PAUSED;
	static ArrayList<Integer> breakpoints = new ArrayList<Integer>();
	static ArrayList<String> sourceLines;

	static int content(int r) {
		// Returns the current content of register r,
		Machine.Reg reg = Machine.intToReg[r];
		switch (reg) {
		case CB:
			return CB;
		case CT:
			return CT;
		case PB:
			return Machine.PB;
		case PT:
			return Machine.PT;
		case SB:
			return SB;
		case ST:
			return ST;
		case HB:
			return HB;
		case HT:
			return HT;
		case LB:
			return LB;
		case OB:
			return OB;
		case CP:
			return CP;
		default:
			return 0;
		}
	}

	// PROGRAM STATUS

	static void dump() {
		// Writes a summary of the machine state.
		int addr, dynamicLink;
		System.out.println("");
		System.out.println("At instruction " + CP
				+ ", state of mJAM data store and registers is:");
		System.out.println("");
		if (HT == HB)
			System.out.println("            |--------|          (heap is empty)");
		else {
			System.out.println("      HB--> ");
			System.out.println("            |--------|");
			for (addr = HB - 1; addr >= HT; addr--) {
				System.out.print(rightPad(6, addr + ":"));
				if (addr == OB)
					System.out.print("OB--> ");
				else if (addr == HT)
					System.out.print("HT--> ");
				else
					System.out.print("      ");
				System.out.println("|" + leftPad(8, String.valueOf(data[addr]))
						+ "|");
			}
			System.out.println("            |--------|");
		}
		System.out.println("            |////////|");
		System.out.println("            |////////|");
		if (ST == SB)
			System.out.println("            |--------|          (stack is empty)");
		else {
			dynamicLink = LB;
			System.out.println("      ST--> |////////|");
			System.out.println("            |--------|");
			for (addr = ST - 1; addr >= SB; addr--) {
				System.out.print(rightPad(6, addr + ": "));
				if (addr == SB)
					System.out.print("SB--> ");
				else if (addr == LB)
					System.out.print("LB--> ");
				else
					System.out.print("      ");
				if ((addr == dynamicLink) && (dynamicLink != SB))
					System.out.print("|OB="
							+ leftPad(5, String.valueOf(data[addr])) + "|");
				else if ((addr == dynamicLink + 1) && (dynamicLink != SB))
					System.out.print("|DL="
							+ leftPad(5, String.valueOf(data[addr])) + "|");
				else if ((addr == dynamicLink + 2) && (dynamicLink != SB))
					System.out.print("|RA="
							+ leftPad(5, String.valueOf(data[addr])) + "|");
				else
					System.out.print("|"
							+ leftPad(8, String.valueOf(data[addr])) + "|");
				System.out.println("");
				if (addr == dynamicLink) {
					System.out.println("            |--------|");
					dynamicLink = data[addr + 1];
				}
			}
		}
		System.out.println("");
	}

	private static String leftPad(int len, String s) {
		int aLen = Math.max(len, s.length());
		StringBuffer buf = new StringBuffer(s);
		String r = buf.insert(0, "        ").toString();
		return r.substring(r.length() - aLen, r.length());
	}

	private static String rightPad(int len, String s) {
		int aLen = Math.max(len, s.length());
		String r = s + "        ";
		return r.substring(0, aLen);
	}

	static void showStatus() {
		// Writes an indication of whether and why the program has terminated.
		System.out.println("");
		System.out.print("*** ");
		switch (status) {
		case running:
			System.out.println("Program is running.");
			break;
		case halted:
			System.out.println("Program has halted normally.");
			break;
		case failedDataStoreFull:
			System.out.println("Program has failed due to exhaustion of Data Store.");
			break;
		case failedInvalidCodeAddress:
			System.out.println("Program has failed due to an invalid code address.");
			break;
		case failedInvalidInstruction:
			System.out.println("Program has failed due to an invalid instruction.");
			break;
		case failedOverflow:
			System.out.println("Program has failed due to overflow.");
			break;
		case failedZeroDivide:
			System.out.println("Program has failed due to division by zero.");
			break;
		case failedIOError:
			System.out.println("Program has failed due to an IO error.");
			break;
		case failedArrayIndex:
			System.out.println("Program has failed due to an array index error.");
			break;
		case failedNullRef:
			System.out.println("Program has failed due to a null pointer reference.");
			break;
		case failedHeapRef:
			System.out.println("Program has failed due to an invalid Heap reference.");
			break;
		case failedFieldIndex:
			System.out.println("Program has failed due to a field index error.");
			break;
		case failedMethodIndex:
			System.out.println("Program has failed due to an improper method index in CALLD.");
			break;
		default:
			System.out.println("Machine is in an unknown state.");
			break;
		}
		if (status != halted)
			dump();
	}

	// INTERPRETATION

	static void checkSpace(int spaceNeeded) {
		// Signals failure if there is not enough space to expand the stack or
		// heap by spaceNeeded.
		if (HT - ST < spaceNeeded)
			status = failedDataStoreFull;
	}

	static boolean invalidHeapRef(int addr) {
		// if addr is null ptr or outside of heap bounds, sets status to failure
		if (addr == Machine.nullRep)
			status = failedNullRef;
		else if (addr < HT + 2 || addr > HB)
			status = failedHeapRef;
		return (status != running);
	}

	static boolean isTrue(int datum) {
		// Tests whether the given datum represents true.
		return (datum == Machine.trueRep);
	}

	static int overflowChecked(long datum) {
		// Signals failure if the datum is too large to fit into a single word,
		// otherwise returns the datum as a single word.
		if ((Machine.minintRep <= datum) && (datum <= Machine.maxintRep))
			return (int) datum;
		else {
			status = failedOverflow;
			return 0;
		}
	}

	static int toInt(boolean b) {
		return b ? Machine.trueRep : Machine.falseRep;
	}

	static int currentChar;

	static int readInt() throws java.io.IOException {
		int temp = 0;
		int sign = 1;

		do {
			currentChar = System.in.read();
		} while (Character.isWhitespace((char) currentChar));

		if ((currentChar == '-') || (currentChar == '+'))
			do {
				sign = (currentChar == '-') ? -1 : 1;
				currentChar = System.in.read();
			} while ((currentChar == '-') || currentChar == '+');

		if (Character.isDigit((char) currentChar))
			do {
				temp = temp * 10 + (currentChar - '0');
				currentChar = System.in.read();
			} while (Character.isDigit((char) currentChar));

		return sign * temp;
	}

	// Invoke primitive operation with argument(s) on the stack
	// primitives are static and are not supplied an instance on the stack.
	static void callPrimitive(int id) {

		int addr, size, index;
		char ch;

		Machine.Prim prim = Machine.intToPrim[id];
		switch (prim) {
		case id:
			break; // nothing to be done
		case not:
			data[ST - 1] = toInt(!isTrue(data[ST - 1]));
			break;
		case and:
			ST = ST - 1;
			data[ST - 1] = toInt(isTrue(data[ST - 1]) & isTrue(data[ST]));
			break;
		case or:
			ST = ST - 1;
			data[ST - 1] = toInt(isTrue(data[ST - 1]) | isTrue(data[ST]));
			break;
		case succ:
			data[ST - 1] = overflowChecked(data[ST - 1] + 1);
			break;
		case pred:
			data[ST - 1] = overflowChecked(data[ST - 1] - 1);
			break;
		case neg:
			data[ST - 1] = overflowChecked(-data[ST - 1]);
			break;
		case add:
			ST = ST - 1;
			accumulator = data[ST - 1];
			data[ST - 1] = overflowChecked(accumulator + data[ST]);
			break;
		case sub:
			ST = ST - 1;
			accumulator = data[ST - 1];
			data[ST - 1] = overflowChecked(accumulator - data[ST]);
			break;
		case mult:
			ST = ST - 1;
			accumulator = data[ST - 1];
			data[ST - 1] = overflowChecked(accumulator * data[ST]);
			break;
		case div:
			ST = ST - 1;
			accumulator = data[ST - 1];
			if (data[ST] != 0)
				data[ST - 1] = (int) (accumulator / data[ST]);
			else
				status = failedZeroDivide;
			break;
		case mod:
			ST = ST - 1;
			accumulator = data[ST - 1];
			if (data[ST] != 0)
				data[ST - 1] = (int) (accumulator % data[ST]);
			else
				status = failedZeroDivide;
			break;
		case lt:
			ST = ST - 1;
			data[ST - 1] = toInt(data[ST - 1] < data[ST]);
			break;
		case le:
			ST = ST - 1;
			data[ST - 1] = toInt(data[ST - 1] <= data[ST]);
			break;
		case ge:
			ST = ST - 1;
			data[ST - 1] = toInt(data[ST - 1] >= data[ST]);
			break;
		case gt:
			ST = ST - 1;
			data[ST - 1] = toInt(data[ST - 1] > data[ST]);
			break;
		case eq:
			ST = ST - 1;
			data[ST - 1] = toInt(data[ST - 1] == data[ST]);
			break;
		case ne:
			ST = ST - 1;
			data[ST - 1] = toInt(data[ST - 1] != data[ST]);
			break;
		case eol:
			data[ST] = toInt(currentChar == '\n');
			ST = ST + 1;
			break;
		case eof:
			data[ST] = toInt(currentChar == -1);
			ST = ST + 1;
			break;
		case get:
			ST = ST - 1;
			addr = data[ST];
			try {
				currentChar = System.in.read();
			} catch (java.io.IOException s) {
				status = failedIOError;
			}
			data[addr] = (int) currentChar;
			break;
		case put:
			ST = ST - 1;
			ch = (char) data[ST];
			System.out.print(ch);
			break;
		case geteol:
			try {
				while ((currentChar = System.in.read()) != '\n')
					;
			} catch (java.io.IOException s) {
				status = failedIOError;
			}
			break;
		case puteol:
			System.out.println("");
			break;
		case getint:
			ST = ST - 1;
			addr = data[ST];
			try {
				accumulator = readInt();
			} catch (java.io.IOException s) {
				status = failedIOError;
			}
			data[addr] = (int) accumulator;
			break;
		case putint:
			ST = ST - 1;
			accumulator = data[ST];
			System.out.print(accumulator);
			break;
		// output with prefix for tester
		case putintnl:
			ST = ST - 1;
			accumulator = data[ST];
			System.out.print(">>> " + accumulator + "\n");
			break;
		case alloc:
			size = data[ST - 1];
			checkSpace(size);
			HT = HT - size;
			data[ST - 1] = HT;
			break;
		case dispose:
			ST = ST - 1; // no action taken at present
			break;
		case newobj:
			// ..., class obj addr, number of fields ==> ..., new obj addr
			size = data[ST - 1] + 2; // number of fields + 2 word descriptor
			checkSpace(size);
			HT = HT - size;          // reserve space
			data[HT] = data[ST - 2]; // set class object addr
			data[HT + 1] = size - 2; // set size of object
			data[ST - 2] = HT + 2;   // addr of new object instance, returned on stack
			ST = ST - 1;             // net effect of pop 2 args, push 1 result
			for (int i = 2; i < size; i++) {
				data[HT + i] = 0;    // zero all fields of new object
			}
			break;
		case newarr:
			// ..., number of elements ==> ..., new int[] addr
			size = data[ST - 1] + 2;  // array  + 2 word descriptor
			checkSpace(size);
			HT = HT - size;
			data[HT] = -2;            // tag for array
			data[HT + 1] = size - 2;  // size of array
			data[ST - 1] = HT + 2;    // addr of array instance, returned on stack
			for (int i = 2; i < size; i++) {
				data[HT + i] = 0;     // zero all elements of new array
			}
			break;
		case arraylen:
			// ..., array addr a ==> ... , array.length
			addr = data[ST-1];
			if (invalidHeapRef(addr))
				break;                   // invalid heap address
			if (data[addr - 2] != -2) {
				status = failedHeapRef;  // not tagged as array in heap
				break;
			}
			data[ST-1] = data[addr - 1]; // return array length field
			break;
		case arrayref:
			// ..., array addr a, element index i ==> ..., a[i]
			addr = data[ST - 2];
			if (invalidHeapRef(addr))
				break;
			index = data[ST - 1];
			if (data[addr - 2] != -2 || index < 0 || index >= data[addr - 1]) {
				status = failedArrayIndex;
				break;
			}
			data[ST - 2] = data[addr + index];  // result element, returned on stack
			ST = ST - 1;                        // pop two args, return one result
			break;
		case arrayupd:
			// ..., array addr a, element index i, new value v ==> ...
			// and a[i] := v
			addr = data[ST - 3];
			if (invalidHeapRef(addr))
				break;
			index = data[ST - 2];
			if (data[addr - 2] != -2 || index < 0 || index >= data[addr - 1]) {
				status = failedArrayIndex;
				break;
			}
			data[addr + index] = data[ST - 1]; // update array element
			ST = ST - 3;                       // pop 3 args, return no result
			break;
		case fieldref:
			// ..., obj addr a, field index i ==> ..., value of ith field of a
			addr = data[ST - 2];
			if (invalidHeapRef(addr))
				break;
			index = data[ST - 1];
			if (index < 0 || index >= data[addr - 1]) {
				status = failedFieldIndex;
				break;
			}
			data[ST - 2] = data[addr + index];   // field to stack top
			ST = ST - 1;                         // pop two args, return one result
			break;
		case fieldupd:
			// ..., obj addr a, field index i, new value v ==> ...
			// and a.i := v
			addr = data[ST - 3];
			if (invalidHeapRef(addr))
				break;
			index = data[ST - 2];
			if (index < 0 || index >= data[addr - 1]) {
				status = failedFieldIndex;	
				break;	
			}	
			data[addr + index] = data[ST - 1];	// update field to new value
			ST = ST - 3;						// pop 3 args, return no result
			break;
		}
	}

	static void interpretOneOperation() {
		// Fetch instruction ...
		Instruction currentInstr = Machine.code[CP];
		// Decode instruction ...
		int op = currentInstr.op;
		int r = currentInstr.r;
		int n = currentInstr.n;
		int d = currentInstr.d;
		int addr;
		// Execute instruction ...

		Machine.Op operation = Machine.intToOp[op];

		switch (operation) {
		case LOAD:
			addr = d + content(r);
			checkSpace(1);
			data[ST] = data[addr];
			ST = ST + 1;
			CP = CP + 1;
			break;
		case LOADA:
			addr = d + content(r);
			checkSpace(1);
			data[ST] = addr;
			ST = ST + 1;
			CP = CP + 1;
			break;
		case LOADI:
			ST = ST - 1;
			addr = data[ST];
			checkSpace(1);
			data[ST] = data[addr];
			ST = ST + 1;
			CP = CP + 1;
			break;
		case LOADL:
			checkSpace(1);
			data[ST] = d;
			ST = ST + 1;
			CP = CP + 1;
			break;
		case STORE:
			addr = d + content(r);
			ST = ST - 1;
			data[addr] = data[ST];
			CP = CP + 1;
			break;
		case STOREI:
			ST = ST - 1;
			addr = data[ST];
			ST = ST - 1;
			data[addr] = data[ST];
			CP = CP + 1;
			break;
			
		case CALL:
			// call static method, including primitives
			// arguments are on stack
			addr = d + content(r);      // effective address
			if (addr >= Machine.PB) {
				callPrimitive(addr - Machine.PB);
				CP = CP + 1;
			} else {
				// static method in code segment, no instance addr on stack
				checkSpace(3);
				data[ST] = OB;         // save caller OB in callee frame
				data[ST + 1] = LB;     // save caller LB in callee frame (dynamic link)
				data[ST + 2] = CP + 1; // save caller return address in callee frame
				OB = Machine.nullRep;  // set callee OB (null since no instance) 
				LB = ST;               // set LB = start of callee frame
				ST = ST + 3;           // set ST = end of callee frame
				CP = addr;             // execution resumes at addr specified in CALL inst
			}
			break;
			
		case CALLI:
			// call instance method
			// arguments on stack, followed by instance address
			addr = d + content(r);      // effective address
			if (addr >= Machine.CT) {
				// no instance methods outside of code segment
				status = failedInvalidInstruction;
				break;
			}
			// instance address is last arg on stack and is overwritten by frame
			checkSpace(2);
			temp = data[ST - 1];   // save instance address temporarily
			data[ST - 1] = OB;     // save caller OB in callee frame
			data[ST] = LB;         // save caller LB in callee frame (dynamic link)
			data[ST + 1] = CP + 1; // save caller return address in callee frame
			OB = temp;             // set OB for callee
			LB = ST - 1;           // set LB = start of callee frame
			ST = ST + 2;           // set ST = end of callee frame 
			CP = addr;             // execution resumes at addr specified in CALL inst
			break;
			
		case RETURN: 
			// d = number of method args (does not include instance addr for CALLI)
			// n = size of result (0 or 1)
			if (n < 0 || n > 1) {
				status = failedInvalidInstruction;
				break;
			}
			addr = LB - d;          // addr of caller args
			OB = data[LB];          // restore caller OB, LB, CP
			CP = data[LB + 2];
			LB = data[LB + 1];
			if (n == 1)
				data[addr] = data[ST - 1];  // return value if any
			ST = addr + n;          // caller stack top
			break;
			
		case CALLD: 
			// dynamic method dispatch of method with index d (origin 0)
			// arguments on stack, followed by instance addr
			{
				addr = data[ST - 1];    // instance addr
				if (invalidHeapRef(addr))
					break;
				int classDescAddr = data[addr - 2];
				if (classDescAddr >= ST || classDescAddr <= SB || d >= data[classDescAddr + 1] || d < 0) {
					status = failedMethodIndex;
					break;
				}
				ST = ST - 1;
				checkSpace(3);
				data[ST] = OB;
				data[ST + 1] = LB;
				data[ST + 2] = CP + 1;
				OB = addr;
				LB = ST;
				ST = ST + 3;
				CP = data[classDescAddr + 2 + n];
			}
			break;
		case PUSH: // push d elements on stack
			checkSpace(d);
			ST = ST + d;
			CP = CP + 1;
			break;
		case POP: // pop d elements off stack
			ST = ST - d;
			CP = CP + 1;
			break;
		case JUMP:
			CP = d + content(r);
			break;
		case JUMPI:
			ST = ST - 1;
			CP = data[ST];
			break;
		case JUMPIF:
			ST = ST - 1;
			if (data[ST] == n)
				CP = d + content(r);
			else
				CP = CP + 1;
			break;
		case HALT:
			if (n > 0) {
				// halt n > 0 --> snapshot machine state and continue execution
				dump();
				CP = CP + 1;
			} else
				status = halted;
			break;
		}

		if ((CP < CB) || (CP >= CT))
			status = failedInvalidCodeAddress;

		if (breakpoints.indexOf(CP) != -1) {
			debuggerStatus = DebuggerStatus.PAUSED;
			System.out.println("Breakpoint hit: " + sourceLines.get(CP));
		}
	}

	static void initMachine() {
		// Initialize registers ...
		ST = SB;
		HT = HB;
		LB = SB;
		CP = CB;
		OB = -1; // invalid instance addr
		CT = Machine.CT;
		status = running;
	}

	static void interpretProgram() {
		// Runs the program in code store.
		initMachine();
		do {
			interpretOneOperation();
		} while (status == running);
	}

	static void runProgramFromStart() {
		initMachine();
		continueProgram();
	}

	static void continueProgram() {
		debuggerStatus = DebuggerStatus.RUNNING;
		do {
			interpretOneOperation();
		} while (status == running && debuggerStatus == DebuggerStatus.RUNNING);
	}

	static void printHelp() {
		String[] help = {
				"p or print:",
				"     print entire machine state",
				"l or list [offset] [size]:",
				"     print the instructions around CP + offset, with size lines on either side",
				"     offset = 0 and size = 2 by default",
				"b or break [address]:",
				"     set a breakpoint at address",
				"     address = CP by default",
				"del:",
				"     delete one or more breakpoints",
				"n or next:",
				"     execute one instruction",
				"c or continue:",
				"     continue running the program from current position, until next breakpoint or completion",
				"r or run:",
				"     run the program from start, until next breakpoint or completion",
				"i or info:", "     list the current breakpoints",
				"q, quit or <EOF>:", "     quit the debugger",
				"Simply press enter to repeat the last command", "? or help:",
				"     print this help" };

		for (String line : help) {
			System.out.println("  " + line);
		}
	}

	static void debugProgram() {
		initMachine();

		BufferedReader inputReader = new BufferedReader(new InputStreamReader(
				System.in));

		String lastCommand = "";

		while (true) {
			System.out.print("\n: ");
			String inputLine;

			try {
				inputLine = inputReader.readLine();
			} catch (IOException e) {
				return;
			}

			if (inputLine == null)
				return;

			Scanner scanner = new Scanner(inputLine);
			String command = (scanner.hasNext() ? scanner.next() : lastCommand);
			lastCommand = command;

			if (command.equals("?") || command.equals("help")) {
				printHelp();
			} else if (command.equalsIgnoreCase("p")
					|| command.equalsIgnoreCase("print")) {
				dump();
			} else if (command.equalsIgnoreCase("l")
					|| command.equalsIgnoreCase("list")) {
				int offset = 0, size = 2;
				if (scanner.hasNextInt())
					offset = scanner.nextInt();
				if (scanner.hasNextInt())
					size = scanner.nextInt();

				for (int i = CP + offset - size; i <= CP + offset + size; ++i) {
					if (i >= 0 && i < sourceLines.size())
						System.out.println((i == CP ? " >" : "  ")
								+ sourceLines.get(i));
				}
			} else if (command.equalsIgnoreCase("b")
					|| command.equalsIgnoreCase("break")) {
				int addr = scanner.hasNextInt() ? scanner.nextInt() : CP;
				if (!breakpoints.contains(addr))
					breakpoints.add(addr);
				System.out.println("Added breakpoint at "
						+ sourceLines.get(addr));
			} else if (command.equalsIgnoreCase("del")) {
				while (scanner.hasNextInt()) {
					int addr = scanner.nextInt(), idx = breakpoints
							.indexOf(addr);
					if (idx != -1) {
						breakpoints.remove(idx);
					} else {
						System.out.println("No breakpoint at " + addr);
					}
				}
			} else if (command.equalsIgnoreCase("n")
					|| command.equalsIgnoreCase("next")) {
				if (status == running) {
					interpretOneOperation();
				} else {
					System.out.println("Program is not running");
				}
			} else if (command.equalsIgnoreCase("c")
					|| command.equalsIgnoreCase("continue")) {
				continueProgram();
			} else if (command.equalsIgnoreCase("r")
					|| command.equalsIgnoreCase("run")) {
				runProgramFromStart();
			} else if (command.equalsIgnoreCase("i")
					|| command.equalsIgnoreCase("info")) {
				System.out.println("Breakpoints:");
				for (int b : breakpoints) {
					System.out.println("\t" + sourceLines.get(b));
				}
			} else if (command.equalsIgnoreCase("q")
					|| command.equalsIgnoreCase("quit")) {
				scanner.close();
				return;
			} else {
				System.out.println("Unknown command '" + command
						+ "'. Type 'help' for a list of commands");
			}
			scanner.close();
		}
	}

	// RUNNING

	public static void main(String[] args) {
		System.out.println("********** mJAM Interpreter (Version 2.3) **********");

		String objectFileName;
		if (args.length >= 1)
			objectFileName = args[0];
		else
			objectFileName = "obj.mJAM";

		String sourceFileName;
		if (args.length >= 2) {
			sourceFileName = args[1];
			debug(objectFileName, sourceFileName);
		} else {
			interpret(objectFileName);
		}
	}

	public static void interpret(String objectFileName) {

		ObjectFile objectFile = new ObjectFile(objectFileName);
		if (objectFile.read()) {
			System.out.println("Unable to load object file " + objectFileName);
			return;
		}
		interpretProgram();
		showStatus();
		// mJAM exit code reflects normal termination or mJAM failure
		if (status == halted)
			System.exit(0);
		else
			System.exit(4);
	}

	public static void debug(String objectFileName, String sourceFileName) {
		ObjectFile objectFile = new ObjectFile(objectFileName);
		if (objectFile.read()) {
			System.out.println("Unable to load object file " + objectFileName);
			return;
		}

		sourceLines = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(
					sourceFileName)));
			String line = reader.readLine();
			while (line != null) {
				sourceLines.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable to load source file " + sourceFileName);
			return;
		} catch (IOException ie) {
			System.out.println("Unable to load source file " + sourceFileName);
			return;
		}

		debugProgram();
	}
}
