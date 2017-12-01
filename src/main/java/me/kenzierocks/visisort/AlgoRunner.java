package me.kenzierocks.visisort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import me.kenzierocks.visisort.coroutine.CoRoIterator;
import me.kenzierocks.visisort.coroutine.CoRoutines;
import me.kenzierocks.visisort.op.Fork;
import me.kenzierocks.visisort.op.Idle;
import me.kenzierocks.visisort.op.Slice;
import me.kenzierocks.visisort.op.UFJF;

public class AlgoRunner {

    private static final int OPS_PER_TICK = 50;

    private final int[] initalData;
    private final List<VisiArray> arrays = new ArrayList<>();
    private final SortAlgo algo;
    private final Consumer<SortOp> hook;
    private final List<CoRoIterator<Op, Object>> iterators = new ArrayList<>();

    public AlgoRunner(int[] data, SortAlgo algo, Consumer<SortOp> hook) {
        this.initalData = data;
        this.algo = algo;
        this.hook = hook;
    }

    public List<VisiArray> getArrays() {
        return arrays;
    }

    public void start() {
        iterators.add(CoRoutines.startCoRoutine((prod) -> {
            arrays.add(new VisiArray(0, 0, 0, initalData, 0, prod));
            algo.sort(arrays.get(0));
        }));
    }

    public boolean pulse() {
        for (int i = 0; i < OPS_PER_TICK; i++) {
            if (iterators.isEmpty()) {
                return false;
            }
            List<CoRoIterator<Op, Object>> itersToAdd = new ArrayList<>();
            for (int j = iterators.size() - 1; j >= 0; j--) {
                CoRoIterator<Op, Object> iterator = iterators.get(j);
                if (!iterator.hasNext()) {
                    iterators.remove(j);
                    continue;
                }
                iterator.processNext(nextOp -> {
                    if (nextOp instanceof SortOp) {
                        SortOp sortOp = (SortOp) nextOp;
                        hook.accept(sortOp);
                        return sortOp.process();
                    } else if (nextOp instanceof Slice) {
                        Slice sliceOp = (Slice) nextOp;
                        VisiArray source = sliceOp.array;
                        int[] data = Arrays.copyOfRange(source.getData(), sliceOp.from, sliceOp.to);
                        int id = arrays.size();
                        int parent = source.getId();
                        VisiArray ret = new VisiArray(id, parent, source.getLevel() + 1, data, source.getOffset() + sliceOp.from, source.getCoRo());
                        arrays.add(ret);
                        return ret;
                    } else if (nextOp instanceof Fork) {
                        Fork forkOp = (Fork) nextOp;
                        VisiArray base = forkOp.array;
                        CompletableFuture<Void> completion = new CompletableFuture<>();
                        itersToAdd.add(CoRoutines.startCoRoutine((prod) -> {
                            VisiArray clone = new VisiArray(base.getId(), base.getParent(), base.getLevel(), base.getData(), base.getOffset(), prod);
                            forkOp.code.accept(clone);
                            completion.complete(null);
                        }));
                        return new UFJF<>(completion);
                    } else if (nextOp instanceof Idle) {
                        return null;
                    }
                    throw new IllegalStateException("Unhandled op " + nextOp.getClass().getName());
                });
            }
            iterators.addAll(itersToAdd);
        }
        return true;
    }

}
