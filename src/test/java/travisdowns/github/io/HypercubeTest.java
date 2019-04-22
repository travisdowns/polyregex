package travisdowns.github.io;

import static org.junit.Assert.*;

import org.junit.Test;

public class HypercubeTest {
    
    @Test
    public void testOneDim() {
        Hypercube<Integer> cube = new Hypercube<>(1, 2);
        
        assertEquals(null, cube.get(0));
        assertEquals(null, cube.get(1));
        
        cube.set( 0, 0);
        cube.set(10, 1);
        
        assertEquals(Integer.valueOf( 0), cube.get(0));
        assertEquals(Integer.valueOf(10), cube.get(1));
    }
    
    @Test
    public void testOneDimeOOB() {
        Hypercube<Integer> cube = new Hypercube<>(1, 2);
        
        try {
            assertEquals(null, cube.get(2));
            fail();
        } catch (ArrayIndexOutOfBoundsException expected) {}
        
        try {
            assertEquals(null, cube.get(2));
            fail();
        } catch (ArrayIndexOutOfBoundsException expected) {}
    }
    
    @Test
    public void testTwoDim() {
        Hypercube<Integer> cube = new Hypercube<>(2, 2);
        
        assertEquals(null, cube.get(0, 0));
        assertEquals(null, cube.get(0, 1));
        assertEquals(null, cube.get(1, 0));
        assertEquals(null, cube.get(1, 1));
        
        cube.set( 0, 0, 0);
        cube.set(10, 1, 0);
        cube.set(20, 0, 1);
        
        assertEquals(Integer.valueOf( 0), cube.get(0, 0));
        assertEquals(Integer.valueOf(20), cube.get(0, 1));
        assertEquals(Integer.valueOf(10), cube.get(1, 0));
        assertEquals(               null, cube.get(1, 1));
    }

}