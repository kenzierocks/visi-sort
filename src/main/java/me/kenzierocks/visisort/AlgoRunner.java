/*
 * This file is part of visi-sort, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshroom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.visisort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import me.kenzierocks.visisort.coroutine.CoRoIterator;
import me.kenzierocks.visisort.coroutine.CoRoutines;
import me.kenzierocks.visisort.op.Fork;
import me.kenzierocks.visisort.op.Idle;
import me.kenzierocks.visisort.op.Slice;
import me.kenzierocks.visisort.op.UFJF;

public class AlgoRunner {

    private static final int OPS_PER_TICK = 1;

    private final Data[] initialData;
    private final List<VisiArray> arrays = new CopyOnWriteArrayList<>();
    private final SortAlgo algo;
    private final Consumer<SortOp> hook;
    private final List<CoRoIterator<Op, Object>> iterators = new ArrayList<>();

    public AlgoRunner(Data[] data, SortAlgo algo, Consumer<SortOp> hook) {
        this.initialData = data;
        this.algo = algo;
        this.hook = hook;
    }

    public List<VisiArray> getArrays() {
        return arrays;
    }

    public void start() {
        iterators.add(CoRoutines.startCoRoutine((prod) -> {
            arrays.add(new VisiArray(0, 0, 0, initialData, 0, prod));
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
                        Data[] data = Arrays.copyOfRange(source.getData(), sliceOp.from, sliceOp.to);
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
