package me.kenzierocks.visisort.op;

import me.kenzierocks.visisort.Op;
import me.kenzierocks.visisort.VisiArray;

public class Slice implements Op {

    public final VisiArray array;
    public final int from;
    public final int to;

    public Slice(VisiArray array, int from, int to) {
        this.array = array;
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "arrays.add(arrays[" + array + "][" + from + ":" + to + "])";
    }

}
