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

public class ShellSort implements SortAlgo {

    private static final int[] GAPS = { 701, 301, 132, 57, 23, 10, 4, 1 };

    @Override
    public String getName() {
        return "Shell Sort";
    }

    @Override
    public void sort(VisiArray array) {
        for (int gap : GAPS) {
            for (int i = gap; i < array.getSize(); i++) {
                Data tmp = array.get(i);
                int j;
                for (j = i; j >= gap; j -= gap) {
                    Data value = array.get(j - gap);
                    if (value.value() <= tmp.value()) {
                        break;
                    }
                    array.set(j, value);
                }
                array.set(j, tmp);
            }
        }
    }

}
