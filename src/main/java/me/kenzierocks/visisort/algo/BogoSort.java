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

import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.Util;
import me.kenzierocks.visisort.VisiArray;

public class BogoSort implements SortAlgo {

    @Override
    public String getName() {
        return "Bogo Sort";
    }

    @Override
    public void sort(VisiArray array) {
        int[] data = gatherData(array);
        main: while (true) {
            Util.shuffle(data);
            for (int i = 0; i < data.length; i++) {
                array.set(i, data[i]);
            }
            for (int i = 0; i < data.length - 1; i++) {
                if (array.compare(i, array, i + 1) > 0) {
                    continue main;
                }
            }
            break;
        }
    }

    private int[] gatherData(VisiArray array) {
        int[] data = new int[array.getSize()];
        for (int i = 0; i < data.length; i++) {
            data[i] = array.get(i);
        }
        return data;
    }

}
