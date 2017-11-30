package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortOp;

public class Finish implements SortOp {

    @Override
    public Result process(List<int[]> arrays) {
        throw new UnsupportedOperationException("Finish shouldn't process anything!");
    }

    @Override
    public String toString() {
        return "return";
    }

}
