package io.github.travisdowns.polyregex;

import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import io.github.travisdowns.polyregex.obsolete.OriginalMatcher;

public class Main {

    /**
     * Do the thing that xnfa-java script wants for use with Russ Cox's timing/xtime timing test.
     */
    private static void doXtime(List<String> args) {
        //      System.err.println("" + (args.length - 1) + " strings passed");
        String pattern = args.get(0);
        OriginalMatcher matcher = new OriginalMatcher(pattern);
        for (String text : args.subList(1, args.size())) {
            if (matcher.matches(text)) {
                System.out.println(text);
            }
        }
    }
    
    @SuppressWarnings("serial")
    private static class UsageException extends Exception {};
    
    /**
     * Do a shallow imitation of grep.
     * <p>
     * We support only the
     * <pre>grep [OPTIONS] PATTERN [FILE...]</pre>
     * variant of grep, which takes a pattern and 0 or more files to grep. If zero files
     * are provided, use stdin as the only file to grep.
     */
    private static void doGrep(List<String> args) throws IOException {
        try {
            if (args.isEmpty()) throw new UsageException();
            
            // add the wildcards at each end to emulate grep's "anywhere in line" match behavior
            String pattern = ".*" + args.get(0) + ".*";
            args = args.subList(1, args.size());
            
            List<Supplier<? extends Readable>> inputs;
            if (args.isEmpty()) {
                inputs = Collections.singletonList(Suppliers.ofInstance(new InputStreamReader(System.in)));
            } else {
                inputs = new ArrayList<>();
                for (String filename : args) {
                    inputs.add(Suppliers.ofInstance(new FileReader(filename)));
                }
            }
            
            Matcher matcher = new BackrefMatcher(pattern);
            
            for (Supplier<? extends Readable> rsupplier : inputs) {
                Readable r = rsupplier.get();
                CharStreams.readLines(r, new LineProcessor<Object>() {
                    @Override
                    public boolean processLine(String line) throws IOException {
                        if (matcher.matches(line)) {
                            System.out.println(line);
                        }
                        return true;
                    };
                    @Override
                    public Object getResult() { return null; }
                });
                
                if (r instanceof Closeable) {
                    ((Closeable)r).close();
                }
            }
            
        } catch (UsageException e) {
            System.err.println("Usage: grep PATTERN [FILE]...");
        }
    }

    public static void main(String[] args) throws IOException {
        Closeable c = (Closeable)System.in;
        List<String> arglist = ImmutableList.copyOf(args);
        if  (!arglist.isEmpty() && arglist.get(0).equals("--xtime")) {
            doXtime(arglist.subList(1, arglist.size()));
        } else {
            doGrep(arglist);
        }
    }
}
