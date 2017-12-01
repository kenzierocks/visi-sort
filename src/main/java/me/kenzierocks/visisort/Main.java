package me.kenzierocks.visisort;

import me.kenzierocks.visisort.algo.QuickSort;

public class Main {

    public static void main(String[] args) {
        VisiSort visi = new VisiSort(new QuickSort());
        visi.run(getVisiData());
    }

    private static int[] getVisiData() {
        int len = 8192;
        int[] array = new int[len];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Util.shuffle(array);
        return array;
    }

}
