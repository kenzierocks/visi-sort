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

import com.google.common.collect.ImmutableSortedMap
import joptsimple.OptionException
import joptsimple.OptionParser
import joptsimple.OptionSet
import joptsimple.ValueConversionException
import joptsimple.ValueConverter
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.kenzierocks.visisort.algo.BogoSort
import me.kenzierocks.visisort.algo.BubbleSort
import me.kenzierocks.visisort.algo.InsertionSort
import me.kenzierocks.visisort.algo.MergeSort
import me.kenzierocks.visisort.algo.ParallelMergeSort
import me.kenzierocks.visisort.algo.ParallelMergeSortWithThresholdSwap
import me.kenzierocks.visisort.algo.QuickSort
import me.kenzierocks.visisort.algo.QuickSortWithThresholdSwap
import me.kenzierocks.visisort.algo.ShellSort
import me.kenzierocks.visisort.dispatcher.NormalMainDispatcher
import java.io.IOException
import java.util.Arrays.asList
import java.util.concurrent.ArrayBlockingQueue

object Main {

    private interface WrappingSortAlgo {

        fun wrap(delegate: SortAlgo, threshold: Int): SortAlgo

    }

    private fun WSA(lambda: (SortAlgo, Int) -> SortAlgo): WrappingSortAlgo {
        return object : WrappingSortAlgo {
            override fun wrap(delegate: SortAlgo, threshold: Int) = lambda(delegate, threshold)
        }
    }

    private val SORT_ALGO_CONVERTER = object : ValueConverter<SortAlgo> {

        private val algoMap = ImmutableSortedMap.naturalOrder<String, SortAlgo>()
                .put("merge", MergeSort())
                .put("parallel-merge", ParallelMergeSort())
                .put("quick-sort", QuickSort())
                .put("bogo", BogoSort())
                .put("bubble", BubbleSort())
                .put("insertion", InsertionSort())
                .put("shell", ShellSort())
                .build()

        override fun convert(value: String): SortAlgo {
            return algoMap[value] ?: throw ValueConversionException("Invalid value for SortAlgo: $value")
        }

        override fun valueType(): Class<out SortAlgo> {
            return SortAlgo::class.java
        }

        override fun valuePattern(): String {
            return algoMap.keys.joinToString("|")
        }
    }

    private val WRAPPING_SORT_ALGO_CONVERTER = object : ValueConverter<WrappingSortAlgo> {

        private val algoMap = ImmutableSortedMap.naturalOrder<String, WrappingSortAlgo>()
                .put("parallel-merge", WSA(::ParallelMergeSortWithThresholdSwap))
                .put("quick-sort", WSA { a, t -> QuickSortWithThresholdSwap(a, t) })
                .build()

        override fun convert(value: String): WrappingSortAlgo {
            return algoMap[value] ?: throw ValueConversionException("Invalid value for WrappingSortAlgo: $value")
        }

        override fun valueType(): Class<out WrappingSortAlgo> {
            return WrappingSortAlgo::class.java
        }

        override fun valuePattern(): String {
            return algoMap.keys.joinToString("|")
        }
    }

    private val PARSER = OptionParser()

    private val HELP = PARSER.acceptsAll(asList("h", "help"), "Show this help")
            .forHelp()

    private val SORT_ALGO = PARSER.acceptsAll(asList("a", "algorithm"), "Sort algorithm")
            .withRequiredArg()
            .withValuesConvertedBy(SORT_ALGO_CONVERTER)
            .required()

    private val WRAPPING_SORT_ALGO = PARSER.acceptsAll(asList("w", "wrapping-algorithm"), "Sort algorithm to start with, swapping to --algorithm at --threshold")
            .withRequiredArg()
            .withValuesConvertedBy(WRAPPING_SORT_ALGO_CONVERTER)

    private val THRESHOLD = PARSER.acceptsAll(asList("t", "threshold"), "Threshold to swap from --wrapping-algorithm to --algorithm")
            .availableIf(WRAPPING_SORT_ALGO)
            .requiredIf(WRAPPING_SORT_ALGO)
            .withRequiredArg()
            .ofType<Int>(Int::class.javaPrimitiveType!!)

    private val DATA_LENGTH = PARSER.acceptsAll(asList("l", "length"), "Data length")
            .withRequiredArg()
            .ofType<Int>(Int::class.javaPrimitiveType!!)
            .defaultsTo(128)

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val opts: OptionSet
        try {
            opts = PARSER.parse(*args)
        } catch (e: OptionException) {
            System.err.println(e.message)
            PARSER.printHelpOn(System.err)
            System.exit(1)
            return
        }

        if (opts.has(HELP)) {
            PARSER.printHelpOn(System.err)
            return
        }

        var sort = SORT_ALGO.value(opts)
        if (opts.has(WRAPPING_SORT_ALGO)) {
            sort = WRAPPING_SORT_ALGO.value(opts).wrap(sort, THRESHOLD.value(opts))
        }
        val visi = VisiSort(sort)
        System.err.println(sort.name)

        // start up the main thread queue
        val queue = ArrayBlockingQueue<Runnable>(16, true)
        val scope = CoroutineScope(
                SupervisorJob() + NormalMainDispatcher(queue, Thread.currentThread())
                        + CoroutineName("Main routine")
        )
        val mainRoutine = scope.launch {
            visi.run(getVisiData(DATA_LENGTH.value(opts)))
        }

        while (!mainRoutine.isCompleted) {
            queue.take().run()
        }
    }

    private fun getVisiData(len: Int): MutableList<Data> {
        val array = (0 until len).map { Data(it, it) }.toMutableList()
        Util.shuffle(array)
        return array
    }

}
