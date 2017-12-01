package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

public class Finish implements SortOp {

    @Override
    public Object process(List<VisiArray> arrays) {
        throw new UnsupportedOperationException("Finish shouldn't process anything!");
    }

    @Override
    public String toString() {
        return "return";
    }

}
