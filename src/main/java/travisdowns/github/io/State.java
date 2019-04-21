package travisdowns.github.io;

public class State {
    /* indirection to allow state pointers to be recorded and updated in fragments */
    public static class StateRef {
        State s;

        public StateRef(State s) {
            this.s = s;
        }
    }

    public static State MATCHSTATE = new State(Type.MATCH, 0);

    enum Type {
        CHAR, ANY, SPLIT, MATCH, LPAREN, RPAREN;
    }

    int c;
    StateRef out;
    StateRef out1;
    final int id;
    final Type type;

    private State(State.Type type, int id) {
        this.type = type;
        this.id = id;
    }

    public static State makeChar(int id, char c) {
        State s = new State(Type.CHAR, id);
        s.c = c;
        s.out = new StateRef(null);
        return s;
    }

    public static State makeDot(int id) {
        State s = new State(Type.ANY, id);
        s.out = new StateRef(null);
        return s;
    }

    public static State makeSplit(int id, State out, State out1) {
        State s = new State(Type.SPLIT, id);
        s.out = new StateRef(out);
        s.out1 = new StateRef(out1);
        return s;
    }
    
    public static State makeLParen(int id, int parenIdx, State out) {
        State s = new State(Type.LPAREN, id);
        s.c = parenIdx;
        s.out = new StateRef(out);
        return s;
    }
    
    public static State makeRParen(int id, int parenIdx) {
        State s = new State(Type.RPAREN, id);
        s.c = parenIdx;
        s.out = new StateRef(null);
        return s;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    private String toString(boolean recurse) {
        switch (type) {
        case CHAR:
            return "CHAR[" + (char)c + "]";
        case SPLIT:
            if (recurse) {
                return "SPLIT[out=" + out.s.toString(false) + ",out1=" + out1.s.toString(false) + "]";
            } else {
                return "SPLIT[...]";
            }
        case LPAREN:
            return "(";
        case RPAREN:
            return ")";
        default:
            return "Unhandled State, type = " + type;
        }
    }

    public boolean isParen() {
        return type == Type.LPAREN || type == Type.RPAREN;
    }
}