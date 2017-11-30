package me.kenzierocks.visisort;

import java.util.concurrent.ThreadLocalRandom;

public class Util {

    public static void shuffle(int[] array) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < array.length - 1; i++) {
            int j = rnd.nextInt(i, array.length);
            int swp = array[i];
            array[i] = array[j];
            array[j] = swp;
        }
    }

}
