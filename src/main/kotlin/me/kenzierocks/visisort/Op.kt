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
package me.kenzierocks.visisort

import kotlinx.coroutines.CoroutineName
import kotlin.coroutines.CoroutineContext

sealed class Op<R> {

    /**
     * Convert the operation to a string representing the actions that happened.
     */
    abstract fun toReadableAction(result: R): String

    object Idle : Op<Unit>() {
        override fun toReadableAction(result: Unit) = "// do nothing"
    }

    data class Compare(val a: VisiArray.Ref, val b: VisiArray.Ref) : Op<Int>() {
        override fun toReadableAction(result: Int) = "compare($a, $b) -> $result"
    }

    data class Fork(val array: VisiArray, val algo: SortAlgo) : Op<CoroutineContext>() {
        val oldArray = array.copy()
        override fun toReadableAction(result: CoroutineContext) = "fork($oldArray, ${algo.name}) -> ${result[CoroutineName.Key]?.name}"
    }

    data class Get(val array: VisiArray, val index: Int) : Op<Data>() {
        val oldArray = array.copy()
        override fun toReadableAction(result: Data) = "$oldArray[$index] -> ${result.value}"
    }

    data class Set(val array: VisiArray, val index: Int, val value: Data) : Op<Unit>() {
        val oldArray = array.copy()
        override fun toReadableAction(result: Unit) = "$oldArray[$index] = ${value.value}"
    }

    data class Slice(val array: VisiArray, val range: IntRange) : Op<VisiArray>() {
        val oldArray = array.copy()
        override fun toReadableAction(result: VisiArray) = "$oldArray[$range] -> $result"
    }

    data class Swap(val array: VisiArray, val a: Int, val b: Int) : Op<Unit>() {
        val oldArray = array.copy()
        override fun toReadableAction(result: Unit) = "$oldArray.swap($a (=${oldArray.data[a].value}), $b (=${oldArray.data[b].value}))"
    }
}

data class OpResult<R>(val op: Op<R>, val result: R) {
    override fun toString() = op.toReadableAction(result)
}
