package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortOp;

public class NewArray implements SortOp {

    public final int size;

    public NewArray(int size) {
        this.size = size;
    }

    @Override
    public Result process(List<int[]> arrays) {
        int[] data = new int[size];
        arrays.add(data);
        return Result.of(arrays.size() - 1);
    }

    @Override
    public String toString() {
        return "arrays.add(new int[" + size + "])";
    }

}
