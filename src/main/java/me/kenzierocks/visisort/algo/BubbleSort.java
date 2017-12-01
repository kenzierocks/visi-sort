package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.VisiArray;

public class BubbleSort implements SortAlgo {

    @Override
    public String getName() {
        return "Bubble Sort";
    }

    @Override
    public void sort(VisiArray array) {
        int topSwap = array.getSize();
        boolean anySwaps = false;
        do {
            int searchSize = topSwap;
            anySwaps = false;
            for (int i = 0; i < searchSize - 1; i++) {
                if (array.compare(i, array, i + 1) > 0) {
                    array.swap(i, i + 1);
                    anySwaps = true;
                    topSwap = i + 1;
                }
            }
        } while (anySwaps);
    }

}
