/*** line 8: cannot assign variable "x" of type "int" a value of type "boolean"
 *** line 9: cannot assign variable "b" of type "boolean" a value of type "int"
 * COMP 520
 * Type checking (2 errors)
 */
class fail315 {
    public void foo() {
	int x = 3  > 4;
	boolean b = 2 + 3;
    }
}
