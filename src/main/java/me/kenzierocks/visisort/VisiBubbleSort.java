package me.kenzierocks.visisort;

import java.util.Random;

import me.kenzierocks.visisort.algo.BubbleSort;

public class VisiBubbleSort {

    public static void main(String[] args) {
        int[] data = new Random().ints(1024).toArray();
        new VisiSort(new BubbleSort()).run(data);
    }
}
