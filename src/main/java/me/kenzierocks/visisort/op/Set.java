package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

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
    public Void process(List<VisiArray> arrays) {
        int[] data = arrays.get(array).getData();
        data[index] = value;
        return null;
    }

    @Override
    public String toString() {
        return "arrays[" + array + "][" + index + "] = " + value;
    }

}
