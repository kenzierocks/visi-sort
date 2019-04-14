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

public class ParallelMergeSort implements SortAlgo {

    @Override
    public String getName() {
        return "Parallel Merge Sort";
    }

    @Override
    public void sort(VisiArray array) {
        splitAndMerge(array);
    }

    private void splitAndMerge(VisiArray array) {
        if (array.getSize() > 1) {
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

                Data append;
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
