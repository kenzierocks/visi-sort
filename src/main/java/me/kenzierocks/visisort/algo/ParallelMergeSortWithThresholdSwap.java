package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.VisiArray;
import me.kenzierocks.visisort.op.UncheckedFuture;

public class ParallelMergeSortWithThresholdSwap implements SortAlgo {

    private final SortAlgo delegate;
    private final int threshold;

    public ParallelMergeSortWithThresholdSwap(SortAlgo delegate, int threshold) {
        this.delegate = delegate;
        this.threshold = threshold;
    }

    @Override
    public String getName() {
        return "Parallel Merge Sort, switching to '" + delegate.getName() + "'@" + threshold + " elements";
    }

    @Override
    public void sort(VisiArray array) {
        splitAndMerge(array);
    }

    private void splitAndMerge(VisiArray array) {
        if (array.getSize() > 1) {
            if (array.getSize() <= threshold) {
                delegate.sort(array);
                return;
            }
            int leftAmt = array.getSize() / 2;
            VisiArray left = array.slice(0, leftAmt);
            VisiArray right = array.slice(leftAmt, array.getSize());
            UncheckedFuture<Void> leftComp = left.fork(this::splitAndMerge);
            UncheckedFuture<Void> rightComp = right.fork(this::splitAndMerge);
            while (!(leftComp.complete() && rightComp.complete())) {
                array.idle();
            }

            int index = 0;
            int indexLeft = 0;
            int indexRight = 0;
            while (indexLeft < left.getSize() && indexRight < right.getSize()) {

                int append;
                if (left.compare(indexLeft, right, indexRight) <= 0) {
                    append = left.get(indexLeft);
                    indexLeft++;
                } else {
                    append = right.get(indexRight);
                    indexRight++;
                }
                array.set(index, append);
                index++;
            }

            while (indexLeft < left.getSize()) {
                array.set(index, left.get(indexLeft));
                indexLeft++;
                index++;
            }

            while (indexRight < right.getSize()) {
                array.set(index, right.get(indexRight));
                indexRight++;
                index++;
            }
        }
    }

}
