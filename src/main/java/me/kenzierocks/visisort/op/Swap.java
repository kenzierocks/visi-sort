package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortOp;

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
    public Result process(List<int[]> arrays) {
        int[] data = arrays.get(array);
        int swp = data[a];
        data[a] = data[b];
        data[b] = swp;
        return Result.empty();
    }

    @Override
    public String toString() {
        return "swap(arrays[" + array + "][" + a + "], arrays[" + array + "][" + b + "])";
    }

}
