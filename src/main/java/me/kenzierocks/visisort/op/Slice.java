package me.kenzierocks.visisort.op;

import java.util.Arrays;
import java.util.List;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortOp;

public class Slice implements SortOp {

    public final int array;
    public final int from;
    public final int to;

    public Slice(int array, int from, int to) {
        this.array = array;
        this.from = from;
        this.to = to;
    }

    @Override
    public Result process(List<int[]> arrays) {
        int[] data = arrays.get(array);
        data = Arrays.copyOfRange(data, from, to);
        arrays.add(data);
        return Result.of(arrays.size() - 1);
    }

    @Override
    public String toString() {
        return "arrays.add(arrays[" + array + "][" + from + ":" + to + "])";
    }

}
