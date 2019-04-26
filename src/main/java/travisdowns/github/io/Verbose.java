package travisdowns.github.io;

import java.io.PrintStream;

/**
 * Supports verbose output to stdout when -Dverbose=true is passed on the command line.
 * @author tdowns
 *
 */
public class Verbose {
    
    private static final boolean IS_VERBOSE = Boolean.getBoolean("verbose");
    private static PrintStream out = System.out;
    
    public static boolean isVerbose() {
        return IS_VERBOSE;
    }
    
    public static void verbose(String msg) {
        if (isVerbose()) {
            out.println(msg);
        }
    }
    
    public static void verbose(String fmt, Object... args) {
        if (isVerbose()) {
            String msg = String.format(fmt, args);
            verbose(msg);
        }
    }
}
