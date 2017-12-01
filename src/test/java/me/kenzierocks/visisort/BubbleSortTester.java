package me.kenzierocks.visisort;

import org.junit.Test;

import me.kenzierocks.visisort.algo.BubbleSort;

public class BubbleSortTester extends AbstractSortTester {

    @Test
    public void testSorts() throws Exception {
        assertSorts(BubbleSort::new);
    }

}
