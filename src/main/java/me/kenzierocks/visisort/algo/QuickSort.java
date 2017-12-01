package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.VisiArray;
import me.kenzierocks.visisort.op.UncheckedFuture;

public class QuickSort implements SortAlgo {

    @Override
    public String getName() {
        return "Quick Sort";
    }

    @Override
    public void sort(VisiArray array) {
        quicksort(array, 0, array.getSize() - 1);
    }

    private void quicksort(VisiArray A, int lo, int hi) {
        if (lo < hi) {
            int p = partition(A, lo, hi);
            UncheckedFuture<Void> l = A.fork(a -> quicksort(a, lo, p - 1));
            UncheckedFuture<Void> r = A.fork(a -> quicksort(a, p + 1, hi));
            while (!(l.complete() && r.complete())) {
                A.idle();
            }
        }
    }

    private int partition(VisiArray a, int lo, int hi) {
        int pivot = a.get(hi);
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (a.get(j) < pivot) {
                i++;
                a.swap(i, j);
            }
        }
        if (a.compare(hi, a, i + 1) < 0) {
            a.swap(i + 1, hi);
        }
        return i + 1;
    }

}
