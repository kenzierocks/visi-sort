package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.Util;
import me.kenzierocks.visisort.VisiArray;

public class BogoSort implements SortAlgo {

    @Override
    public String getName() {
        return "Bogo Sort";
    }

    @Override
    public void sort(VisiArray array) {
        int[] data = gatherData(array);
        main: while (true) {
            Util.shuffle(data);
            for (int i = 0; i < data.length; i++) {
                array.set(i, data[i]);
            }
            for (int i = 0; i < data.length - 1; i++) {
                if (array.compare(i, array, i + 1) > 0) {
                    continue main;
                }
            }
            break;
        }
    }

    private int[] gatherData(VisiArray array) {
        int[] data = new int[array.getSize()];
        for (int i = 0; i < data.length; i++) {
            data[i] = array.get(i);
        }
        return data;
    }

}
