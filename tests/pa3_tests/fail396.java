/*** line 7: binary operator "+" expecting "int" type, received "boolean" type
 * COMP 520
 * Type checking (should return single error, not multiple errors)
 */
class Fail316 {
    public void foo() {
        int x = 2 + (3 + (4 + (5 + (1 != 0))));
    }
}


