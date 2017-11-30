package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortOp;

public class Get implements SortOp {

    public final int array;
    public final int index;

    public Get(int array, int index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public Result process(List<int[]> arrays) {
        int[] data = arrays.get(array);
        return Result.of(data[index]);
    }

    @Override
    public String toString() {
        return "int result = arrays[" + array + "][" + index + "]";
    }

}
