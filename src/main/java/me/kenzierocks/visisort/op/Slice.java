package me.kenzierocks.visisort.op;

import java.util.Arrays;
import java.util.List;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

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
    public VisiArray process(List<VisiArray> arrays) {
        VisiArray source = arrays.get(array);
        int[] data = Arrays.copyOfRange(source.getData(), from, to);
        int id = arrays.size();
        int parent = source.getId();
        VisiArray ret = new VisiArray(id, parent, source.getLevel() + 1, data, from, source.getCoRo());
        arrays.add(ret);
        return ret;
    }

    @Override
    public String toString() {
        return "arrays.add(arrays[" + array + "][" + from + ":" + to + "])";
    }

}
