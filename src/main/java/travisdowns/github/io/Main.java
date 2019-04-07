package travisdowns.github.io;

public class Main {
    public static void main(String[] args) {
//        System.err.println("" + (args.length - 1) + " strings passed");
        String pattern = args[0];
        Matcher matcher = new Matcher(pattern);
        for (int i = 1; i < args.length; i++) {
            if (matcher.matches(args[i])) {
                System.out.println(args[i]);
            }
        }
    }
}
