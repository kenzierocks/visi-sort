package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

public class Swap implements SortOp {

    public final int array;
    public final int a;
    public final int b;

    public Swap(int array, int a, int b) {
        this.array = array;
        this.a = a;
        this.b = b;
    }

    @Override
    public Void process(List<VisiArray> arrays) {
        int[] data = arrays.get(array).getData();
        int swp = data[a];
        data[a] = data[b];
        data[b] = swp;
        return null;
    }

    @Override
    public String toString() {
        return "swap(arrays[" + array + "][" + a + "], arrays[" + array + "][" + b + "])";
    }

}
