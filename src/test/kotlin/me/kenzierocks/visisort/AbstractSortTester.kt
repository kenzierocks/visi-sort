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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.slf4j.LoggerFactory
import java.util.Arrays
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

abstract class AbstractSortTester {
    companion object {

        private val MAX_RUNTIME = TimeUnit.MINUTES.toNanos(10L)

        private val DEBUG = System.getProperty("visisort.test.debug", "false")!!.toBoolean()

        private val DEFAULT_ARRAY_SIZE = when {
            DEBUG -> 4
            else -> 1024
        }

        private val LOGGER = LoggerFactory.getLogger(AbstractSortTester::class.java)

        private val EMPTY_DATA = intArrayOf()

        fun assertSorts(algo: SortAlgo, size: Int = DEFAULT_ARRAY_SIZE) {
            assertSorts("random data 1", algo, getRandomData(size))
            assertSorts("random data 2", algo, getRandomData(size))
            assertSorts("random data 3", algo, getRandomData(size))
            assertSorts("sorted data", algo, getSortedData(size))
            assertSorts("length 1 data", algo, smallData)
            assertSorts("length 0 data", algo, EMPTY_DATA)
        }

        private fun index(data: IntArray): MutableList<Data> {
            return data.mapIndexed { index, value -> Data(value, index) }.toMutableList()
        }

        private fun getRandomData(size: Int): IntArray {
            return ThreadLocalRandom.current().ints(size.toLong(), 0, size).toArray()
        }

        private fun getSortedData(size: Int): IntArray {
            val data = getRandomData(size)
            Arrays.sort(data)
            return data
        }

        private val smallData: IntArray
            get() = intArrayOf(42)

        @Throws(InterruptedException::class)
        fun assertSorts(dataDesc: String, algo: SortAlgo, data: IntArray) {
            LOGGER.info("Starting test for data=$dataDesc,algo=${algo.name}")
            val output = index(data)
            val operationInput = Channel<OpResult<*>>(Channel.UNLIMITED)
            val runner = AlgoRunner(output, algo, operationInput,
                    CoroutineScope(CoroutineName("Algorithm") + Dispatchers.Default))
            runBlocking(CoroutineName("Test Coroutines")) {
                val start = System.nanoTime()
                val end = start + MAX_RUNTIME
                runner.start()
                do {
                    while (true) {
                        val v = operationInput.poll() ?: break
                        if (DEBUG) {
                            LOGGER.debug("Step: $v")
                        }
                    }
                    if (System.nanoTime() > end) {
                        fail("Time limit exceeded")
                    }
                    if (DEBUG) {
                        LOGGER.debug("About to pulse, state is as follows")
                        runner.arrays.forEach { array ->
                            LOGGER.debug("$array = ${array.data.map { it.value }}")
                        }
                    }
                } while (runner.pulse())
            }
            // sort data using built-in sorts, then compare
            val javaSorted = data.sorted()
            val testSorted = output.map { it.value }
            assertEquals("Incorrect sort", javaSorted, testSorted)
        }
    }

}
