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
package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.Data;
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
        Data pivot = a.get(hi);
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (a.get(j).value() < pivot.value()) {
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
