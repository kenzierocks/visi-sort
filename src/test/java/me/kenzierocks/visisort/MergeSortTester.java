package me.kenzierocks.visisort;

import org.junit.Test;

import me.kenzierocks.visisort.algo.MergeSort;

public class MergeSortTester extends AbstractSortTester {

    @Test
    public void testSorts() throws Exception {
        assertSorts(MergeSort::new);
    }

}
