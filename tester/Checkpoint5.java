package tester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/* Automated regression tester for Checkpoint 4 tests
 * Created by Max Beckman-Harned
 * modified by jfp to improve robustness and accommodate different project organizations
 * Put your tests in "tests/pa4_tests" folder in your Eclipse workspace directory
 * If you preface your compiler error messages with *** then they will be displayed 
 */

public class Checkpoint5 {

    private static String projDir;
    private static File classPath;
    private static File testDir;

    public static void main(String[] args) throws IOException, InterruptedException {
        // project directory for miniJava and tester
        projDir = System.getProperty("user.dir");
        System.out.println("Run pa5_tests on miniJava compiler in " + projDir);

        // compensate for project organization
        classPath = new File(projDir + "/bin");
        if (!classPath.isDirectory()) {
            // no bin directory in project, assume projDir is root for class files
            classPath = new File(projDir);
        }

        // miniJava compiler mainclass present ?
        if (!new File(classPath + "/miniJava/Compiler.class").exists()) {
            System.out.println("No miniJava Compiler.class found (has it been compiled?) - exiting!");
            return;
        }

        // test directory present ?
        testDir = (new File(projDir + "/../tests/pa5_tests").getCanonicalFile());
        if (!testDir.isDirectory()) {
            System.out.println("pa5_tests directory not found - exiting!");
            return;
        }

        System.out.println("Running tests from directory " + testDir);
        int total = 0;
        int failures = 0;
        for (File x : testDir.listFiles()) {
            if (x.getName().startsWith(".") || x.getName().endsWith("mJAM") || x.getName().endsWith("asm"))
                continue;

            int returnCode = runTest(x);
            total++;

            if (returnCode == 1) {
                System.err.println("### miniJava Compiler fails while processing test " + x.getName());
                failures++;
                continue;
            }
            if (returnCode == 130) {
                System.err.println("### miniJava Compiler hangs on test " + x.getName());
                failures++;
                continue;
            }
            if (x.getName().indexOf("pass") != -1) {
                int testnum = Integer.parseInt(x.getName().substring(4, 7));
//                System.out.println("Test pass" + testnum);
                if (returnCode == 0) {

                    // contextual analysis regression tests
                    if (300 <= testnum && testnum < 400) {
//                        System.out.println(x.getName() + " was correctly accepted (regression test)");
                        continue;
                    }

                    // code generator tests
                    try {
                        int val = executeTest(x);
                        if (val == testnum % 100) {
//                            System.out.println(x.getName() + " ran successfully!");
                        } else {
                            failures++;
                            System.err
                                    .println(x.getName() + " compiled but did not run successfully--got output " + val);
                        }
                    } catch (Exception ex) {
                        failures++;
                        System.err.println(x.getName() + " did not output correctly.");
                    }
                } else {
                    failures++;
                    System.err.println(x.getName() + " failed to be processed!");
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

    private static int runTest(File x) throws IOException, InterruptedException {
        String testPath = x.getPath();
        ProcessBuilder pb = new ProcessBuilder("java", "miniJava.Compiler", testPath, "CODE GENERATION");
        pb.directory(classPath);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        processStream(p.getInputStream());
        if (!p.waitFor(5, TimeUnit.SECONDS)) {
            // hung test
            p.destroy();
            return 130; // interrupted
        }
        return p.exitValue();
    }

    private static int executeTest(File x) throws IOException, InterruptedException {
        String testPath = x.getPath().replace(".java", ".mJAM");
        ProcessBuilder pb = new ProcessBuilder("java", "mJAM.Interpreter", testPath);
        pb.directory(classPath);
        Process process = pb.start();

        Scanner scan = new Scanner(process.getInputStream());
        int num = -1;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.startsWith(">>> ")) {
                num = Integer.parseInt(line.substring(4));
//                System.out.println("Result = " + num);
            }
            if (line.startsWith("*** ")) {
//                System.out.println(line);
            }
        }
        scan.close();

        return num;
    }

    public static void processStream(InputStream stream) {
        Scanner scan = new Scanner(stream);
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.startsWith("*** ")) {
//                System.out.println(line);
            }
        }
        scan.close();
    }
}
