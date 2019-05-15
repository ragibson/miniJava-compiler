package tester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/* Automated regression tester for Checkpoint 2 tests
 * Created by Max Beckman-Harned and jfp
 * Put your tests in "tests/pa2_tests" folder in your Eclipse workspace directory
 * If you preface your error messages / exceptions with ERROR or *** they will be 
 * displayed if they appear during processing
 */

public class Checkpoint2 {

    private static class ReturnInfo {
        int returnCode;
        String ast;

        public ReturnInfo(int _returnCode, String _ast) {
            returnCode = _returnCode;
            ast = _ast;
        }
    }

    private static String projDir;
    private static File classPath;
    private static File testDir;

    public static void main(String[] args) throws IOException, InterruptedException {
        // project directory for miniJava and tester
        projDir = System.getProperty("user.dir");
        System.out.println("Run pa2_tests on miniJava compiler in " + projDir);

        // compensate for project organization
        classPath = new File(projDir + "/bin");
        if (!classPath.isDirectory()) {
            // no bin directory in project, assume projDir is root for class files
            classPath = new File(projDir);
        }

        // miniJava compiler mainclass present ?
        if (!new File(classPath + "/miniJava/Compiler.class").exists()) {
            System.out.println("No miniJava Compiler.class found (has it been compiled?) - exiting");
            return;
        }

        // test directory present ?
        testDir = (new File(projDir + "/../tests/pa2_tests").getCanonicalFile());
        if (!testDir.isDirectory()) {
            System.out.println("pa2_tests directory not found - exiting!");
            return;
        }

        System.out.println("Running tests from directory " + testDir);

        int total = 0;
        int failures = 0;
        for (File x : testDir.listFiles()) {
            if (x.getName().endsWith("out") || x.getName().startsWith("."))
                continue;
            ReturnInfo info = runTest(x);
            total++;
            String ast = info.ast;
            int returnCode = info.returnCode;
            if (returnCode == 1) {
                System.err.println("### miniJava Compiler failed while processing test " + x.getName());
                failures++;
                continue;
            }
            if (returnCode == 130) {
                System.err.println("### miniJava Compiler hangs on test " + x.getName());
                failures++;
                continue;
            }
            if (x.getName().indexOf("pass") != -1) {
                if (returnCode == 0) {
                    String actualAST = getAST(new FileInputStream(x.getPath() + ".out"));
                    if (actualAST.equals(ast)) {
//                        System.out.println(x.getName() + " parsed successfully and has a correct AST!");
                    } else {
                        System.err.println(x.getName() + " parsed successfully but has an incorrect AST!");
                        failures++;
                    }
                } else {
                    failures++;
                    System.err.println(x.getName() + " failed to be parsed!");
                }
            } else {
                if (returnCode == 4) {
//                    System.out.println(x.getName() + " failed successfully!");
                } else {
                    System.err.println(x.getName() + " did not fail properly!");
                    failures++;
                }
            }
        }

        double percentage = 100 * (total - failures) / total;
        System.out.println(percentage + "% succeeded. " + failures + " failures in all out of " + total + ".");
    }

    private static ReturnInfo runTest(File x) throws IOException, InterruptedException {
        String testPath = x.getPath();
        ProcessBuilder pb = new ProcessBuilder("java", "miniJava.Compiler", testPath, "PARSER");
        pb.directory(classPath);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        String ast = getAST(p.getInputStream());
        int exitValue;
        if (!p.waitFor(4, TimeUnit.SECONDS)) {
            // hung test
            p.destroy();
            exitValue = 130; // interrupted
        } else {
            exitValue = p.exitValue();
        }
        return new ReturnInfo(exitValue, ast);
    }

    public static String getAST(InputStream stream) {
        Scanner scan = new Scanner(stream);
        String ast = null;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.equals("======= AST Display =========================")) {
                line = scan.nextLine();
                while (scan.hasNext() && !line.equals("=============================================")) {
                    ast += line + "\n";
                    line = scan.nextLine();
                }
            }
            if (line.startsWith("*** "))
                System.out.println(line);
            if (line.startsWith("ERROR")) {
                System.out.println(line);
                while (scan.hasNext())
                    System.out.println(scan.next());
            }
        }
        scan.close();
        return ast;
    }
}
