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
package me.kenzierocks.visisort.algo

import kotlinx.coroutines.Job
import me.kenzierocks.visisort.OpChannel
import me.kenzierocks.visisort.SortAlgo
import me.kenzierocks.visisort.VisiArray

class QuickSortWithThresholdSwap(
        private val delegate: SortAlgo,
        private val threshold: Int,
        private val range: IntRange? = null
) : SortAlgo {

    override val name = when (range) {
        null -> "Quick Sort, switching to '${delegate.name}' @ $threshold element(s)"
        else -> "Quick Sort, switching to '${delegate.name}' @ $threshold element(s) ($range)"
    }

    override suspend fun OpChannel.sort(array: VisiArray) {
        val fullRange = range ?: 0 until array.size
        quickSort(array, fullRange)
    }

    private suspend fun OpChannel.quickSort(A: VisiArray, range: IntRange) {
        val lo = range.first
        val hi = range.last
        if (lo < hi) {
            if (hi - lo <= threshold) {
                val data = A.slice(lo..(hi + 1))
                with (delegate) {
                    sort(data)
                }
                for (i in 0 until data.size) {
                    A.set(i + lo, data.get(i))
                }
                return
            }
            val p = partition(A, lo, hi)
            val l = fork(A, QuickSortWithThresholdSwap(delegate, threshold, lo until p))[Job.Key]!!
            val r = fork(A, QuickSortWithThresholdSwap(delegate, threshold, (p + 1)..hi))[Job.Key]!!
            l.join()
            r.join()
        }
    }

    private suspend fun OpChannel.partition(a: VisiArray, lo: Int, hi: Int): Int {
        val pivot = a.get(hi)
        var i = lo - 1
        for (j in lo until hi) {
            if (a.get(j).value < pivot.value) {
                i++
                a.swap(i, j)
            }
        }
        if (compare(a.ref(hi), a.ref(i + 1)) < 0) {
            a.swap(i + 1, hi)
        }
        return i + 1
    }

}
