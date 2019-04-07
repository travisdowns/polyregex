package travisdowns.github.io;

public class Main {
    public static void main(String[] args) {
        String pattern = args[0];
        for (int i = 1; i < args.length; i++) {
            if (Matcher.matches(pattern, args[i])) {
                System.out.println(args[i]);
            }
        }
    }
}
