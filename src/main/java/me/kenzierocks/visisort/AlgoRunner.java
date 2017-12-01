package me.kenzierocks.visisort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import me.kenzierocks.visisort.coroutine.CoRoIterator;
import me.kenzierocks.visisort.coroutine.CoRoutines;

public class AlgoRunner {

    private static final int OPS_PER_TICK = 20;

    private final int[] initalData;
    private final List<VisiArray> arrays = new ArrayList<>();
    private final SortAlgo algo;
    private final Consumer<SortOp> hook;
    private CoRoIterator<SortOp, Object> iterator;

    public AlgoRunner(int[] data, SortAlgo algo, Consumer<SortOp> hook) {
        this.initalData = data;
        this.algo = algo;
        this.hook = hook;
    }

    public List<VisiArray> getArrays() {
        return arrays;
    }

    public void start() {
        iterator = CoRoutines.startCoRoutine((prod) -> {
            arrays.add(new VisiArray(0, 0, 0, initalData, 0, prod));
            algo.sort(arrays.get(0));
        });
    }

    public boolean pulse() {
        for (int i = 0; i < OPS_PER_TICK; i++) {
            if (!iterator.hasNext()) {
                return false;
            }
            iterator.processNext(nextOp -> {
                hook.accept(nextOp);
                return nextOp.process(arrays);
            });
        }
        return true;
    }

}
