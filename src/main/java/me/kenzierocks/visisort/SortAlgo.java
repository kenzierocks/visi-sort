package me.kenzierocks.visisort;

import javax.annotation.Nullable;

public interface SortAlgo {

    String getName();

    /**
     * Pulses one sort operation.
     * 
     * @param size
     *            - the size of the array
     * @param result
     *            - the result of the last operation, the first one is the
     *            initial array index
     */
    SortOp pulse(int size, @Nullable Result result);

}
