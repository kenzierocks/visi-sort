package me.kenzierocks.visisort.op;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

public class Set implements SortOp {

    public final VisiArray array;
    public final int index;
    public final int value;

    public Set(VisiArray array, int index, int value) {
        this.array = array;
        this.index = index;
        this.value = value;
    }

    @Override
    public Void process() {
        int[] data = array.getData();
        data[index] = value;
        return null;
    }

    @Override
    public String toString() {
        return "arrays[" + array + "][" + index + "] = " + value;
    }

}
