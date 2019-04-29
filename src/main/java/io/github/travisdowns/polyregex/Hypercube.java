/**
 * 
 */
package io.github.travisdowns.polyregex;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * A hypercube - i.e., a D-dimensional array which is "square" (all dimentions having the same
 * size).
 *   
 * @author tdowns
 *
 */
public class Hypercube<T> {

    /**
     * The hypercube is implemented recursively with slices. At the top level we have a 1D array of 
     * slices, where each slice is a sub-cube of dimension D-1, and so on until the last level which
     * is just an array of elements.  
     */
    private final Slice cube;
    private final int dimensions, n;
    
    private static class Slice {
        final Slice[] slices;
        final Object[] data;
        
        public Slice(Slice[] slices, Object[] data) {
            checkState(slices == null ^ data == null);
            this.slices = slices;
            this.data = data;
        }
    }
    
    /**
     * Create a new {@link Hypercube} with the given dimension and size.
     *
     * @param dimensions the number of dimensions this cube should have, i.e., 1 for a normal array, 2 for
     * a 2D array (matrix), etc.
     * @param n the size of each dimension
     */
    public Hypercube(int dimensions, int n) {
        checkArgument(dimensions >= 1, "dimension must be >= 1");
        this.dimensions = dimensions;
        this.n = n;
        this.cube = makeCube(dimensions);
        
    }
    

    @SuppressWarnings("unchecked")
    public T get(int... indices) {
        return (T)getDataArray(indices)[last(indices)];
    }
    
    /**
     * Set the given {@code value} at the location specified by indices.
     * 
     * @param value the value to set
     * @param indices the location to set it at
     */
    public void set(T value, int... indices) {
        getDataArray(indices)[last(indices)] = value;
    }
    
    private static int last(int... indices) {
        return indices[indices.length - 1];
    }
    
    private Slice makeCube(int dimsleft) {
        if (dimsleft > 1) {
            Slice[] temp = new Slice[n];
            for (int i = 0; i < n; i++) {
                temp[i] = makeCube(dimsleft - 1);
            }
            return new Slice(temp, null);
        } else {
            return new Slice(null, new Object[n]);
        }
    }
    
    /**
     * Return the data array associated with indices, i.e., all indices other than the last
     * one which (which is used to index the data array).
     */
    private Object[] getDataArray(int... indices) {
        checkArgument(indices.length == dimensions,
                "number of indices (%s) must be equal to number of dimensions (%s)",
                indices.length, dimensions);
        Slice s = cube;
        for (int i = 0; i < indices.length - 1; i++) {
            s = s.slices[indices[i]];
        }
        checkState(s.data != null);
        return s.data;
    }
}
