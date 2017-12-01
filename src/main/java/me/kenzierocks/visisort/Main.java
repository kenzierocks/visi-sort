package me.kenzierocks.visisort;

import me.kenzierocks.visisort.algo.InsertionSort;
import me.kenzierocks.visisort.algo.ParallelMergeSortWithThresholdSwap;
import me.kenzierocks.visisort.algo.QuickSortWithThresholdSwap;

public class Main {

    public static void main(String[] args) {
        SortAlgo sort = new ParallelMergeSortWithThresholdSwap(
                new QuickSortWithThresholdSwap(
                        new InsertionSort(), 32),
                256);
        VisiSort visi = new VisiSort(sort);
        System.err.println(sort.getName());
        visi.run(getVisiData());
    }

    private static int[] getVisiData() {
        int len = 4096;
        int[] array = new int[len];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Util.shuffle(array);
        return array;
    }

}
