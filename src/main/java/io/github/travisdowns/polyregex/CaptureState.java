package io.github.travisdowns.polyregex;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class CaptureState {
    
    private final int[] starts, ends;
    private final int textlen;
    
    public CaptureState(int[] starts, int[] ends, int textlen) {
        checkArgument(starts.length == ends.length);
        this.starts = starts;
        this.ends = ends;
        this.textlen = textlen;
    }
    
    /** get the start value for the given backref (/1 to /9) */
    public int start(int backref) {
        checkArgument(backref >= 1 && backref <= 9);
        return starts[backref - 1];
    }
    
    /** get the end value for the given backref (/1 to /9) */
    public int end(int backref) {
        checkArgument(backref >= 1 && backref <= 9);
        return ends[backref - 1];
    }
    
    /**
     * @return a new {@link CaptureState} with the given capture updated to start at startIdx
     */
    public CaptureState withStart(int capture, int startIdx) {
        checkState(startIdx <= textlen);
        int[] newstarts = this.starts.clone();
        int[] newends   = this.ends  .clone();
        newstarts[capture - 1] = startIdx;
        newends  [capture - 1] = -1; // when we restart a capture we set the end to -1 ("unset")
        return new CaptureState(newstarts, newends, textlen);
    }
    
    /**
     * @return a new {@link CaptureState} with the given capture updated to end at endIdx
     */
    public CaptureState withEnd(int capture, int endIdx) {
        checkState(endIdx <= textlen);
        checkState(starts[capture - 1] != -1, "end without start");
        int[] newends = this.ends.clone();
        newends[capture - 1] = endIdx;
        return new CaptureState(this.starts, newends, textlen);
    }
    
    /** the number of capture groups tracked in this state */
    public int size() {
        return starts.length;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{ starts, ends });
    }
    
    @Override
    public boolean equals(Object rhs_) {
        CaptureState rhs = (CaptureState)rhs_;
        return Objects.deepEquals(this.starts, rhs.starts) && Objects.deepEquals(this.ends, rhs.ends); 
    }
    
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).toString();
    }
    
    /**
     * @return terse representation than {@link #toString()}, with a (start, end) pair for each captured group, like (1,5)
     */
    public String str() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < starts.length; i++) {
            sb.append(String.format("(%d,%d)%s", starts[i], ends[i], i == 0 ? "" : ", "));
        }
        return sb.toString();
    }
}