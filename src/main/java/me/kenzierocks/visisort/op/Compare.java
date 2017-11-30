package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortOp;

public class Compare implements SortOp {

    public final int array;
    public final int a;
    public final int b;

    public Compare(int array, int a, int b) {
        this.array = array;
        this.a = a;
        this.b = b;
    }

    @Override
    public Result process(List<int[]> arrays) {
        int[] data = arrays.get(array);
        return Result.of(Integer.compare(data[a], data[b]));
    }

    @Override
    public String toString() {
        return "int result = compare(arrays[" + array + "][" + a + "], arrays[" + array + "][" + b + "])";
    }

}
