/**
 * Reads and writes mJAM object files
 * @author prins
 * @version COMP 520 V2.3
 */
package mJAM;

import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;

public class ObjectFile {
	
	String objectFileName;

	public ObjectFile(String objectFileName) {
		super();
		this.objectFileName = objectFileName;
	}
	
	/**
	 * Write mJAM code in code store into a binary object file
	 * @param output  object file
	 * @return true if write fails
	 */
	public boolean write(){
		boolean failed = false;
		try {
			FileOutputStream objectFile = new FileOutputStream(objectFileName);
			DataOutputStream is = new DataOutputStream(objectFile);
			for (int i = Machine.CB; i < Machine.CT; i++ ){
				Instruction inst = Machine.code[i];
				is.writeInt(inst.op);
				is.writeInt(inst.n);
				is.writeInt(inst.r);
				is.writeInt(inst.d);
			}
			objectFile.close();
		}
		catch (Exception e) {failed = true;}
		return failed;
	}

	/**
	 * Read binary object file into code store, setting CT 
	 * @return true if object code read fails
	 */
	public boolean read() {
		boolean failed = false;
		try {
			FileInputStream objectFile = new FileInputStream(objectFileName);
			DataInputStream is = new DataInputStream(objectFile);
			
			Machine.CT = Machine.CB;
			while (is.available() > 0 && Machine.CT < Machine.PB){
				Instruction inst = new Instruction();
				inst.op = is.readInt();
				inst.n = is.readInt();
				inst.r = is.readInt();
				inst.d = is.readInt();
				Machine.code[Machine.CT++] = inst;
			}
			objectFile.close();
		} catch (Exception e) {
			failed = true;
		}	
		return failed;
	}
}
