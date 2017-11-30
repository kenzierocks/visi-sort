package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.Result;
import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.Util;
import me.kenzierocks.visisort.op.Compare;
import me.kenzierocks.visisort.op.Finish;
import me.kenzierocks.visisort.op.Get;
import me.kenzierocks.visisort.op.Set;

public class BogoSort implements SortAlgo {

    @Override
    public String getName() {
        return "Bogo Sort";
    }

    private enum State {
        LEARN_ARRAY,
        SET_NEXT,
        CHECK_SORT,
    }

    private int array = -1;
    private int[] content;
    private int index = 0;

    private State state = State.LEARN_ARRAY;

    @Override
    public SortOp pulse(int size, Result result) {
        if (size < 2) {
            // sorted
            return new Finish();
        }
        if (array == -1) {
            // initial state
            array = result.getValue();
            content = new int[size];
        }
        switch (state) {
            case LEARN_ARRAY:
                if (index > 0) {
                    content[index - 1] = result.getValue();
                }
                if (index >= size) {
                    index = 0;
                    state = State.SET_NEXT;
                    Util.shuffle(content);
                    break;
                }
                Get get = new Get(array, index);
                index++;
                return get;
            case SET_NEXT:
                if (index >= size) {
                    index = 0;
                    state = State.CHECK_SORT;
                    break;
                }
                Set set = new Set(array, index, content[index]);
                index++;
                return set;
            case CHECK_SORT:
                if (index > 0) {
                    if (result.getValue() == 1) {
                        // not sorted, try again!
                        index = 0;
                        state = State.SET_NEXT;
                        Util.shuffle(content);
                        break;
                    }
                }
                if (index + 1 >= size) {
                    // yay, sorted
                    return new Finish();
                }
                Compare comp = new Compare(array, index, index + 1);
                index++;
                return comp;
        }
        return null;
    }

}
