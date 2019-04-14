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
package me.kenzierocks.visisort;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class AbstractSortTester {

    private static final long MAX_RUNTIME = TimeUnit.MINUTES.toNanos(10L);

    private static final boolean DEBUG = Boolean.valueOf(System.getProperty("visisort.test.debug", "false"));

    private static final int DEFAULT_ARRAY_SIZE = DEBUG ? 10 : 1024;

    private static final int[] EMPTY_DATA = {};

    public static void assertSorts(Supplier<SortAlgo> algo) {
        assertSorts(algo, DEFAULT_ARRAY_SIZE);
    }

    public static void assertSorts(Supplier<SortAlgo> algo, int size) {
        assertSorts("random data 1", algo.get(), getRandomData(size));
        assertSorts("random data 2", algo.get(), getRandomData(size));
        assertSorts("random data 3", algo.get(), getRandomData(size));
        assertSorts("sorted data", algo.get(), getSortedData(size));
        assertSorts("length 1 data", algo.get(), getSmallData());
        assertSorts("length 0 data", algo.get(), EMPTY_DATA);
    }

    private static int[] getRandomData(int size) {
        return ThreadLocalRandom.current().ints(size).toArray();
    }

    private static int[] getSortedData(int size) {
        int[] data = getRandomData(size);
        Arrays.sort(data);
        return data;
    }

    private static int[] getSmallData() {
        return new int[] { 42 };
    }

    public static void assertSorts(String dataDesc, SortAlgo algo, int[] data) {
        String fullId = "data=" + dataDesc + ",algo=" + algo.getName();
        int[] output = data.clone();
        AlgoRunner runner = new AlgoRunner(output, algo, v -> {
            if (DEBUG) {
                System.err.println("Step: " + fullId + ",step=" + v);
            }
        });
        long start = System.nanoTime();
        long end = start + MAX_RUNTIME;
        runner.start();
        while (runner.pulse()) {
            if (DEBUG) {
                System.err.println(fullId + ",array=" + Arrays.toString(output));
            }
            if (System.nanoTime() > end) {
                fail("Time limit exceeded: " + fullId);
            }
        }
        // sort data using built-in sorts, then compare
        int[] sorted = data.clone();
        Arrays.sort(sorted);
        assertArrayEquals("Incorrect sort: " + fullId, sorted, output);
    }

}
