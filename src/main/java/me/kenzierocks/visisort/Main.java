package me.kenzierocks.visisort;

import me.kenzierocks.visisort.algo.BogoSort;

public class Main {

    public static void main(String[] args) {
        VisiSort visi = new VisiSort(new BogoSort());
        visi.run(getVisiData());
    }

    private static int[] getVisiData() {
        int len = 100;
        int[] array = new int[len];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Util.shuffle(array);
        return array;
    }

}
