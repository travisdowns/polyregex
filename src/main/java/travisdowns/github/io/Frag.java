package travisdowns.github.io;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;

import travisdowns.github.io.State.StateRef;

public class Frag {
    State start;
    List<StateRef> out;

    public Frag(State start, List<StateRef> out) {
        this.start = start;
        this.out = out;
    }

    public Frag(State start, StateRef out) {
        this(start, singleton(out));
    }

    public static List<StateRef> singleton(StateRef out) {
        checkNotNull(out);
        List<StateRef> ret = new ArrayList<>();
        ret.add(out);
        return ret;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("s", start)
                .add("out", out)
                .toString();
    }
}