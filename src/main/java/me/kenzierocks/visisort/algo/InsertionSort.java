package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.VisiArray;

public class InsertionSort implements SortAlgo {

    @Override
    public String getName() {
        return "Insertion Sort";
    }

    @Override
    public void sort(VisiArray array) {
        for (int i = 1; i < array.getSize(); i++) {
            for (int j = i; j > 0 && array.compare(j - 1, array, j) > 0; j--) {
                array.swap(j - 1, j);
            }
        }
    }

}
