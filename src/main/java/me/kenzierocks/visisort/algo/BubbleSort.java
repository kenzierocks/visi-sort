package me.kenzierocks.visisort.algo;

import static com.google.common.base.Preconditions.checkState;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.op.Compare;
import me.kenzierocks.visisort.op.Finish;
import me.kenzierocks.visisort.op.Swap;

public class BubbleSort implements SortAlgo {

    @Override
    public String getName() {
        return "Bubble Sort";
    }

    private enum State {
        SEND_COMPARE,
        MAYBE_SWAP,
        AWAIT_SWAP,
    }

    private int array = -1;
    private int index = 0;
    private boolean anySwaps;

    private State state = State.SEND_COMPARE;

    @Override
    public SortOp pulse(int size, Result result) {
        if (array == -1) {
            // initial state
            array = result.getValue();
        }
        if (size < 2) {
            // sorted
            return new Finish();
        }
        if (index + 1 >= size) {
            checkState(state == State.SEND_COMPARE);
            // reached end of array, reset
            if (!anySwaps) {
                return new Finish();
            }
            anySwaps = false;
            index = 0;
        }
        switch (state) {
            case SEND_COMPARE:
                state = State.MAYBE_SWAP;
                return new Compare(array, index, index + 1);
            case MAYBE_SWAP:
                if (result.getValue() == 1) {
                    anySwaps = true;
                    state = State.AWAIT_SWAP;
                    return new Swap(array, index, index + 1);
                }
                //$FALL-THROUGH$
            case AWAIT_SWAP:
                index++;
                state = State.SEND_COMPARE;
        }
        return null;
    }

}
