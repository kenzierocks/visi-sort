package me.kenzierocks.visisort.op;

import java.util.function.Consumer;

import me.kenzierocks.visisort.Op;
import me.kenzierocks.visisort.VisiArray;

public class Fork implements Op {

    public final VisiArray array;
    public final Consumer<VisiArray> code;

    public Fork(VisiArray array, Consumer<VisiArray> code) {
        this.array = array;
        this.code = code;
    }

    @Override
    public String toString() {
        return "fork(arrays[" + array + "])";
    }

}
