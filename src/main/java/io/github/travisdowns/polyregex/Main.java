package io.github.travisdowns.polyregex;

import io.github.travisdowns.polyregex.obsolete.OriginalMatcher;

public class Main {
    public static void main(String[] args) {
//        System.err.println("" + (args.length - 1) + " strings passed");
        String pattern = args[0];
        OriginalMatcher matcher = new OriginalMatcher(pattern);
        for (int i = 1; i < args.length; i++) {
            if (matcher.matches(args[i])) {
                System.out.println(args[i]);
            }
        }
    }
}
