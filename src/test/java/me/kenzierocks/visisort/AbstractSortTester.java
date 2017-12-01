package me.kenzierocks.visisort;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class AbstractSortTester {

    private static final long MAX_RUNTIME = TimeUnit.SECONDS.toNanos(60L);

    private static final boolean DEBUG = Boolean.valueOf(System.getProperty("visisort.test.debug", "false"));

    private static final int DEFAULT_ARRAY_SIZE = DEBUG ? 10 : 0xDED;

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
