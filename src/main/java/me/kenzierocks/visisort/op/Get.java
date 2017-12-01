package me.kenzierocks.visisort.op;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

public class Get implements SortOp {

    public final VisiArray array;
    public final int index;

    public Get(VisiArray array, int index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public Integer process() {
        int[] data = array.getData();
        return data[index];
    }

    @Override
    public String toString() {
        return "int result = arrays[" + array + "][" + index + "]";
    }

}
