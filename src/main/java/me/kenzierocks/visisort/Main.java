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

import com.google.common.collect.ImmutableSortedMap;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;
import me.kenzierocks.visisort.algo.BogoSort;
import me.kenzierocks.visisort.algo.BubbleSort;
import me.kenzierocks.visisort.algo.InsertionSort;
import me.kenzierocks.visisort.algo.ParallelMergeSort;
import me.kenzierocks.visisort.algo.ParallelMergeSortWithThresholdSwap;
import me.kenzierocks.visisort.algo.QuickSort;
import me.kenzierocks.visisort.algo.QuickSortWithThresholdSwap;
import me.kenzierocks.visisort.algo.ShellSort;

import java.io.IOException;
import java.util.Map;

import static java.util.Arrays.asList;

public class Main {

    private static final ValueConverter<SortAlgo> SORT_ALGO_CONVERTER = new ValueConverter<SortAlgo>() {

        private final Map<String, SortAlgo> algoMap = ImmutableSortedMap.<String, SortAlgo>naturalOrder()
            .put("parallel-merge", new ParallelMergeSort())
            .put("quick-sort", new QuickSort())
            .put("bogo", new BogoSort())
            .put("bubble", new BubbleSort())
            .put("insertion", new InsertionSort())
            .put("shell", new ShellSort())
            .build();

        @Override
        public SortAlgo convert(String value) {
            if (!algoMap.containsKey(value)) {
                throw new ValueConversionException("Invalid value for SortAlgo: " + value);
            }
            return algoMap.get(value);
        }

        @Override
        public Class<? extends SortAlgo> valueType() {
            return SortAlgo.class;
        }

        @Override
        public String valuePattern() {
            return String.join("|", algoMap.keySet());
        }
    };

    private interface WrappingSortAlgo {

        SortAlgo wrap(SortAlgo delegate, int threshold);

    }

    private static final ValueConverter<WrappingSortAlgo> WRAPPING_SORT_ALGO_CONVERTER = new ValueConverter<WrappingSortAlgo>() {

        private final Map<String, WrappingSortAlgo> algoMap = ImmutableSortedMap.<String, WrappingSortAlgo>naturalOrder()
            .put("parallel-merge", ParallelMergeSortWithThresholdSwap::new)
            .put("quick-sort", QuickSortWithThresholdSwap::new)
            .build();

        @Override
        public WrappingSortAlgo convert(String value) {
            if (!algoMap.containsKey(value)) {
                throw new ValueConversionException("Invalid value for WrappingSortAlgo: " + value);
            }
            return algoMap.get(value);
        }

        @Override
        public Class<? extends WrappingSortAlgo> valueType() {
            return WrappingSortAlgo.class;
        }

        @Override
        public String valuePattern() {
            return String.join("|", algoMap.keySet());
        }
    };

    private static final OptionParser PARSER = new OptionParser();

    private static final AbstractOptionSpec<Void> HELP =
        PARSER.acceptsAll(asList("h", "help"), "Show this help")
            .forHelp();

    private static final ArgumentAcceptingOptionSpec<SortAlgo> SORT_ALGO =
        PARSER.acceptsAll(asList("a", "algorithm"), "Sort algorithm")
            .withRequiredArg()
            .withValuesConvertedBy(SORT_ALGO_CONVERTER)
            .required();

    private static final ArgumentAcceptingOptionSpec<WrappingSortAlgo> WRAPPING_SORT_ALGO =
        PARSER.acceptsAll(asList("w", "wrapping-algorithm"), "Sort algorithm to start with, swapping to --algorithm at --threshold")
            .withRequiredArg()
            .withValuesConvertedBy(WRAPPING_SORT_ALGO_CONVERTER);

    private static final ArgumentAcceptingOptionSpec<Integer> THRESHOLD =
        PARSER.acceptsAll(asList("t", "threshold"), "Threshold to swap from --wrapping-algorithm to --algorithm")
            .availableIf(WRAPPING_SORT_ALGO)
            .requiredIf(WRAPPING_SORT_ALGO)
            .withRequiredArg()
            .ofType(int.class);

    private static final ArgumentAcceptingOptionSpec<Integer> DATA_LENGTH =
        PARSER.acceptsAll(asList("l", "length"), "Data length")
            .withRequiredArg()
            .ofType(int.class)
            .defaultsTo(128);

    public static void main(String[] args) throws IOException {
        OptionSet opts;
        try {
            opts = PARSER.parse(args);
        } catch (OptionException e) {
            System.err.println(e.getMessage());
            PARSER.printHelpOn(System.err);
            System.exit(1);
            return;
        }

        if (opts.has(HELP)) {
            PARSER.printHelpOn(System.err);
            return;
        }

        SortAlgo sort = SORT_ALGO.value(opts);
        if (opts.has(WRAPPING_SORT_ALGO)) {
            sort = WRAPPING_SORT_ALGO.value(opts).wrap(sort, THRESHOLD.value(opts));
        }
        VisiSort visi = new VisiSort(sort);
        System.err.println(sort.getName());
        visi.run(getVisiData(DATA_LENGTH.value(opts)));
    }

    private static int[] getVisiData(int len) {
        int[] array = new int[len];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Util.shuffle(array);
        return array;
    }

}
