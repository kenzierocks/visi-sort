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

class ParallelMergeSortWithThresholdSwap(
        private val delegate: SortAlgo,
        private val threshold: Int
) : SortAlgo {

    override val name = "Parallel Merge Sort, switching to '${delegate.name}' @ $threshold element(s)"

    private class ParallelMergeSwapArray(
            data: VisiArray,
            private val parent: ParallelMergeSortWithThresholdSwap
    ) : MergeArray(data) {

        override fun newArray(data: VisiArray) = ParallelMergeSwapArray(data, parent)

        override suspend fun performSubMerge(opChannel: OpChannel, left: MergeArray, right: MergeArray) {
            val leftJob = opChannel.fork(left.data, when {
                left.data.size <= parent.threshold -> parent.delegate
                else -> parent
            })[Job.Key]!!
            val rightJob = opChannel.fork(right.data, when {
                left.data.size <= parent.threshold -> parent.delegate
                else -> parent
            })[Job.Key]!!
            leftJob.join()
            rightJob.join()
        }
    }

    override suspend fun OpChannel.sort(array: VisiArray) {
        ParallelMergeSwapArray(array, this@ParallelMergeSortWithThresholdSwap).mergeLR(this)
    }
}
