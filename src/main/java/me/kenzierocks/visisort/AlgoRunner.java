package me.kenzierocks.visisort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import me.kenzierocks.visisort.op.Finish;

public class AlgoRunner {

    private final List<int[]> arrays = new ArrayList<>();
    private final SortAlgo algo;
    private final Consumer<SortOp> hook;
    private Result result = Result.of(0);

    public AlgoRunner(int[] data, SortAlgo algo, Consumer<SortOp> hook) {
        this.arrays.add(data);
        this.algo = algo;
        this.hook = hook;
    }

    public List<int[]> getArrays() {
        return arrays;
    }

    public boolean pulse() {
        SortOp nextOp = null;
        while ((nextOp = algo.pulse(arrays.get(0).length, result)) == null) {
        }
        hook.accept(nextOp);
        if (nextOp instanceof Finish) {
            return false;
        }
        result = nextOp.process(arrays);
        return true;
    }

}
