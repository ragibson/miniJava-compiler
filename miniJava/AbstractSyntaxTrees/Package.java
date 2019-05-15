/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class Package extends AST {

  public Package(ClassDeclList cdl, SourcePosition posn) {
    super(posn);
    classDeclList = cdl;
  }
    
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitPackage(this, o);
    }

    public ClassDeclList classDeclList;
}
