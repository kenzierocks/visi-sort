package me.kenzierocks.visisort;

import org.junit.Test;

import me.kenzierocks.visisort.algo.BogoSort;

public class BogoSortTester extends AbstractSortTester {

    @Test
    public void testSorts() throws Exception {
        // dammit bogosort
        assertSorts(BogoSort::new, 3);
    }

}
