package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.VisiArray;

public class ShellSort implements SortAlgo {

    private static final int[] GAPS = { 701, 301, 132, 57, 23, 10, 4, 1 };

    @Override
    public String getName() {
        return "Shell Sort";
    }

    @Override
    public void sort(VisiArray array) {
        for (int gap : GAPS) {
            for (int i = gap; i < array.getSize(); i++) {
                int tmp = array.get(i);
                int j;
                for (j = i; j >= gap && array.get(j - gap) > tmp; j -= gap) {
                    array.set(j, array.get(j - gap));
                }
                array.set(j, tmp);
            }
        }
    }

}
