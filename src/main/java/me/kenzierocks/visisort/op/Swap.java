package me.kenzierocks.visisort.op;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

public class Swap implements SortOp {

    public final VisiArray array;
    public final int a;
    public final int b;

    public Swap(VisiArray array, int a, int b) {
        this.array = array;
        this.a = a;
        this.b = b;
    }

    @Override
    public Void process() {
        int[] data = array.getData();
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
