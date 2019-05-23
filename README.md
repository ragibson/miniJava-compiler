# miniJava-compiler

This repository contains a compiler for a subset of Java known as "miniJava".

In particular, this language is based off of Appel and Palsberg's MiniJava
(see Appendix A of ISBN-13 9780521820608):

> MiniJava is a subset of Java. The meaning of a MiniJava program is given by
its meaning as a Java program. Overloading is not allowed in MiniJava. The
MiniJava statement `System.out.println(...);` can only print integers. The
MiniJava expression `e.length` only applies to expressions of type `int[]`.

# Table of Contents
  * [Compilation Steps](#CompilationSteps)
  * [Example Compilations](#ExampleCompilations)
    * [Factorial](#Example1)
    * [Objects and Arrays](#Example2)
    * [Invalid Program](#Example3)
  * [Tests](#Tests)
  * [Installation and Usage](#InstallationAndUsage)

<a name = "CompilationSteps"></a>
## Compilation Steps

This compiler's operation is broken into four primary steps: syntactic
analysis, abstract syntax tree construction, type checking, and code
generation.

1. __Syntactic Analysis__: Recognize syntactically correct miniJava programs
and reject syntactically incorrect inputs.

2. __AST Construction__: Create a syntax tree that represents the abstract
structure of source code (including operator precedence).

3. __Type Checking__: Identify the variable/parameter/member/class
declarations associated with each expression. Then, ensure that all
expressions, function calls, variable assignments, etc. obey Java's type
rules.

4. __Code Generation__: Generate machine instructions to execute the program
of interest (targeting the "mJAM" abstract machine).

<a name = "ExampleCompilations"></a>
## Example Compilations

<a name = "Example1"></a>
### Factorial

The program

    class Factorial {
        public static void main(String[] args) {
            // 7! = 5040
            System.out.println(factorial(7));
        }

        static int factorial(int n) {
            if (n <= 1)
                return 1;
            return n * factorial(n - 1);
        }
    }

yields the following assembly

      0         PUSH         0
      1         LOADL        0
      2         CALL         newarr
      3         CALL         L10
      4         HALT   (0)
      5  L10:   LOADL        7
      6         CALL         L11
      7         CALL         putintnl
      8         RETURN (0)   1
      9  L11:   LOAD         -1[LB]
     10         LOADL        1
     11         CALL         le
     12         JUMPIF (0)   L12
     13         LOADL        1
     14         RETURN (1)   1
     15         JUMP         L12
     16  L12:   LOAD         -1[LB]
     17         LOAD         -1[LB]
     18         LOADL        1
     19         CALL         sub
     20         CALL         L11
     21         CALL         mult
     22         RETURN (1)   1

which outputs

    >>> 5040

when run.

<a name = "Example2"></a>
### Objects and Arrays

The program

    class MainClass {
        public static void main(String[] args) {
            A[] aa = new A[2];
            A b = new A();
            b.x = 3;
            aa[0] = b;
            A c = new A();
            c.x = 5;
            aa[1] = c;

            A t = aa[0];
            A s = aa[1];

            int result = t.x + s.x + 13; // 21
            System.out.println(result);
        }
    }

    class A {
        int x;
    }

yields the following assembly

      0         PUSH         0
      1         LOADL        0
      2         CALL         newarr
      3         CALL         L10
      4         HALT   (0)
      5  L10:   LOADL        2
      6         CALL         newarr
      7         LOADL        -1
      8         LOADL        1
      9         CALL         newobj
     10         LOAD         4[LB]
     11         LOADL        0
     12         LOADL        3
     13         CALL         fieldupd
     14         LOAD         3[LB]
     15         LOADL        0
     16         LOAD         4[LB]
     17         CALL         arrayupd
     18         LOADL        -1
     19         LOADL        1
     20         CALL         newobj
     21         LOAD         5[LB]
     22         LOADL        0
     23         LOADL        5
     24         CALL         fieldupd
     25         LOAD         3[LB]
     26         LOADL        1
     27         LOAD         5[LB]
     28         CALL         arrayupd
     29         LOAD         3[LB]
     30         LOADL        0
     31         CALL         arrayref
     32         LOAD         3[LB]
     33         LOADL        1
     34         CALL         arrayref
     35         LOAD         6[LB]
     36         LOADL        0
     37         CALL         fieldref
     38         LOAD         7[LB]
     39         LOADL        0
     40         CALL         fieldref
     41         CALL         add
     42         LOADL        13
     43         CALL         add
     44         LOAD         8[LB]
     45         CALL         putintnl
     46         RETURN (0)   1

which outputs

    >>> 21

when run.

<a name = "Example3"></a>
### Invalid Program

The program

    class Fail328 {
        public static void main(String[] args) {
            F05 c = new F05();
            c = c.foo.next;
        }
    }

    class F05 {
        public F05 next;
        public F05 foo() {return this;}
    }

yields the compiler output

    *** line 4 function reference cannot appear in the middle of a qualified reference.
    INVALID program after identification

<a name = "Tests"></a>
## Tests

The tests directory contains the test suites (provided by Jan Prins of
UNC-Chapel Hill)

  |Directory|Compilation Step  |Success Count|Success Rate|
  |---------|------------------|-------------|------------|
  |pa1_tests|Syntactic Analysis|106/106      |100%        |
  |pa2_tests|AST Construction  |80/80        |100%        |
  |pa3_tests|Type Checking     |94/94        |100%        |
  |pa4_tests|Code Generation   |35/35        |100%        |
  |pa5_tests|All               |53/60        |88%         |

These can be automatically run by the testers in miniJava.tester package.

<a name = "InstallationAndUsage"></a>
## Installation and Usage

In Eclipse, this compiler can be installed by simply importing the mJAM,
miniJava, and tester packages into a new Java project.

The tester subpackage requires the tests directory to be imported into a
separate project called "tests".

Compiler.java is the main entry point of the compiler. It takes the path to a
source code file as the first argument and a final compiler stage as an
optional second argument for testing purposes (one of "PARSER", "TYPE
CHECKING", and "CODE GENERATION").

The compiler generates an object code .mJAM file and an assembly .asm file,
which can be run with the mJAM abstract machine.
