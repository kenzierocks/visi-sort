package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

public class Get implements SortOp {

    public final int array;
    public final int index;

    public Get(int array, int index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public Integer process(List<VisiArray> arrays) {
        int[] data = arrays.get(array).getData();
        return data[index];
    }

    @Override
    public String toString() {
        return "int result = arrays[" + array + "][" + index + "]";
    }

}
