package me.kenzierocks.visisort.op;

import me.kenzierocks.visisort.SortOp;

public class Finish implements SortOp {

    @Override
    public Object process() {
        throw new UnsupportedOperationException("Finish shouldn't process anything!");
    }

    @Override
    public String toString() {
        return "return";
    }

}
