package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortOp;

public class Set implements SortOp {

    public final int array;
    public final int index;
    public final int value;

    public Set(int array, int index, int value) {
        this.array = array;
        this.index = index;
        this.value = value;
    }

    @Override
    public Result process(List<int[]> arrays) {
        int[] data = arrays.get(array);
        data[index] = value;
        return Result.empty();
    }

    @Override
    public String toString() {
        return "arrays[" + array + "][" + index + "] = " + value;
    }

}
